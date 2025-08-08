package dev.kreaker.vtda.service.impl;

import dev.kreaker.vtda.exception.DatabaseConnectionException;
import dev.kreaker.vtda.exception.MetadataExtractionException;
import dev.kreaker.vtda.exception.SchemaNotFoundException;
import dev.kreaker.vtda.model.*;
import dev.kreaker.vtda.service.MetadataExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of MetadataExtractionService for Oracle databases.
 * Uses Oracle system views (ALL_TABLES, ALL_TAB_COLUMNS, etc.) to extract comprehensive metadata.
 */
@Slf4j
@Service
public class MetadataExtractionServiceImpl implements MetadataExtractionService {
    
    private final DatabaseConfig databaseConfig;
    private final DataSource dataSource;
    
    // Oracle system view queries
    private static final String SCHEMA_VALIDATION_QUERY = 
            "SELECT COUNT(*) FROM ALL_USERS WHERE USERNAME = UPPER(?)";
    
    private static final String AVAILABLE_SCHEMAS_QUERY = 
            "SELECT USERNAME FROM ALL_USERS ORDER BY USERNAME";
    
    private static final String TABLE_METADATA_QUERY = """
            SELECT t.OWNER, t.TABLE_NAME, t.TABLESPACE_NAME, 
                   c.COMMENTS as TABLE_COMMENT,
                   t.CREATED, t.LAST_ANALYZED,
                   t.NUM_ROWS, t.BLOCKS, t.AVG_ROW_LEN
            FROM ALL_TABLES t
            LEFT JOIN ALL_TAB_COMMENTS c ON t.OWNER = c.OWNER AND t.TABLE_NAME = c.TABLE_NAME
            WHERE t.OWNER = UPPER(?)
            ORDER BY t.TABLE_NAME
            """;
    
    private static final String COLUMN_METADATA_QUERY = """
            SELECT c.COLUMN_NAME, c.DATA_TYPE, c.DATA_LENGTH, c.DATA_PRECISION, c.DATA_SCALE,
                   c.NULLABLE, c.DATA_DEFAULT, c.COLUMN_ID, c.CHAR_LENGTH, c.CHAR_USED,
                   cc.COMMENTS as COLUMN_COMMENT,
                   CASE 
                       WHEN c.DATA_TYPE IN ('VARCHAR2', 'NVARCHAR2', 'CHAR', 'NCHAR') THEN 
                           CASE WHEN c.CHAR_USED = 'C' THEN c.CHAR_LENGTH ELSE c.DATA_LENGTH END
                       ELSE c.DATA_LENGTH 
                   END as EFFECTIVE_LENGTH
            FROM ALL_TAB_COLUMNS c
            LEFT JOIN ALL_COL_COMMENTS cc ON c.OWNER = cc.OWNER 
                AND c.TABLE_NAME = cc.TABLE_NAME AND c.COLUMN_NAME = cc.COLUMN_NAME
            WHERE c.OWNER = UPPER(?) AND c.TABLE_NAME = UPPER(?)
            ORDER BY c.COLUMN_ID
            """;
    
    private static final String PRIMARY_KEY_QUERY = """
            SELECT cons.CONSTRAINT_NAME, cons.INDEX_NAME, cons.STATUS, cons.VALIDATED,
                   cons.GENERATED, cols.COLUMN_NAME, cols.POSITION
            FROM ALL_CONSTRAINTS cons
            JOIN ALL_CONS_COLUMNS cols ON cons.OWNER = cols.OWNER 
                AND cons.CONSTRAINT_NAME = cols.CONSTRAINT_NAME
            WHERE cons.OWNER = UPPER(?) AND cons.TABLE_NAME = UPPER(?) 
                AND cons.CONSTRAINT_TYPE = 'P'
            ORDER BY cols.POSITION
            """;
    
    private static final String INDEX_METADATA_QUERY = """
            SELECT i.INDEX_NAME, i.INDEX_TYPE, i.UNIQUENESS, i.STATUS, i.TABLESPACE_NAME,
                   i.DEGREE, i.GENERATED, ic.COLUMN_NAME, ic.COLUMN_POSITION, ic.DESCEND
            FROM ALL_INDEXES i
            JOIN ALL_IND_COLUMNS ic ON i.OWNER = ic.INDEX_OWNER AND i.INDEX_NAME = ic.INDEX_NAME
            WHERE i.TABLE_OWNER = UPPER(?) AND i.TABLE_NAME = UPPER(?)
                AND i.INDEX_TYPE != 'LOB'
            ORDER BY i.INDEX_NAME, ic.COLUMN_POSITION
            """;
    
    private static final String CONSTRAINT_METADATA_QUERY = """
            SELECT cons.CONSTRAINT_NAME, cons.CONSTRAINT_TYPE, cons.STATUS, cons.VALIDATED,
                   cons.GENERATED, cons.R_CONSTRAINT_NAME, cons.DELETE_RULE,
                   cols.COLUMN_NAME, cols.POSITION
            FROM ALL_CONSTRAINTS cons
            LEFT JOIN ALL_CONS_COLUMNS cols ON cons.OWNER = cols.OWNER 
                AND cons.CONSTRAINT_NAME = cols.CONSTRAINT_NAME
            WHERE cons.OWNER = UPPER(?) AND cons.TABLE_NAME = UPPER(?)
                AND cons.CONSTRAINT_TYPE IN ('C', 'U', 'R')
            ORDER BY cons.CONSTRAINT_TYPE, cons.CONSTRAINT_NAME, cols.POSITION
            """;
    
    /**
     * Constructs a new MetadataExtractionServiceImpl with the specified configuration and data source.
     * 
     * @param databaseConfig the database configuration
     * @param dataSource the data source for database connections
     */
    public MetadataExtractionServiceImpl(DatabaseConfig databaseConfig, DataSource dataSource) {
        this.databaseConfig = databaseConfig;
        this.dataSource = dataSource;
        log.info("Initialized MetadataExtractionService for schema: {}", databaseConfig.getSchema());
    }
    
    @Override
    public List<TableMetadata> extractTableMetadata(String schema, FilterConfig filters) 
            throws MetadataExtractionException, SchemaNotFoundException, DatabaseConnectionException {
        
        log.info("Starting metadata extraction for schema: {} with filters: {}", schema, filters);
        
        validateConnection();
        validateSchema(schema);
        
        try (Connection connection = dataSource.getConnection()) {
            
            List<TableMetadata> tables = new ArrayList<>();
            List<String> tableNames = getFilteredTableNames(connection, schema, filters);
            
            log.info("Found {} tables matching filter criteria in schema: {}", tableNames.size(), schema);
            
            for (String tableName : tableNames) {
                try {
                    TableMetadata tableMetadata = extractSingleTableMetadata(connection, schema, tableName);
                    tables.add(tableMetadata);
                    log.debug("Extracted metadata for table: {}.{}", schema, tableName);
                } catch (SQLException e) {
                    log.warn("Failed to extract metadata for table {}.{}: {}", schema, tableName, e.getMessage());
                    // Continue with other tables rather than failing completely
                }
            }
            
            log.info("Successfully extracted metadata for {} tables from schema: {}", tables.size(), schema);
            return tables;
            
        } catch (SQLException e) {
            throw new MetadataExtractionException(
                    "Failed to extract table metadata from schema: " + schema, 
                    "SQL Error: " + e.getMessage(), 
                    e
            );
        }
    }
    
    @Override
    public List<String> getAvailableSchemas() throws DatabaseConnectionException, MetadataExtractionException {
        log.debug("Retrieving available schemas");
        
        validateConnection();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(AVAILABLE_SCHEMAS_QUERY)) {
            
            stmt.setQueryTimeout(databaseConfig.getQueryTimeout());
            
            try (ResultSet rs = stmt.executeQuery()) {
            
            List<String> schemas = new ArrayList<>();
            while (rs.next()) {
                schemas.add(rs.getString("USERNAME"));
            }
            
            log.debug("Found {} available schemas", schemas.size());
            return schemas;
            }
            
        } catch (SQLException e) {
            throw new MetadataExtractionException(
                    "Failed to retrieve available schemas", 
                    "SQL Error: " + e.getMessage(), 
                    e
            );
        }
    }
    
    @Override
    public void validateConnection() throws DatabaseConnectionException {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(databaseConfig.getConnectionTimeout())) {
                throw new DatabaseConnectionException(
                        "Database connection validation failed",
                        "Connection timeout: " + databaseConfig.getConnectionTimeout() + " seconds"
                );
            }
            log.debug("Database connection validated successfully");
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                    "Failed to validate database connection",
                    "SQL Error: " + e.getMessage(),
                    e
            );
        }
    }
    
    @Override
    public void validateSchema(String schema) throws SchemaNotFoundException, DatabaseConnectionException {
        log.debug("Validating schema: {}", schema);
        
        validateConnection();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SCHEMA_VALIDATION_QUERY)) {
            
            stmt.setQueryTimeout(databaseConfig.getQueryTimeout());
            stmt.setString(1, schema);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    log.debug("Schema validation successful: {}", schema);
                    return;
                }
            }
            
            // Schema not found, get available schemas for better error message
            try {
                List<String> availableSchemas = getAvailableSchemas();
                String suggestion = availableSchemas.isEmpty() ? 
                        "No schemas are accessible to the current user" :
                        "Available schemas: " + String.join(", ", availableSchemas);
                
                throw new SchemaNotFoundException(schema, suggestion);
            } catch (MetadataExtractionException e) {
                throw new SchemaNotFoundException(schema, "Unable to retrieve available schemas", e);
            }
            
        } catch (SQLException e) {
            throw new SchemaNotFoundException(
                    schema,
                    "SQL Error during schema validation: " + e.getMessage(),
                    e
            );
        }
    }
    
    @Override
    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }
    
    @Override
    public void close() {
        // DataSource cleanup is handled by Spring Boot
        log.debug("MetadataExtractionService closed");
    }
    
    /**
     * Gets filtered table names from the specified schema.
     */
    private List<String> getFilteredTableNames(Connection connection, String schema, FilterConfig filters) 
            throws SQLException {
        
        List<String> allTableNames = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(TABLE_METADATA_QUERY)) {
            stmt.setQueryTimeout(databaseConfig.getQueryTimeout());
            stmt.setString(1, schema);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    allTableNames.add(rs.getString("TABLE_NAME"));
                }
            }
        }
        
        // Apply filters if configured
        if (filters != null && filters.hasFilters()) {
            return allTableNames.stream()
                    .filter(filters::matches)
                    .collect(Collectors.toList());
        }
        
        return allTableNames;
    }
    
    /**
     * Extracts complete metadata for a single table.
     */
    private TableMetadata extractSingleTableMetadata(Connection connection, String schema, String tableName) 
            throws SQLException {
        
        // Extract basic table information
        TableMetadata.TableMetadataBuilder builder = TableMetadata.builder()
                .schemaName(schema)
                .tableName(tableName);
        
        // Get table-level metadata
        extractTableInfo(connection, schema, tableName, builder);
        
        // Get column metadata
        List<ColumnMetadata> columns = extractColumnMetadata(connection, schema, tableName);
        builder.columns(columns);
        
        // Get primary key metadata
        PrimaryKeyMetadata primaryKey = extractPrimaryKeyMetadata(connection, schema, tableName);
        if (primaryKey != null) {
            builder.primaryKey(primaryKey);
        }
        
        // Get index metadata
        List<IndexMetadata> indexes = extractIndexMetadata(connection, schema, tableName);
        builder.indexes(indexes);
        
        // Extract additional constraints (check, unique, foreign key)
        extractAdditionalConstraints(connection, schema, tableName, builder);
        
        TableMetadata tableMetadata = builder.build();
        
        // Validate the extracted metadata
        validateExtractedMetadata(tableMetadata);
        
        return tableMetadata;
    }
    
    /**
     * Extracts basic table information (comment, creation date, etc.).
     */
    private void extractTableInfo(Connection connection, String schema, String tableName, 
                                 TableMetadata.TableMetadataBuilder builder) throws SQLException {
        
        try (PreparedStatement stmt = connection.prepareStatement(TABLE_METADATA_QUERY)) {
            stmt.setQueryTimeout(databaseConfig.getQueryTimeout());
            stmt.setString(1, schema);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (tableName.equals(rs.getString("TABLE_NAME"))) {
                        builder.tableComment(rs.getString("TABLE_COMMENT"));
                        
                        Timestamp created = rs.getTimestamp("CREATED");
                        if (created != null) {
                            builder.createdDate(created.toInstant());
                        }
                        
                        Timestamp lastAnalyzed = rs.getTimestamp("LAST_ANALYZED");
                        if (lastAnalyzed != null) {
                            builder.lastModified(lastAnalyzed.toInstant());
                        }
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Extracts column metadata for the specified table with comprehensive Oracle data type mapping.
     */
    private List<ColumnMetadata> extractColumnMetadata(Connection connection, String schema, String tableName) 
            throws SQLException {
        
        List<ColumnMetadata> columns = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(COLUMN_METADATA_QUERY)) {
            stmt.setQueryTimeout(databaseConfig.getQueryTimeout());
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String rawDataType = rs.getString("DATA_TYPE");
                    Integer dataLength = getIntegerOrNull(rs, "DATA_LENGTH");
                    Integer dataPrecision = getIntegerOrNull(rs, "DATA_PRECISION");
                    Integer dataScale = getIntegerOrNull(rs, "DATA_SCALE");
                    Integer effectiveLength = getIntegerOrNull(rs, "EFFECTIVE_LENGTH");
                    String charUsed = rs.getString("CHAR_USED");
                    
                    // Use effective length for character types, otherwise use data length
                    Integer finalLength = isCharacterDataType(rawDataType) ? effectiveLength : dataLength;
                    
                    // Map Oracle data type to standardized representation
                    String mappedDataType = mapOracleDataType(rawDataType, finalLength, dataPrecision, dataScale, charUsed);
                    
                    // Clean up default value (Oracle includes trailing spaces and newlines)
                    String defaultValue = rs.getString("DATA_DEFAULT");
                    if (defaultValue != null) {
                        defaultValue = defaultValue.trim();
                        if (defaultValue.isEmpty()) {
                            defaultValue = null;
                        }
                    }
                    
                    ColumnMetadata column = ColumnMetadata.builder()
                            .columnName(rs.getString("COLUMN_NAME"))
                            .dataType(mappedDataType)
                            .dataLength(finalLength)
                            .dataPrecision(dataPrecision)
                            .dataScale(dataScale)
                            .nullable("Y".equals(rs.getString("NULLABLE")))
                            .defaultValue(defaultValue)
                            .columnComment(rs.getString("COLUMN_COMMENT"))
                            .columnId(rs.getInt("COLUMN_ID"))
                            .build();
                    
                    columns.add(column);
                    log.debug("Extracted column metadata: {}.{}.{} - {}", 
                             schema, tableName, column.getColumnName(), mappedDataType);
                }
            }
        }
        
        log.debug("Extracted {} columns for table {}.{}", columns.size(), schema, tableName);
        return columns;
    }
    
    /**
     * Extracts primary key metadata for the specified table with enhanced constraint information.
     */
    private PrimaryKeyMetadata extractPrimaryKeyMetadata(Connection connection, String schema, String tableName) 
            throws SQLException {
        
        try (PreparedStatement stmt = connection.prepareStatement(PRIMARY_KEY_QUERY)) {
            stmt.setQueryTimeout(databaseConfig.getQueryTimeout());
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    log.debug("No primary key found for table {}.{}", schema, tableName);
                    return null; // No primary key
                }
                
                String constraintName = rs.getString("CONSTRAINT_NAME");
                String indexName = rs.getString("INDEX_NAME");
                boolean enabled = "ENABLED".equals(rs.getString("STATUS"));
                boolean validated = "VALIDATED".equals(rs.getString("VALIDATED"));
                String generated = rs.getString("GENERATED");
                
                List<String> columnNames = new ArrayList<>();
                
                // Process first row
                columnNames.add(rs.getString("COLUMN_NAME"));
                
                // Process remaining rows (for composite primary keys)
                while (rs.next()) {
                    columnNames.add(rs.getString("COLUMN_NAME"));
                }
                
                PrimaryKeyMetadata primaryKey = PrimaryKeyMetadata.builder()
                        .constraintName(constraintName)
                        .columnNames(columnNames)
                        .indexName(indexName)
                        .enabled(enabled)
                        .validated(validated)
                        .build();
                
                log.debug("Extracted primary key for table {}.{}: {} on columns [{}]", 
                         schema, tableName, constraintName, String.join(", ", columnNames));
                
                return primaryKey;
            }
        }
    }
    
    /**
     * Extracts index metadata for the specified table with comprehensive index information.
     */
    private List<IndexMetadata> extractIndexMetadata(Connection connection, String schema, String tableName) 
            throws SQLException {
        
        Map<String, IndexInfo> indexInfoMap = new LinkedHashMap<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(INDEX_METADATA_QUERY)) {
            stmt.setQueryTimeout(databaseConfig.getQueryTimeout());
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    String columnName = rs.getString("COLUMN_NAME");
                    String descend = rs.getString("DESCEND");
                    
                    IndexInfo indexInfo = indexInfoMap.get(indexName);
                    if (indexInfo == null) {
                        indexInfo = new IndexInfo();
                        indexInfo.indexName = indexName;
                        indexInfo.indexType = rs.getString("INDEX_TYPE");
                        indexInfo.unique = "UNIQUE".equals(rs.getString("UNIQUENESS"));
                        indexInfo.status = rs.getString("STATUS");
                        indexInfo.tablespace = rs.getString("TABLESPACE_NAME");
                        indexInfo.degree = getIntegerOrNull(rs, "DEGREE");
                        indexInfo.generated = rs.getString("GENERATED");
                        indexInfo.columnNames = new ArrayList<>();
                        indexInfoMap.put(indexName, indexInfo);
                    }
                    
                    // Add column name with sort order if descending
                    String columnSpec = columnName;
                    if ("DESC".equals(descend)) {
                        columnSpec += " DESC";
                    }
                    indexInfo.columnNames.add(columnSpec);
                }
            }
        }
        
        List<IndexMetadata> indexes = new ArrayList<>();
        for (IndexInfo info : indexInfoMap.values()) {
            // Determine if this is a primary key index
            boolean isPrimaryKeyIndex = "Y".equals(info.generated) && info.unique;
            
            IndexMetadata index = IndexMetadata.builder()
                    .indexName(info.indexName)
                    .indexType(info.indexType)
                    .columnNames(info.columnNames)
                    .unique(info.unique)
                    .status(info.status)
                    .tablespace(info.tablespace)
                    .primaryKeyIndex(isPrimaryKeyIndex)
                    .degree(info.degree)
                    .build();
            
            indexes.add(index);
            
            log.debug("Extracted index for table {}.{}: {} ({}) on columns [{}]", 
                     schema, tableName, info.indexName, info.indexType, 
                     String.join(", ", info.columnNames));
        }
        
        log.debug("Extracted {} indexes for table {}.{}", indexes.size(), schema, tableName);
        return indexes;
    }
    
    /**
     * Extracts additional constraint information (check, unique, foreign key constraints).
     */
    private void extractAdditionalConstraints(Connection connection, String schema, String tableName,
                                            TableMetadata.TableMetadataBuilder builder) throws SQLException {
        
        try (PreparedStatement stmt = connection.prepareStatement(CONSTRAINT_METADATA_QUERY)) {
            stmt.setQueryTimeout(databaseConfig.getQueryTimeout());
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                Map<String, ConstraintInfo> constraintMap = new LinkedHashMap<>();
                
                while (rs.next()) {
                    String constraintName = rs.getString("CONSTRAINT_NAME");
                    String constraintType = rs.getString("CONSTRAINT_TYPE");
                    String columnName = rs.getString("COLUMN_NAME");
                    
                    ConstraintInfo constraintInfo = constraintMap.get(constraintName);
                    if (constraintInfo == null) {
                        constraintInfo = new ConstraintInfo();
                        constraintInfo.constraintName = constraintName;
                        constraintInfo.constraintType = constraintType;
                        constraintInfo.status = rs.getString("STATUS");
                        constraintInfo.validated = "VALIDATED".equals(rs.getString("VALIDATED"));
                        constraintInfo.generated = rs.getString("GENERATED");
                        constraintInfo.referencedConstraint = rs.getString("R_CONSTRAINT_NAME");
                        constraintInfo.deleteRule = rs.getString("DELETE_RULE");
                        constraintInfo.columnNames = new ArrayList<>();
                        constraintMap.put(constraintName, constraintInfo);
                    }
                    
                    if (columnName != null) {
                        constraintInfo.columnNames.add(columnName);
                    }
                }
                
                // Log constraint information for debugging
                for (ConstraintInfo info : constraintMap.values()) {
                    String typeDescription = getConstraintTypeDescription(info.constraintType);
                    log.debug("Found {} constraint for table {}.{}: {} on columns [{}]", 
                             typeDescription, schema, tableName, info.constraintName,
                             String.join(", ", info.columnNames));
                }
            }
        }
    }
    
    /**
     * Gets a human-readable description for constraint types.
     */
    private String getConstraintTypeDescription(String constraintType) {
        switch (constraintType) {
            case "C": return "CHECK";
            case "U": return "UNIQUE";
            case "R": return "FOREIGN KEY";
            case "P": return "PRIMARY KEY";
            default: return constraintType;
        }
    }
    
    /**
     * Helper class to collect constraint information during extraction.
     */
    private static class ConstraintInfo {
        String constraintName;
        String constraintType;
        String status;
        boolean validated;
        String generated;
        String referencedConstraint;
        String deleteRule;
        List<String> columnNames;
    }
    
    /**
     * Helper class to collect index information during extraction.
     */
    private static class IndexInfo {
        String indexName;
        String indexType;
        boolean unique;
        String status;
        String tablespace;
        String generated;
        Integer degree;
        List<String> columnNames;
    }
    
    /**
     * Helper method to safely extract Integer values from ResultSet, returning null for SQL NULL.
     */
    private Integer getIntegerOrNull(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
    
    /**
     * Maps Oracle data types to standardized internal representation with proper handling
     * of Oracle-specific types like VARCHAR2, NVARCHAR2, NUMBER, TIMESTAMP, etc.
     */
    private String mapOracleDataType(String oracleType, Integer length, Integer precision, Integer scale, String charUsed) {
        if (oracleType == null) {
            return "UNKNOWN";
        }
        
        String upperType = oracleType.toUpperCase();
        
        switch (upperType) {
            case "VARCHAR2":
                if (length != null && length > 0) {
                    String unit = "C".equals(charUsed) ? " CHAR" : " BYTE";
                    return String.format("VARCHAR2(%d%s)", length, unit);
                }
                return "VARCHAR2";
                
            case "NVARCHAR2":
                if (length != null && length > 0) {
                    return String.format("NVARCHAR2(%d)", length);
                }
                return "NVARCHAR2";
                
            case "CHAR":
                if (length != null && length > 0) {
                    String unit = "C".equals(charUsed) ? " CHAR" : " BYTE";
                    return String.format("CHAR(%d%s)", length, unit);
                }
                return "CHAR";
                
            case "NCHAR":
                if (length != null && length > 0) {
                    return String.format("NCHAR(%d)", length);
                }
                return "NCHAR";
                
            case "NUMBER":
                if (precision != null && precision > 0) {
                    if (scale != null && scale > 0) {
                        return String.format("NUMBER(%d,%d)", precision, scale);
                    } else if (scale != null && scale == 0) {
                        return String.format("NUMBER(%d)", precision);
                    } else {
                        return String.format("NUMBER(%d)", precision);
                    }
                }
                return "NUMBER";
                
            case "FLOAT":
                if (precision != null && precision > 0) {
                    return String.format("FLOAT(%d)", precision);
                }
                return "FLOAT";
                
            case "BINARY_FLOAT":
                return "BINARY_FLOAT";
                
            case "BINARY_DOUBLE":
                return "BINARY_DOUBLE";
                
            case "DATE":
                return "DATE";
                
            case "TIMESTAMP":
                if (scale != null && scale > 0) {
                    return String.format("TIMESTAMP(%d)", scale);
                }
                return "TIMESTAMP";
                
            case "TIMESTAMP WITH TIME ZONE":
                if (scale != null && scale > 0) {
                    return String.format("TIMESTAMP(%d) WITH TIME ZONE", scale);
                }
                return "TIMESTAMP WITH TIME ZONE";
                
            case "TIMESTAMP WITH LOCAL TIME ZONE":
                if (scale != null && scale > 0) {
                    return String.format("TIMESTAMP(%d) WITH LOCAL TIME ZONE", scale);
                }
                return "TIMESTAMP WITH LOCAL TIME ZONE";
                
            case "INTERVAL YEAR TO MONTH":
                if (precision != null && precision > 0) {
                    return String.format("INTERVAL YEAR(%d) TO MONTH", precision);
                }
                return "INTERVAL YEAR TO MONTH";
                
            case "INTERVAL DAY TO SECOND":
                if (precision != null && scale != null) {
                    return String.format("INTERVAL DAY(%d) TO SECOND(%d)", precision, scale);
                } else if (precision != null) {
                    return String.format("INTERVAL DAY(%d) TO SECOND", precision);
                }
                return "INTERVAL DAY TO SECOND";
                
            case "RAW":
                if (length != null && length > 0) {
                    return String.format("RAW(%d)", length);
                }
                return "RAW";
                
            case "LONG RAW":
                return "LONG RAW";
                
            case "LONG":
                return "LONG";
                
            case "CLOB":
                return "CLOB";
                
            case "NCLOB":
                return "NCLOB";
                
            case "BLOB":
                return "BLOB";
                
            case "BFILE":
                return "BFILE";
                
            case "ROWID":
                return "ROWID";
                
            case "UROWID":
                if (length != null && length > 0) {
                    return String.format("UROWID(%d)", length);
                }
                return "UROWID";
                
            case "XMLTYPE":
                return "XMLTYPE";
                
            default:
                // Handle user-defined types and other Oracle-specific types
                if (upperType.startsWith("TIMESTAMP(")) {
                    return upperType;
                }
                if (upperType.contains("INTERVAL")) {
                    return upperType;
                }
                return oracleType; // Return as-is for unknown types
        }
    }
    
    /**
     * Determines if the Oracle data type is a character-based type.
     */
    private boolean isCharacterDataType(String dataType) {
        if (dataType == null) return false;
        String upperType = dataType.toUpperCase();
        return upperType.startsWith("VARCHAR") || upperType.startsWith("NVARCHAR") ||
               upperType.startsWith("CHAR") || upperType.startsWith("NCHAR") ||
               upperType.equals("CLOB") || upperType.equals("NCLOB") || upperType.equals("LONG");
    }
    
    /**
     * Determines if the Oracle data type is a numeric type.
     */
    private boolean isNumericDataType(String dataType) {
        if (dataType == null) return false;
        String upperType = dataType.toUpperCase();
        return upperType.equals("NUMBER") || upperType.equals("INTEGER") || 
               upperType.equals("FLOAT") || upperType.equals("BINARY_FLOAT") || 
               upperType.equals("BINARY_DOUBLE");
    }
    
    /**
     * Determines if the Oracle data type is a temporal (date/time) type.
     */
    private boolean isTemporalDataType(String dataType) {
        if (dataType == null) return false;
        String upperType = dataType.toUpperCase();
        return upperType.equals("DATE") || upperType.startsWith("TIMESTAMP") || 
               upperType.startsWith("INTERVAL");
    }
    
    /**
     * Validates the extracted table metadata for consistency and completeness.
     */
    private void validateExtractedMetadata(TableMetadata tableMetadata) {
        if (tableMetadata == null) {
            throw new IllegalStateException("Table metadata cannot be null");
        }
        
        // Validate basic table information
        if (tableMetadata.getSchemaName() == null || tableMetadata.getSchemaName().trim().isEmpty()) {
            throw new IllegalStateException("Schema name cannot be null or empty");
        }
        
        if (tableMetadata.getTableName() == null || tableMetadata.getTableName().trim().isEmpty()) {
            throw new IllegalStateException("Table name cannot be null or empty");
        }
        
        // Validate columns
        List<ColumnMetadata> columns = tableMetadata.getColumns();
        if (columns == null || columns.isEmpty()) {
            log.warn("Table {}.{} has no columns", tableMetadata.getSchemaName(), tableMetadata.getTableName());
            return;
        }
        
        // Validate each column
        for (ColumnMetadata column : columns) {
            try {
                column.validate();
            } catch (IllegalStateException e) {
                log.warn("Invalid column metadata in table {}.{}: {}", 
                        tableMetadata.getSchemaName(), tableMetadata.getTableName(), e.getMessage());
            }
        }
        
        // Validate primary key if present
        PrimaryKeyMetadata primaryKey = tableMetadata.getPrimaryKey();
        if (primaryKey != null) {
            try {
                primaryKey.validate();
                
                // Verify primary key columns exist in table
                List<String> columnNames = columns.stream()
                        .map(ColumnMetadata::getColumnName)
                        .collect(Collectors.toList());
                
                for (String pkColumn : primaryKey.getColumnNames()) {
                    if (!columnNames.contains(pkColumn)) {
                        log.warn("Primary key column '{}' not found in table {}.{}", 
                                pkColumn, tableMetadata.getSchemaName(), tableMetadata.getTableName());
                    }
                }
            } catch (IllegalStateException e) {
                log.warn("Invalid primary key metadata in table {}.{}: {}", 
                        tableMetadata.getSchemaName(), tableMetadata.getTableName(), e.getMessage());
            }
        }
        
        // Validate indexes if present
        List<IndexMetadata> indexes = tableMetadata.getIndexes();
        if (indexes != null) {
            for (IndexMetadata index : indexes) {
                try {
                    index.validate();
                } catch (IllegalStateException e) {
                    log.warn("Invalid index metadata in table {}.{}: {}", 
                            tableMetadata.getSchemaName(), tableMetadata.getTableName(), e.getMessage());
                }
            }
        }
        
        log.debug("Metadata validation completed for table {}.{}", 
                 tableMetadata.getSchemaName(), tableMetadata.getTableName());
    }
}
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
                   t.CREATED, t.LAST_ANALYZED
            FROM ALL_TABLES t
            LEFT JOIN ALL_TAB_COMMENTS c ON t.OWNER = c.OWNER AND t.TABLE_NAME = c.TABLE_NAME
            WHERE t.OWNER = UPPER(?)
            ORDER BY t.TABLE_NAME
            """;
    
    private static final String COLUMN_METADATA_QUERY = """
            SELECT c.COLUMN_NAME, c.DATA_TYPE, c.DATA_LENGTH, c.DATA_PRECISION, c.DATA_SCALE,
                   c.NULLABLE, c.DATA_DEFAULT, c.COLUMN_ID,
                   cc.COMMENTS as COLUMN_COMMENT
            FROM ALL_TAB_COLUMNS c
            LEFT JOIN ALL_COL_COMMENTS cc ON c.OWNER = cc.OWNER 
                AND c.TABLE_NAME = cc.TABLE_NAME AND c.COLUMN_NAME = cc.COLUMN_NAME
            WHERE c.OWNER = UPPER(?) AND c.TABLE_NAME = UPPER(?)
            ORDER BY c.COLUMN_ID
            """;
    
    private static final String PRIMARY_KEY_QUERY = """
            SELECT cons.CONSTRAINT_NAME, cons.INDEX_NAME, cons.STATUS, cons.VALIDATED,
                   cols.COLUMN_NAME, cols.POSITION
            FROM ALL_CONSTRAINTS cons
            JOIN ALL_CONS_COLUMNS cols ON cons.OWNER = cols.OWNER 
                AND cons.CONSTRAINT_NAME = cols.CONSTRAINT_NAME
            WHERE cons.OWNER = UPPER(?) AND cons.TABLE_NAME = UPPER(?) 
                AND cons.CONSTRAINT_TYPE = 'P'
            ORDER BY cols.POSITION
            """;
    
    private static final String INDEX_METADATA_QUERY = """
            SELECT i.INDEX_NAME, i.INDEX_TYPE, i.UNIQUENESS, i.STATUS, i.TABLESPACE_NAME,
                   i.DEGREE, ic.COLUMN_NAME, ic.COLUMN_POSITION
            FROM ALL_INDEXES i
            JOIN ALL_IND_COLUMNS ic ON i.OWNER = ic.INDEX_OWNER AND i.INDEX_NAME = ic.INDEX_NAME
            WHERE i.TABLE_OWNER = UPPER(?) AND i.TABLE_NAME = UPPER(?)
            ORDER BY i.INDEX_NAME, ic.COLUMN_POSITION
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
        
        return builder.build();
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
     * Extracts column metadata for the specified table.
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
                    ColumnMetadata column = ColumnMetadata.builder()
                            .columnName(rs.getString("COLUMN_NAME"))
                            .dataType(rs.getString("DATA_TYPE"))
                            .dataLength(getIntegerOrNull(rs, "DATA_LENGTH"))
                            .dataPrecision(getIntegerOrNull(rs, "DATA_PRECISION"))
                            .dataScale(getIntegerOrNull(rs, "DATA_SCALE"))
                            .nullable("Y".equals(rs.getString("NULLABLE")))
                            .defaultValue(rs.getString("DATA_DEFAULT"))
                            .columnComment(rs.getString("COLUMN_COMMENT"))
                            .columnId(rs.getInt("COLUMN_ID"))
                            .build();
                    
                    columns.add(column);
                }
            }
        }
        
        return columns;
    }
    
    /**
     * Extracts primary key metadata for the specified table.
     */
    private PrimaryKeyMetadata extractPrimaryKeyMetadata(Connection connection, String schema, String tableName) 
            throws SQLException {
        
        try (PreparedStatement stmt = connection.prepareStatement(PRIMARY_KEY_QUERY)) {
            stmt.setQueryTimeout(databaseConfig.getQueryTimeout());
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null; // No primary key
                }
                
                String constraintName = rs.getString("CONSTRAINT_NAME");
                String indexName = rs.getString("INDEX_NAME");
                boolean enabled = "ENABLED".equals(rs.getString("STATUS"));
                boolean validated = "VALIDATED".equals(rs.getString("VALIDATED"));
                
                List<String> columnNames = new ArrayList<>();
                
                // Process first row
                columnNames.add(rs.getString("COLUMN_NAME"));
                
                // Process remaining rows
                while (rs.next()) {
                    columnNames.add(rs.getString("COLUMN_NAME"));
                }
                
                return PrimaryKeyMetadata.builder()
                        .constraintName(constraintName)
                        .columnNames(columnNames)
                        .indexName(indexName)
                        .enabled(enabled)
                        .validated(validated)
                        .build();
            }
        }
    }
    
    /**
     * Extracts index metadata for the specified table.
     */
    private List<IndexMetadata> extractIndexMetadata(Connection connection, String schema, String tableName) 
            throws SQLException {
        
        Map<String, IndexMetadata.IndexMetadataBuilder> indexBuilders = new LinkedHashMap<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(INDEX_METADATA_QUERY)) {
            stmt.setQueryTimeout(databaseConfig.getQueryTimeout());
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    
                    IndexMetadata.IndexMetadataBuilder builder = indexBuilders.get(indexName);
                    if (builder == null) {
                        builder = IndexMetadata.builder()
                                .indexName(indexName)
                                .indexType(rs.getString("INDEX_TYPE"))
                                .unique("UNIQUE".equals(rs.getString("UNIQUENESS")))
                                .status(rs.getString("STATUS"))
                                .tablespace(rs.getString("TABLESPACE_NAME"))
                                .degree(getIntegerOrNull(rs, "DEGREE"));
                        indexBuilders.put(indexName, builder);
                    }
                    
                    builder.columnName(rs.getString("COLUMN_NAME"));
                }
            }
        }
        
        return indexBuilders.values().stream()
                .map(IndexMetadata.IndexMetadataBuilder::build)
                .collect(Collectors.toList());
    }
    
    /**
     * Helper method to safely extract Integer values from ResultSet, returning null for SQL NULL.
     */
    private Integer getIntegerOrNull(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
}
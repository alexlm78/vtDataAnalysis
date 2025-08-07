package dev.kreaker.vtda.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for validating database metadata objects and their relationships.
 */
public final class MetadataValidator {
    
    private MetadataValidator() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates a complete table metadata object including all relationships.
     * 
     * @param table the table metadata to validate
     * @throws IllegalStateException if validation fails
     */
    public static void validateTableMetadata(TableMetadata table) {
        if (table == null) {
            throw new IllegalStateException("Table metadata cannot be null");
        }
        
        // Validate basic table properties
        table.validate();
        
        // Additional cross-validation
        validateColumnConsistency(table);
        validatePrimaryKeyConsistency(table);
        validateIndexConsistency(table);
    }
    
    /**
     * Validates that all columns in a table have consistent metadata.
     */
    private static void validateColumnConsistency(TableMetadata table) {
        List<ColumnMetadata> columns = table.getColumns();
        
        // Check for gaps in column positions
        for (int i = 0; i < columns.size(); i++) {
            int expectedPosition = i + 1;
            int actualPosition = columns.get(i).getColumnId();
            
            if (actualPosition != expectedPosition) {
                throw new IllegalStateException(
                    String.format("Column position gap in table %s: expected %d, found %d", 
                        table.getFullyQualifiedName(), expectedPosition, actualPosition));
            }
        }
        
        // Validate Oracle naming conventions
        for (ColumnMetadata column : columns) {
            validateOracleIdentifier(column.getColumnName(), "Column name");
        }
    }
    
    /**
     * Validates primary key consistency with table columns.
     */
    private static void validatePrimaryKeyConsistency(TableMetadata table) {
        PrimaryKeyMetadata primaryKey = table.getPrimaryKey();
        if (primaryKey == null) {
            return; // No primary key is valid
        }
        
        primaryKey.validate();
        
        // Ensure all primary key columns exist in the table
        Set<String> tableColumnNames = table.getColumns().stream()
                .map(col -> col.getColumnName().toUpperCase())
                .collect(Collectors.toSet());
        
        for (String pkColumn : primaryKey.getColumnNames()) {
            if (!tableColumnNames.contains(pkColumn.toUpperCase())) {
                throw new IllegalStateException(
                    String.format("Primary key column '%s' not found in table %s", 
                        pkColumn, table.getFullyQualifiedName()));
            }
        }
        
        // Validate that primary key columns are not nullable
        List<ColumnMetadata> pkColumns = table.getPrimaryKeyColumns();
        for (ColumnMetadata pkColumn : pkColumns) {
            if (pkColumn.isNullable()) {
                throw new IllegalStateException(
                    String.format("Primary key column '%s' cannot be nullable in table %s", 
                        pkColumn.getColumnName(), table.getFullyQualifiedName()));
            }
        }
    }
    
    /**
     * Validates index consistency with table columns.
     */
    private static void validateIndexConsistency(TableMetadata table) {
        List<IndexMetadata> indexes = table.getIndexes();
        if (indexes == null || indexes.isEmpty()) {
            return; // No indexes is valid
        }
        
        Set<String> tableColumnNames = table.getColumns().stream()
                .map(col -> col.getColumnName().toUpperCase())
                .collect(Collectors.toSet());
        
        for (IndexMetadata index : indexes) {
            index.validate();
            
            // Ensure all index columns exist in the table
            for (String indexColumn : index.getColumnNames()) {
                if (!tableColumnNames.contains(indexColumn.toUpperCase())) {
                    throw new IllegalStateException(
                        String.format("Index column '%s' not found in table %s for index %s", 
                            indexColumn, table.getFullyQualifiedName(), index.getIndexName()));
                }
            }
            
            // Validate Oracle naming conventions
            validateOracleIdentifier(index.getIndexName(), "Index name");
        }
        
        // Check for duplicate index names
        Set<String> indexNames = indexes.stream()
                .map(idx -> idx.getIndexName().toUpperCase())
                .collect(Collectors.toSet());
        
        if (indexNames.size() != indexes.size()) {
            throw new IllegalStateException(
                String.format("Duplicate index names found in table %s", table.getFullyQualifiedName()));
        }
    }
    
    /**
     * Validates that an identifier follows Oracle naming conventions.
     * 
     * @param identifier the identifier to validate
     * @param type the type of identifier (for error messages)
     * @throws IllegalStateException if the identifier is invalid
     */
    public static void validateOracleIdentifier(String identifier, String type) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalStateException(type + " cannot be null or empty");
        }
        
        String trimmed = identifier.trim();
        
        // Check length (Oracle limit is 128 characters for most identifiers)
        if (trimmed.length() > 128) {
            throw new IllegalStateException(
                String.format("%s '%s' exceeds maximum length of 128 characters", type, trimmed));
        }
        
        // Check for valid Oracle identifier format (simplified check)
        if (!trimmed.matches("^[A-Za-z][A-Za-z0-9_$#]*$")) {
            throw new IllegalStateException(
                String.format("%s '%s' contains invalid characters. Must start with letter and contain only letters, numbers, _, $, #", 
                    type, trimmed));
        }
        
        // Check for Oracle reserved words (basic set)
        if (isOracleReservedWord(trimmed.toUpperCase())) {
            throw new IllegalStateException(
                String.format("%s '%s' is an Oracle reserved word", type, trimmed));
        }
    }
    
    /**
     * Checks if a word is an Oracle reserved word (basic set).
     */
    private static boolean isOracleReservedWord(String word) {
        // This is a simplified set of Oracle reserved words
        Set<String> reservedWords = Set.of(
            "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER",
            "TABLE", "INDEX", "VIEW", "SEQUENCE", "TRIGGER", "PROCEDURE", "FUNCTION", "PACKAGE",
            "AND", "OR", "NOT", "NULL", "TRUE", "FALSE", "DISTINCT", "ORDER", "GROUP", "HAVING",
            "UNION", "INTERSECT", "MINUS", "CONNECT", "START", "WITH", "BY", "AS", "IS", "IN",
            "EXISTS", "BETWEEN", "LIKE", "ESCAPE", "CASE", "WHEN", "THEN", "ELSE", "END",
            "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "UNIQUE", "CHECK", "CONSTRAINT",
            "DEFAULT", "COLUMN", "GRANT", "REVOKE", "COMMIT", "ROLLBACK", "SAVEPOINT"
        );
        
        return reservedWords.contains(word);
    }
    
    /**
     * Validates a list of table metadata objects for consistency.
     * 
     * @param tables the list of tables to validate
     * @throws IllegalStateException if validation fails
     */
    public static void validateTableList(List<TableMetadata> tables) {
        if (tables == null) {
            return; // Null list is acceptable
        }
        
        // Validate each table individually
        for (TableMetadata table : tables) {
            validateTableMetadata(table);
        }
        
        // Check for duplicate table names within the same schema
        Set<String> tableKeys = tables.stream()
                .map(table -> (table.getSchemaName() + "." + table.getTableName()).toUpperCase())
                .collect(Collectors.toSet());
        
        if (tableKeys.size() != tables.size()) {
            throw new IllegalStateException("Duplicate table names found in the table list");
        }
    }
}
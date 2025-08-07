package dev.kreaker.vtda.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents comprehensive metadata for a database table including all columns,
 * constraints, and Oracle-specific properties.
 */
@Data
@Builder
public class TableMetadata {
    
    /**
     * The schema (owner) of the table
     */
    @NonNull
    private final String schemaName;
    
    /**
     * The name of the table
     */
    @NonNull
    private final String tableName;
    
    /**
     * Comment or description for the table
     */
    private final String tableComment;
    
    /**
     * List of all columns in the table, ordered by column position
     */
    @NonNull
    @Singular
    private final List<ColumnMetadata> columns;
    
    /**
     * Primary key metadata for the table, null if no primary key exists
     */
    private final PrimaryKeyMetadata primaryKey;
    
    /**
     * List of indexes defined on the table
     */
    @Singular
    private final List<IndexMetadata> indexes;
    
    /**
     * Timestamp when the table was created
     */
    private final Instant createdDate;
    
    /**
     * Timestamp when the table was last modified
     */
    private final Instant lastModified;
    
    /**
     * Gets the fully qualified table name (schema.table).
     * 
     * @return formatted table name
     */
    public String getFullyQualifiedName() {
        return schemaName + "." + tableName;
    }
    
    /**
     * Gets the total number of columns in the table.
     * 
     * @return column count
     */
    public int getColumnCount() {
        return columns != null ? columns.size() : 0;
    }
    
    /**
     * Finds a column by name (case-insensitive).
     * 
     * @param columnName the name of the column to find
     * @return Optional containing the column if found, empty otherwise
     */
    public Optional<ColumnMetadata> findColumn(String columnName) {
        if (columnName == null || columns == null) {
            return Optional.empty();
        }
        
        return columns.stream()
                .filter(col -> col.getColumnName().equalsIgnoreCase(columnName))
                .findFirst();
    }
    
    /**
     * Gets all columns that are part of the primary key.
     * 
     * @return list of primary key columns, empty if no primary key exists
     */
    public List<ColumnMetadata> getPrimaryKeyColumns() {
        if (primaryKey == null || primaryKey.getColumnNames() == null) {
            return List.of();
        }
        
        return primaryKey.getColumnNames().stream()
                .map(this::findColumn)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all nullable columns in the table.
     * 
     * @return list of columns that allow NULL values
     */
    public List<ColumnMetadata> getNullableColumns() {
        if (columns == null) {
            return List.of();
        }
        
        return columns.stream()
                .filter(ColumnMetadata::isNullable)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all non-nullable columns in the table.
     * 
     * @return list of columns that do not allow NULL values
     */
    public List<ColumnMetadata> getNonNullableColumns() {
        if (columns == null) {
            return List.of();
        }
        
        return columns.stream()
                .filter(col -> !col.isNullable())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all columns of a specific data type category.
     * 
     * @param typeCategory the category to filter by (character, numeric, temporal, lob)
     * @return list of columns matching the type category
     */
    public List<ColumnMetadata> getColumnsByType(ColumnTypeCategory typeCategory) {
        if (columns == null) {
            return List.of();
        }
        
        return columns.stream()
                .filter(col -> matchesTypeCategory(col, typeCategory))
                .collect(Collectors.toList());
    }
    
    private boolean matchesTypeCategory(ColumnMetadata column, ColumnTypeCategory category) {
        return switch (category) {
            case CHARACTER -> column.isCharacterType();
            case NUMERIC -> column.isNumericType();
            case TEMPORAL -> column.isTemporalType();
            case LOB -> column.isLobType();
        };
    }
    
    /**
     * Determines if the table has a primary key defined.
     * 
     * @return true if the table has a primary key
     */
    public boolean hasPrimaryKey() {
        return primaryKey != null && primaryKey.getColumnNames() != null && !primaryKey.getColumnNames().isEmpty();
    }
    
    /**
     * Determines if the table has any indexes defined.
     * 
     * @return true if the table has one or more indexes
     */
    public boolean hasIndexes() {
        return indexes != null && !indexes.isEmpty();
    }
    
    /**
     * Gets a summary of the table structure.
     * 
     * @return formatted summary string
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Table: %s (%d columns)", getFullyQualifiedName(), getColumnCount()));
        
        if (hasPrimaryKey()) {
            summary.append(String.format(", PK: %s", String.join(", ", primaryKey.getColumnNames())));
        }
        
        if (hasIndexes()) {
            summary.append(String.format(", %d indexes", indexes.size()));
        }
        
        if (tableComment != null && !tableComment.trim().isEmpty()) {
            summary.append(String.format(", Comment: %s", tableComment));
        }
        
        return summary.toString();
    }
    
    /**
     * Validates the table metadata for consistency and completeness.
     * 
     * @throws IllegalStateException if the metadata is invalid
     */
    public void validate() {
        if (schemaName == null || schemaName.trim().isEmpty()) {
            throw new IllegalStateException("Schema name cannot be null or empty");
        }
        
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalStateException("Table name cannot be null or empty");
        }
        
        if (columns == null || columns.isEmpty()) {
            throw new IllegalStateException("Table must have at least one column: " + getFullyQualifiedName());
        }
        
        // Validate all columns
        for (ColumnMetadata column : columns) {
            try {
                column.validate();
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Invalid column in table " + getFullyQualifiedName() + ": " + e.getMessage(), e);
            }
        }
        
        // Validate primary key references existing columns
        if (primaryKey != null && primaryKey.getColumnNames() != null) {
            for (String pkColumn : primaryKey.getColumnNames()) {
                if (findColumn(pkColumn).isEmpty()) {
                    throw new IllegalStateException("Primary key references non-existent column '" + pkColumn + "' in table " + getFullyQualifiedName());
                }
            }
        }
        
        // Check for duplicate column names
        long uniqueColumnNames = columns.stream()
                .map(col -> col.getColumnName().toUpperCase())
                .distinct()
                .count();
        
        if (uniqueColumnNames != columns.size()) {
            throw new IllegalStateException("Table contains duplicate column names: " + getFullyQualifiedName());
        }
        
        // Validate column positions are sequential
        for (int i = 0; i < columns.size(); i++) {
            int expectedPosition = i + 1;
            int actualPosition = columns.get(i).getColumnId();
            if (actualPosition != expectedPosition) {
                throw new IllegalStateException(String.format("Column position mismatch in table %s: expected %d, got %d for column %s", 
                    getFullyQualifiedName(), expectedPosition, actualPosition, columns.get(i).getColumnName()));
            }
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableMetadata that = (TableMetadata) o;
        return Objects.equals(schemaName, that.schemaName) &&
               Objects.equals(tableName, that.tableName) &&
               Objects.equals(columns, that.columns);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(schemaName, tableName, columns);
    }
    
    @Override
    public String toString() {
        return String.format("TableMetadata{schema='%s', table='%s', columns=%d, hasPK=%s}", 
                           schemaName, tableName, getColumnCount(), hasPrimaryKey());
    }
    
    /**
     * Enumeration of column type categories for filtering and analysis.
     */
    public enum ColumnTypeCategory {
        CHARACTER,
        NUMERIC,
        TEMPORAL,
        LOB
    }
}
package dev.kreaker.vtda.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Objects;

/**
 * Represents metadata for a primary key constraint on a database table.
 */
@Data
@Builder
public class PrimaryKeyMetadata {
    
    /**
     * The name of the primary key constraint
     */
    @NonNull
    private final String constraintName;
    
    /**
     * List of column names that make up the primary key, in order
     */
    @NonNull
    @Singular
    private final List<String> columnNames;
    
    /**
     * The name of the index backing this primary key (Oracle-specific)
     */
    private final String indexName;
    
    /**
     * Whether the primary key constraint is enabled
     */
    private final boolean enabled;
    
    /**
     * Whether the primary key constraint is validated
     */
    private final boolean validated;
    
    /**
     * Determines if this is a composite primary key (multiple columns).
     * 
     * @return true if the primary key consists of more than one column
     */
    public boolean isComposite() {
        return columnNames != null && columnNames.size() > 1;
    }
    
    /**
     * Gets the number of columns in the primary key.
     * 
     * @return column count
     */
    public int getColumnCount() {
        return columnNames != null ? columnNames.size() : 0;
    }
    
    /**
     * Gets a formatted string representation of the primary key columns.
     * 
     * @return comma-separated list of column names
     */
    public String getColumnNamesAsString() {
        return columnNames != null ? String.join(", ", columnNames) : "";
    }
    
    /**
     * Validates the primary key metadata for consistency and completeness.
     * 
     * @throws IllegalStateException if the metadata is invalid
     */
    public void validate() {
        if (constraintName == null || constraintName.trim().isEmpty()) {
            throw new IllegalStateException("Primary key constraint name cannot be null or empty");
        }
        
        if (columnNames == null || columnNames.isEmpty()) {
            throw new IllegalStateException("Primary key must have at least one column");
        }
        
        // Check for null or empty column names
        for (String columnName : columnNames) {
            if (columnName == null || columnName.trim().isEmpty()) {
                throw new IllegalStateException("Primary key column name cannot be null or empty");
            }
        }
        
        // Check for duplicate column names
        long uniqueColumns = columnNames.stream()
                .map(String::toUpperCase)
                .distinct()
                .count();
        
        if (uniqueColumns != columnNames.size()) {
            throw new IllegalStateException("Primary key contains duplicate column names");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimaryKeyMetadata that = (PrimaryKeyMetadata) o;
        return enabled == that.enabled &&
               validated == that.validated &&
               Objects.equals(constraintName, that.constraintName) &&
               Objects.equals(columnNames, that.columnNames) &&
               Objects.equals(indexName, that.indexName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(constraintName, columnNames, indexName, enabled, validated);
    }
    
    @Override
    public String toString() {
        return String.format("PrimaryKeyMetadata{constraint='%s', columns=[%s], enabled=%s}", 
                           constraintName, getColumnNamesAsString(), enabled);
    }
}
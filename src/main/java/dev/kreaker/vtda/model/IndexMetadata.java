package dev.kreaker.vtda.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Objects;

/**
 * Represents metadata for a database index including all Oracle-specific properties.
 */
@Data
@Builder
public class IndexMetadata {
    
    /**
     * The name of the index
     */
    @NonNull
    private final String indexName;
    
    /**
     * The type of index (NORMAL, BITMAP, FUNCTION-BASED, etc.)
     */
    @NonNull
    private final String indexType;
    
    /**
     * List of column names that make up the index, in order
     */
    @NonNull
    @Singular
    private final List<String> columnNames;
    
    /**
     * Whether the index enforces uniqueness
     */
    private final boolean unique;
    
    /**
     * The status of the index (VALID, UNUSABLE, etc.)
     */
    private final String status;
    
    /**
     * The tablespace where the index is stored
     */
    private final String tablespace;
    
    /**
     * Whether this index was created to support a primary key constraint
     */
    private final boolean primaryKeyIndex;
    
    /**
     * The degree of parallelism for index operations
     */
    private final Integer degree;
    
    /**
     * Determines if this is a composite index (multiple columns).
     * 
     * @return true if the index consists of more than one column
     */
    public boolean isComposite() {
        return columnNames != null && columnNames.size() > 1;
    }
    
    /**
     * Gets the number of columns in the index.
     * 
     * @return column count
     */
    public int getColumnCount() {
        return columnNames != null ? columnNames.size() : 0;
    }
    
    /**
     * Gets a formatted string representation of the index columns.
     * 
     * @return comma-separated list of column names
     */
    public String getColumnNamesAsString() {
        return columnNames != null ? String.join(", ", columnNames) : "";
    }
    
    /**
     * Determines if the index is currently usable.
     * 
     * @return true if the index status is VALID
     */
    public boolean isUsable() {
        return "VALID".equalsIgnoreCase(status);
    }
    
    /**
     * Determines if this is a bitmap index.
     * 
     * @return true if the index type is BITMAP
     */
    public boolean isBitmapIndex() {
        return "BITMAP".equalsIgnoreCase(indexType);
    }
    
    /**
     * Determines if this is a function-based index.
     * 
     * @return true if the index type contains FUNCTION-BASED
     */
    public boolean isFunctionBasedIndex() {
        return indexType != null && indexType.toUpperCase().contains("FUNCTION-BASED");
    }
    
    /**
     * Gets a summary description of the index.
     * 
     * @return formatted summary string
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Index: %s (%s)", indexName, indexType));
        
        if (unique) {
            summary.append(" [UNIQUE]");
        }
        
        if (primaryKeyIndex) {
            summary.append(" [PK]");
        }
        
        summary.append(String.format(" on (%s)", getColumnNamesAsString()));
        
        if (status != null && !"VALID".equalsIgnoreCase(status)) {
            summary.append(String.format(" [%s]", status));
        }
        
        return summary.toString();
    }
    
    /**
     * Validates the index metadata for consistency and completeness.
     * 
     * @throws IllegalStateException if the metadata is invalid
     */
    public void validate() {
        if (indexName == null || indexName.trim().isEmpty()) {
            throw new IllegalStateException("Index name cannot be null or empty");
        }
        
        if (indexType == null || indexType.trim().isEmpty()) {
            throw new IllegalStateException("Index type cannot be null or empty for index: " + indexName);
        }
        
        if (columnNames == null || columnNames.isEmpty()) {
            throw new IllegalStateException("Index must have at least one column: " + indexName);
        }
        
        // Check for null or empty column names
        for (String columnName : columnNames) {
            if (columnName == null || columnName.trim().isEmpty()) {
                throw new IllegalStateException("Index column name cannot be null or empty for index: " + indexName);
            }
        }
        
        // Check for duplicate column names
        long uniqueColumns = columnNames.stream()
                .map(String::toUpperCase)
                .distinct()
                .count();
        
        if (uniqueColumns != columnNames.size()) {
            throw new IllegalStateException("Index contains duplicate column names: " + indexName);
        }
        
        // Validate degree if specified
        if (degree != null && degree < 1) {
            throw new IllegalStateException("Index degree must be positive for index: " + indexName);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexMetadata that = (IndexMetadata) o;
        return unique == that.unique &&
               primaryKeyIndex == that.primaryKeyIndex &&
               Objects.equals(indexName, that.indexName) &&
               Objects.equals(indexType, that.indexType) &&
               Objects.equals(columnNames, that.columnNames) &&
               Objects.equals(status, that.status);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(indexName, indexType, columnNames, unique, status, primaryKeyIndex);
    }
    
    @Override
    public String toString() {
        return String.format("IndexMetadata{name='%s', type='%s', columns=[%s], unique=%s}", 
                           indexName, indexType, getColumnNamesAsString(), unique);
    }
}
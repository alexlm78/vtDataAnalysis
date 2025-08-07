package dev.kreaker.vtda.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Objects;

/**
 * Represents metadata for a database column including all Oracle-specific properties.
 * This class encapsulates column information extracted from Oracle system views.
 */
@Data
@Builder
public class ColumnMetadata {
    
    /**
     * The name of the column
     */
    @NonNull
    private final String columnName;
    
    /**
     * The Oracle data type (e.g., VARCHAR2, NUMBER, TIMESTAMP)
     */
    @NonNull
    private final String dataType;
    
    /**
     * The maximum length for character data types, null for numeric/date types
     */
    private final Integer dataLength;
    
    /**
     * The precision for numeric data types (total number of digits)
     */
    private final Integer dataPrecision;
    
    /**
     * The scale for numeric data types (number of digits after decimal point)
     */
    private final Integer dataScale;
    
    /**
     * Whether the column allows NULL values
     */
    private final boolean nullable;
    
    /**
     * The default value for the column, null if no default is specified
     */
    private final String defaultValue;
    
    /**
     * Comment or description for the column
     */
    private final String columnComment;
    
    /**
     * The position of the column in the table (1-based)
     */
    private final int columnId;
    
    /**
     * Gets the full data type specification including length, precision, and scale.
     * 
     * @return formatted data type string (e.g., "VARCHAR2(100)", "NUMBER(10,2)")
     */
    public String getFullDataType() {
        StringBuilder fullType = new StringBuilder(dataType);
        
        if (dataLength != null && dataLength > 0) {
            // Character types use length
            fullType.append("(").append(dataLength).append(")");
        } else if (dataPrecision != null && dataPrecision > 0) {
            // Numeric types use precision and scale
            fullType.append("(").append(dataPrecision);
            if (dataScale != null && dataScale > 0) {
                fullType.append(",").append(dataScale);
            }
            fullType.append(")");
        }
        
        return fullType.toString();
    }
    
    /**
     * Determines if this column is a character-based data type.
     * 
     * @return true if the column is VARCHAR2, NVARCHAR2, CHAR, NCHAR, CLOB, or NCLOB
     */
    public boolean isCharacterType() {
        return dataType != null && (
            dataType.startsWith("VARCHAR") ||
            dataType.startsWith("NVARCHAR") ||
            dataType.startsWith("CHAR") ||
            dataType.startsWith("NCHAR") ||
            dataType.equals("CLOB") ||
            dataType.equals("NCLOB")
        );
    }
    
    /**
     * Determines if this column is a numeric data type.
     * 
     * @return true if the column is NUMBER, INTEGER, FLOAT, or similar numeric type
     */
    public boolean isNumericType() {
        return dataType != null && (
            dataType.equals("NUMBER") ||
            dataType.equals("INTEGER") ||
            dataType.equals("FLOAT") ||
            dataType.equals("BINARY_FLOAT") ||
            dataType.equals("BINARY_DOUBLE")
        );
    }
    
    /**
     * Determines if this column is a date/time data type.
     * 
     * @return true if the column is DATE, TIMESTAMP, or similar temporal type
     */
    public boolean isTemporalType() {
        return dataType != null && (
            dataType.equals("DATE") ||
            dataType.startsWith("TIMESTAMP") ||
            dataType.startsWith("INTERVAL")
        );
    }
    
    /**
     * Determines if this column is a large object (LOB) type.
     * 
     * @return true if the column is BLOB, CLOB, NCLOB, or BFILE
     */
    public boolean isLobType() {
        return dataType != null && (
            dataType.equals("BLOB") ||
            dataType.equals("CLOB") ||
            dataType.equals("NCLOB") ||
            dataType.equals("BFILE")
        );
    }
    
    /**
     * Validates the column metadata for consistency and completeness.
     * 
     * @throws IllegalStateException if the metadata is invalid
     */
    public void validate() {
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalStateException("Column name cannot be null or empty");
        }
        
        if (dataType == null || dataType.trim().isEmpty()) {
            throw new IllegalStateException("Data type cannot be null or empty for column: " + columnName);
        }
        
        if (columnId < 1) {
            throw new IllegalStateException("Column ID must be positive for column: " + columnName);
        }
        
        // Validate numeric constraints
        if (isNumericType() && dataPrecision != null && dataPrecision <= 0) {
            throw new IllegalStateException("Numeric precision must be positive for column: " + columnName);
        }
        
        if (dataScale != null && dataScale < 0) {
            throw new IllegalStateException("Data scale cannot be negative for column: " + columnName);
        }
        
        if (dataPrecision != null && dataScale != null && dataScale > dataPrecision) {
            throw new IllegalStateException("Data scale cannot exceed precision for column: " + columnName);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnMetadata that = (ColumnMetadata) o;
        return columnId == that.columnId &&
               nullable == that.nullable &&
               Objects.equals(columnName, that.columnName) &&
               Objects.equals(dataType, that.dataType) &&
               Objects.equals(dataLength, that.dataLength) &&
               Objects.equals(dataPrecision, that.dataPrecision) &&
               Objects.equals(dataScale, that.dataScale);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(columnName, dataType, dataLength, dataPrecision, dataScale, nullable, columnId);
    }
    
    @Override
    public String toString() {
        return String.format("ColumnMetadata{name='%s', type='%s', nullable=%s, position=%d}", 
                           columnName, getFullDataType(), nullable, columnId);
    }
}
package dev.kreaker.vtda.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for creating TableMetadata objects with validation and convenience methods.
 * This builder provides additional validation and helper methods beyond the Lombok-generated builder.
 */
public class TableMetadataBuilder {
    
    private String schemaName;
    private String tableName;
    private String tableComment;
    private List<ColumnMetadata> columns = new ArrayList<>();
    private PrimaryKeyMetadata primaryKey;
    private List<IndexMetadata> indexes = new ArrayList<>();
    private Instant createdDate;
    private Instant lastModified;
    
    private TableMetadataBuilder() {
        // Private constructor - use static factory method
    }
    
    /**
     * Creates a new builder instance.
     * 
     * @return new builder instance
     */
    public static TableMetadataBuilder create() {
        return new TableMetadataBuilder();
    }
    
    /**
     * Creates a new builder instance with schema and table name.
     * 
     * @param schemaName the schema name
     * @param tableName the table name
     * @return new builder instance
     */
    public static TableMetadataBuilder create(String schemaName, String tableName) {
        return new TableMetadataBuilder()
                .withSchema(schemaName)
                .withTable(tableName);
    }
    
    /**
     * Sets the schema name.
     * 
     * @param schemaName the schema name
     * @return this builder
     */
    public TableMetadataBuilder withSchema(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }
    
    /**
     * Sets the table name.
     * 
     * @param tableName the table name
     * @return this builder
     */
    public TableMetadataBuilder withTable(String tableName) {
        this.tableName = tableName;
        return this;
    }
    
    /**
     * Sets the table comment.
     * 
     * @param tableComment the table comment
     * @return this builder
     */
    public TableMetadataBuilder withComment(String tableComment) {
        this.tableComment = tableComment;
        return this;
    }
    
    /**
     * Adds a column to the table.
     * 
     * @param column the column to add
     * @return this builder
     */
    public TableMetadataBuilder addColumn(ColumnMetadata column) {
        if (column != null) {
            this.columns.add(column);
        }
        return this;
    }
    
    /**
     * Adds multiple columns to the table.
     * 
     * @param columns the columns to add
     * @return this builder
     */
    public TableMetadataBuilder addColumns(List<ColumnMetadata> columns) {
        if (columns != null) {
            this.columns.addAll(columns);
        }
        return this;
    }
    
    /**
     * Adds a column with basic properties.
     * 
     * @param name the column name
     * @param dataType the data type
     * @param nullable whether the column is nullable
     * @param position the column position
     * @return this builder
     */
    public TableMetadataBuilder addColumn(String name, String dataType, boolean nullable, int position) {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName(name)
                .dataType(dataType)
                .nullable(nullable)
                .columnId(position)
                .build();
        return addColumn(column);
    }
    
    /**
     * Adds a VARCHAR2 column.
     * 
     * @param name the column name
     * @param length the column length
     * @param nullable whether the column is nullable
     * @param position the column position
     * @return this builder
     */
    public TableMetadataBuilder addVarchar2Column(String name, int length, boolean nullable, int position) {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName(name)
                .dataType("VARCHAR2")
                .dataLength(length)
                .nullable(nullable)
                .columnId(position)
                .build();
        return addColumn(column);
    }
    
    /**
     * Adds a NUMBER column.
     * 
     * @param name the column name
     * @param precision the precision
     * @param scale the scale
     * @param nullable whether the column is nullable
     * @param position the column position
     * @return this builder
     */
    public TableMetadataBuilder addNumberColumn(String name, int precision, int scale, boolean nullable, int position) {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName(name)
                .dataType("NUMBER")
                .dataPrecision(precision)
                .dataScale(scale)
                .nullable(nullable)
                .columnId(position)
                .build();
        return addColumn(column);
    }
    
    /**
     * Adds a DATE column.
     * 
     * @param name the column name
     * @param nullable whether the column is nullable
     * @param position the column position
     * @return this builder
     */
    public TableMetadataBuilder addDateColumn(String name, boolean nullable, int position) {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName(name)
                .dataType("DATE")
                .nullable(nullable)
                .columnId(position)
                .build();
        return addColumn(column);
    }
    
    /**
     * Sets the primary key for the table.
     * 
     * @param primaryKey the primary key metadata
     * @return this builder
     */
    public TableMetadataBuilder withPrimaryKey(PrimaryKeyMetadata primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }
    
    /**
     * Sets a single-column primary key.
     * 
     * @param constraintName the constraint name
     * @param columnName the column name
     * @return this builder
     */
    public TableMetadataBuilder withPrimaryKey(String constraintName, String columnName) {
        this.primaryKey = PrimaryKeyMetadata.builder()
                .constraintName(constraintName)
                .columnName(columnName)
                .enabled(true)
                .validated(true)
                .build();
        return this;
    }
    
    /**
     * Sets a composite primary key.
     * 
     * @param constraintName the constraint name
     * @param columnNames the column names
     * @return this builder
     */
    public TableMetadataBuilder withPrimaryKey(String constraintName, List<String> columnNames) {
        PrimaryKeyMetadata.PrimaryKeyMetadataBuilder pkBuilder = PrimaryKeyMetadata.builder()
                .constraintName(constraintName)
                .enabled(true)
                .validated(true);
        
        if (columnNames != null) {
            columnNames.forEach(pkBuilder::columnName);
        }
        
        this.primaryKey = pkBuilder.build();
        return this;
    }
    
    /**
     * Adds an index to the table.
     * 
     * @param index the index to add
     * @return this builder
     */
    public TableMetadataBuilder addIndex(IndexMetadata index) {
        if (index != null) {
            this.indexes.add(index);
        }
        return this;
    }
    
    /**
     * Adds a simple index.
     * 
     * @param indexName the index name
     * @param columnName the column name
     * @param unique whether the index is unique
     * @return this builder
     */
    public TableMetadataBuilder addIndex(String indexName, String columnName, boolean unique) {
        IndexMetadata index = IndexMetadata.builder()
                .indexName(indexName)
                .indexType("NORMAL")
                .columnName(columnName)
                .unique(unique)
                .status("VALID")
                .build();
        return addIndex(index);
    }
    
    /**
     * Sets the created date.
     * 
     * @param createdDate the created date
     * @return this builder
     */
    public TableMetadataBuilder withCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }
    
    /**
     * Sets the last modified date.
     * 
     * @param lastModified the last modified date
     * @return this builder
     */
    public TableMetadataBuilder withLastModified(Instant lastModified) {
        this.lastModified = lastModified;
        return this;
    }
    
    /**
     * Builds the TableMetadata object with validation.
     * 
     * @return the built TableMetadata object
     * @throws IllegalStateException if the metadata is invalid
     */
    public TableMetadata build() {
        TableMetadata table = TableMetadata.builder()
                .schemaName(schemaName)
                .tableName(tableName)
                .tableComment(tableComment)
                .columns(columns)
                .primaryKey(primaryKey)
                .indexes(indexes)
                .createdDate(createdDate)
                .lastModified(lastModified)
                .build();
        
        // Validate the built object
        MetadataValidator.validateTableMetadata(table);
        
        return table;
    }
    
    /**
     * Builds the TableMetadata object without validation.
     * Use this method only when you need to create invalid objects for testing.
     * 
     * @return the built TableMetadata object
     */
    public TableMetadata buildWithoutValidation() {
        return TableMetadata.builder()
                .schemaName(schemaName)
                .tableName(tableName)
                .tableComment(tableComment)
                .columns(columns)
                .primaryKey(primaryKey)
                .indexes(indexes)
                .createdDate(createdDate)
                .lastModified(lastModified)
                .build();
    }
}
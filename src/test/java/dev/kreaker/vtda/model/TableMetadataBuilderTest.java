package dev.kreaker.vtda.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TableMetadataBuilder Tests")
class TableMetadataBuilderTest {
    
    @Test
    @DisplayName("Should build table with fluent API")
    void shouldBuildTableWithFluentApi() {
        Instant now = Instant.now();
        
        TableMetadata table = TableMetadataBuilder.create("TEST_SCHEMA", "TEST_TABLE")
                .withComment("Test table created with builder")
                .addVarchar2Column("NAME", 100, false, 1)  // Primary key columns cannot be nullable
                .addNumberColumn("PRICE", 10, 2, false, 2)
                .addDateColumn("CREATED_DATE", false, 3)
                .withPrimaryKey("PK_TEST_TABLE", "NAME")
                .addIndex("IDX_TEST_PRICE", "PRICE", false)
                .withCreatedDate(now)
                .withLastModified(now)
                .build();
        
        assertThat(table.getSchemaName()).isEqualTo("TEST_SCHEMA");
        assertThat(table.getTableName()).isEqualTo("TEST_TABLE");
        assertThat(table.getTableComment()).isEqualTo("Test table created with builder");
        assertThat(table.getColumnCount()).isEqualTo(3);
        assertThat(table.hasPrimaryKey()).isTrue();
        assertThat(table.hasIndexes()).isTrue();
        assertThat(table.getCreatedDate()).isEqualTo(now);
        assertThat(table.getLastModified()).isEqualTo(now);
        
        // Verify columns
        assertThat(table.findColumn("NAME")).isPresent();
        assertThat(table.findColumn("PRICE")).isPresent();
        assertThat(table.findColumn("CREATED_DATE")).isPresent();
        
        // Verify column properties
        ColumnMetadata nameColumn = table.findColumn("NAME").get();
        assertThat(nameColumn.getDataType()).isEqualTo("VARCHAR2");
        assertThat(nameColumn.getDataLength()).isEqualTo(100);
        assertThat(nameColumn.isNullable()).isFalse();  // Primary key columns cannot be nullable
        
        ColumnMetadata priceColumn = table.findColumn("PRICE").get();
        assertThat(priceColumn.getDataType()).isEqualTo("NUMBER");
        assertThat(priceColumn.getDataPrecision()).isEqualTo(10);
        assertThat(priceColumn.getDataScale()).isEqualTo(2);
        assertThat(priceColumn.isNullable()).isFalse();
        
        ColumnMetadata dateColumn = table.findColumn("CREATED_DATE").get();
        assertThat(dateColumn.getDataType()).isEqualTo("DATE");
        assertThat(dateColumn.isNullable()).isFalse();
        
        // Verify primary key
        assertThat(table.getPrimaryKey().getConstraintName()).isEqualTo("PK_TEST_TABLE");
        assertThat(table.getPrimaryKey().getColumnNames()).containsExactly("NAME");
        
        // Verify index
        assertThat(table.getIndexes()).hasSize(1);
        IndexMetadata index = table.getIndexes().get(0);
        assertThat(index.getIndexName()).isEqualTo("IDX_TEST_PRICE");
        assertThat(index.getColumnNames()).containsExactly("PRICE");
        assertThat(index.isUnique()).isFalse();
    }
    
    @Test
    @DisplayName("Should build table with composite primary key")
    void shouldBuildTableWithCompositePrimaryKey() {
        TableMetadata table = TableMetadataBuilder.create("TEST_SCHEMA", "TEST_TABLE")
                .addVarchar2Column("CODE", 10, false, 1)
                .addVarchar2Column("TYPE", 5, false, 2)
                .addVarchar2Column("NAME", 100, true, 3)
                .withPrimaryKey("PK_TEST_TABLE", List.of("CODE", "TYPE"))
                .build();
        
        assertThat(table.hasPrimaryKey()).isTrue();
        assertThat(table.getPrimaryKey().isComposite()).isTrue();
        assertThat(table.getPrimaryKey().getColumnNames()).containsExactly("CODE", "TYPE");
        
        List<ColumnMetadata> pkColumns = table.getPrimaryKeyColumns();
        assertThat(pkColumns).hasSize(2);
        assertThat(pkColumns.get(0).getColumnName()).isEqualTo("CODE");
        assertThat(pkColumns.get(1).getColumnName()).isEqualTo("TYPE");
    }
    
    @Test
    @DisplayName("Should build table with multiple columns and indexes")
    void shouldBuildTableWithMultipleColumnsAndIndexes() {
        ColumnMetadata customColumn = ColumnMetadata.builder()
                .columnName("CUSTOM_FIELD")
                .dataType("CLOB")
                .nullable(true)
                .columnId(4)
                .build();
        
        IndexMetadata customIndex = IndexMetadata.builder()
                .indexName("IDX_CUSTOM")
                .indexType("BITMAP")
                .columnName("CUSTOM_FIELD")
                .unique(false)
                .status("VALID")
                .build();
        
        TableMetadata table = TableMetadataBuilder.create()
                .withSchema("TEST_SCHEMA")
                .withTable("COMPLEX_TABLE")
                .addVarchar2Column("ID", 20, false, 1)
                .addNumberColumn("AMOUNT", 15, 4, true, 2)
                .addDateColumn("PROCESS_DATE", true, 3)
                .addColumn(customColumn)
                .withPrimaryKey("PK_COMPLEX", "ID")
                .addIndex("IDX_AMOUNT", "AMOUNT", false)
                .addIndex(customIndex)
                .build();
        
        assertThat(table.getColumnCount()).isEqualTo(4);
        assertThat(table.hasIndexes()).isTrue();
        assertThat(table.getIndexes()).hasSize(2);
        
        // Verify custom column was added
        assertThat(table.findColumn("CUSTOM_FIELD")).isPresent();
        ColumnMetadata foundCustomColumn = table.findColumn("CUSTOM_FIELD").get();
        assertThat(foundCustomColumn.getDataType()).isEqualTo("CLOB");
        assertThat(foundCustomColumn.isLobType()).isTrue();
        
        // Verify custom index was added
        IndexMetadata foundCustomIndex = table.getIndexes().stream()
                .filter(idx -> idx.getIndexName().equals("IDX_CUSTOM"))
                .findFirst()
                .orElse(null);
        assertThat(foundCustomIndex).isNotNull();
        assertThat(foundCustomIndex.getIndexType()).isEqualTo("BITMAP");
        assertThat(foundCustomIndex.isBitmapIndex()).isTrue();
    }
    
    @Test
    @DisplayName("Should validate built table automatically")
    void shouldValidateBuiltTableAutomatically() {
        // This should fail validation because primary key references non-existent column
        assertThatThrownBy(() -> 
            TableMetadataBuilder.create("TEST_SCHEMA", "TEST_TABLE")
                    .addVarchar2Column("NAME", 100, true, 1)
                    .withPrimaryKey("PK_TEST", "NON_EXISTENT_COLUMN")
                    .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Primary key references non-existent column");
    }
    
    @Test
    @DisplayName("Should allow building without validation for testing")
    void shouldAllowBuildingWithoutValidationForTesting() {
        // This would normally fail validation, but buildWithoutValidation allows it
        TableMetadata table = TableMetadataBuilder.create("TEST_SCHEMA", "TEST_TABLE")
                .addVarchar2Column("NAME", 100, true, 1)
                .withPrimaryKey("PK_TEST", "NON_EXISTENT_COLUMN")
                .buildWithoutValidation();
        
        assertThat(table).isNotNull();
        assertThat(table.getTableName()).isEqualTo("TEST_TABLE");
        
        // But manual validation should still fail
        assertThatThrownBy(table::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Primary key references non-existent column");
    }
    
    @Test
    @DisplayName("Should handle empty builder gracefully")
    void shouldHandleEmptyBuilderGracefully() {
        assertThatThrownBy(() -> TableMetadataBuilder.create().build())
                .isInstanceOf(NullPointerException.class);  // Lombok @NonNull throws NPE for null required fields
    }
    
    @Test
    @DisplayName("Should add multiple columns at once")
    void shouldAddMultipleColumnsAtOnce() {
        List<ColumnMetadata> columns = List.of(
                ColumnMetadata.builder()
                        .columnName("COL1")
                        .dataType("VARCHAR2")
                        .dataLength(50)
                        .nullable(true)
                        .columnId(1)
                        .build(),
                ColumnMetadata.builder()
                        .columnName("COL2")
                        .dataType("NUMBER")
                        .dataPrecision(10)
                        .nullable(false)
                        .columnId(2)
                        .build()
        );
        
        TableMetadata table = TableMetadataBuilder.create("TEST", "TABLE")
                .addColumns(columns)
                .build();
        
        assertThat(table.getColumnCount()).isEqualTo(2);
        assertThat(table.findColumn("COL1")).isPresent();
        assertThat(table.findColumn("COL2")).isPresent();
    }
}
package dev.kreaker.vtda.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TableMetadata Tests")
class TableMetadataTest {
    
    @Test
    @DisplayName("Should create valid table with columns")
    void shouldCreateValidTableWithColumns() {
        ColumnMetadata idColumn = ColumnMetadata.builder()
                .columnName("ID")
                .dataType("NUMBER")
                .dataPrecision(10)
                .nullable(false)
                .columnId(1)
                .build();
        
        ColumnMetadata nameColumn = ColumnMetadata.builder()
                .columnName("NAME")
                .dataType("VARCHAR2")
                .dataLength(100)
                .nullable(true)
                .columnId(2)
                .build();
        
        PrimaryKeyMetadata primaryKey = PrimaryKeyMetadata.builder()
                .constraintName("PK_TEST_TABLE")
                .columnName("ID")
                .enabled(true)
                .validated(true)
                .build();
        
        TableMetadata table = TableMetadata.builder()
                .schemaName("TEST_SCHEMA")
                .tableName("TEST_TABLE")
                .tableComment("Test table for unit tests")
                .column(idColumn)
                .column(nameColumn)
                .primaryKey(primaryKey)
                .createdDate(Instant.now())
                .build();
        
        assertThat(table.getSchemaName()).isEqualTo("TEST_SCHEMA");
        assertThat(table.getTableName()).isEqualTo("TEST_TABLE");
        assertThat(table.getFullyQualifiedName()).isEqualTo("TEST_SCHEMA.TEST_TABLE");
        assertThat(table.getColumnCount()).isEqualTo(2);
        assertThat(table.hasPrimaryKey()).isTrue();
        assertThat(table.getTableComment()).isEqualTo("Test table for unit tests");
    }
    
    @Test
    @DisplayName("Should find column by name")
    void shouldFindColumnByName() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("TEST_COLUMN")
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(1)
                .build();
        
        TableMetadata table = TableMetadata.builder()
                .schemaName("TEST")
                .tableName("TABLE")
                .column(column)
                .build();
        
        Optional<ColumnMetadata> found = table.findColumn("TEST_COLUMN");
        assertThat(found).isPresent();
        assertThat(found.get().getColumnName()).isEqualTo("TEST_COLUMN");
        
        Optional<ColumnMetadata> notFound = table.findColumn("NON_EXISTENT");
        assertThat(notFound).isEmpty();
    }
    
    @Test
    @DisplayName("Should find column by name case-insensitive")
    void shouldFindColumnByNameCaseInsensitive() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("TEST_COLUMN")
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(1)
                .build();
        
        TableMetadata table = TableMetadata.builder()
                .schemaName("TEST")
                .tableName("TABLE")
                .column(column)
                .build();
        
        Optional<ColumnMetadata> found = table.findColumn("test_column");
        assertThat(found).isPresent();
        assertThat(found.get().getColumnName()).isEqualTo("TEST_COLUMN");
    }
    
    @Test
    @DisplayName("Should get primary key columns")
    void shouldGetPrimaryKeyColumns() {
        ColumnMetadata idColumn = ColumnMetadata.builder()
                .columnName("ID")
                .dataType("NUMBER")
                .nullable(false)
                .columnId(1)
                .build();
        
        ColumnMetadata codeColumn = ColumnMetadata.builder()
                .columnName("CODE")
                .dataType("VARCHAR2")
                .dataLength(10)
                .nullable(false)
                .columnId(2)
                .build();
        
        PrimaryKeyMetadata primaryKey = PrimaryKeyMetadata.builder()
                .constraintName("PK_TEST")
                .columnName("ID")
                .columnName("CODE")
                .enabled(true)
                .validated(true)
                .build();
        
        TableMetadata table = TableMetadata.builder()
                .schemaName("TEST")
                .tableName("TABLE")
                .column(idColumn)
                .column(codeColumn)
                .primaryKey(primaryKey)
                .build();
        
        List<ColumnMetadata> pkColumns = table.getPrimaryKeyColumns();
        assertThat(pkColumns).hasSize(2);
        assertThat(pkColumns.get(0).getColumnName()).isEqualTo("ID");
        assertThat(pkColumns.get(1).getColumnName()).isEqualTo("CODE");
    }
    
    @Test
    @DisplayName("Should get nullable and non-nullable columns")
    void shouldGetNullableAndNonNullableColumns() {
        ColumnMetadata nullableColumn = ColumnMetadata.builder()
                .columnName("NULLABLE_COL")
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(1)
                .build();
        
        ColumnMetadata nonNullableColumn = ColumnMetadata.builder()
                .columnName("NON_NULLABLE_COL")
                .dataType("NUMBER")
                .nullable(false)
                .columnId(2)
                .build();
        
        TableMetadata table = TableMetadata.builder()
                .schemaName("TEST")
                .tableName("TABLE")
                .column(nullableColumn)
                .column(nonNullableColumn)
                .build();
        
        List<ColumnMetadata> nullable = table.getNullableColumns();
        List<ColumnMetadata> nonNullable = table.getNonNullableColumns();
        
        assertThat(nullable).hasSize(1);
        assertThat(nullable.get(0).getColumnName()).isEqualTo("NULLABLE_COL");
        
        assertThat(nonNullable).hasSize(1);
        assertThat(nonNullable.get(0).getColumnName()).isEqualTo("NON_NULLABLE_COL");
    }
    
    @Test
    @DisplayName("Should get columns by type category")
    void shouldGetColumnsByTypeCategory() {
        ColumnMetadata charColumn = ColumnMetadata.builder()
                .columnName("CHAR_COL")
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(1)
                .build();
        
        ColumnMetadata numColumn = ColumnMetadata.builder()
                .columnName("NUM_COL")
                .dataType("NUMBER")
                .dataPrecision(10)
                .nullable(true)
                .columnId(2)
                .build();
        
        ColumnMetadata dateColumn = ColumnMetadata.builder()
                .columnName("DATE_COL")
                .dataType("DATE")
                .nullable(true)
                .columnId(3)
                .build();
        
        TableMetadata table = TableMetadata.builder()
                .schemaName("TEST")
                .tableName("TABLE")
                .column(charColumn)
                .column(numColumn)
                .column(dateColumn)
                .build();
        
        List<ColumnMetadata> charColumns = table.getColumnsByType(TableMetadata.ColumnTypeCategory.CHARACTER);
        List<ColumnMetadata> numColumns = table.getColumnsByType(TableMetadata.ColumnTypeCategory.NUMERIC);
        List<ColumnMetadata> dateColumns = table.getColumnsByType(TableMetadata.ColumnTypeCategory.TEMPORAL);
        
        assertThat(charColumns).hasSize(1);
        assertThat(charColumns.get(0).getColumnName()).isEqualTo("CHAR_COL");
        
        assertThat(numColumns).hasSize(1);
        assertThat(numColumns.get(0).getColumnName()).isEqualTo("NUM_COL");
        
        assertThat(dateColumns).hasSize(1);
        assertThat(dateColumns.get(0).getColumnName()).isEqualTo("DATE_COL");
    }
    
    @Test
    @DisplayName("Should generate meaningful summary")
    void shouldGenerateMeaningfulSummary() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("ID")
                .dataType("NUMBER")
                .nullable(false)
                .columnId(1)
                .build();
        
        PrimaryKeyMetadata primaryKey = PrimaryKeyMetadata.builder()
                .constraintName("PK_TEST")
                .columnName("ID")
                .enabled(true)
                .validated(true)
                .build();
        
        IndexMetadata index = IndexMetadata.builder()
                .indexName("IDX_TEST")
                .indexType("NORMAL")
                .columnName("ID")
                .unique(false)
                .status("VALID")
                .build();
        
        TableMetadata table = TableMetadata.builder()
                .schemaName("TEST_SCHEMA")
                .tableName("TEST_TABLE")
                .tableComment("Test table")
                .column(column)
                .primaryKey(primaryKey)
                .index(index)
                .build();
        
        String summary = table.getSummary();
        assertThat(summary).contains("TEST_SCHEMA.TEST_TABLE");
        assertThat(summary).contains("1 columns");
        assertThat(summary).contains("PK: ID");
        assertThat(summary).contains("1 indexes");
        assertThat(summary).contains("Comment: Test table");
    }
    
    @Test
    @DisplayName("Should validate table metadata successfully")
    void shouldValidateTableMetadataSuccessfully() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("ID")
                .dataType("NUMBER")
                .nullable(false)
                .columnId(1)
                .build();
        
        TableMetadata table = TableMetadata.builder()
                .schemaName("TEST_SCHEMA")
                .tableName("TEST_TABLE")
                .column(column)
                .build();
        
        assertThatCode(table::validate).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should fail validation for empty schema name")
    void shouldFailValidationForEmptySchemaName() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("ID")
                .dataType("NUMBER")
                .nullable(false)
                .columnId(1)
                .build();
        
        TableMetadata table = TableMetadata.builder()
                .schemaName("")
                .tableName("TEST_TABLE")
                .column(column)
                .build();
        
        assertThatThrownBy(table::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Schema name cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should fail validation for table without columns")
    void shouldFailValidationForTableWithoutColumns() {
        TableMetadata table = TableMetadata.builder()
                .schemaName("TEST_SCHEMA")
                .tableName("TEST_TABLE")
                .build();
        
        assertThatThrownBy(table::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Table must have at least one column");
    }
    
    @Test
    @DisplayName("Should fail validation for primary key referencing non-existent column")
    void shouldFailValidationForPrimaryKeyReferencingNonExistentColumn() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("ID")
                .dataType("NUMBER")
                .nullable(false)
                .columnId(1)
                .build();
        
        PrimaryKeyMetadata primaryKey = PrimaryKeyMetadata.builder()
                .constraintName("PK_TEST")
                .columnName("NON_EXISTENT")
                .enabled(true)
                .validated(true)
                .build();
        
        TableMetadata table = TableMetadata.builder()
                .schemaName("TEST_SCHEMA")
                .tableName("TEST_TABLE")
                .column(column)
                .primaryKey(primaryKey)
                .build();
        
        assertThatThrownBy(table::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Primary key references non-existent column");
    }
}
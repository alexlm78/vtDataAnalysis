package dev.kreaker.vtda.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ColumnMetadata Tests")
class ColumnMetadataTest {
    
    @Test
    @DisplayName("Should create valid VARCHAR2 column")
    void shouldCreateValidVarchar2Column() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("NAME")
                .dataType("VARCHAR2")
                .dataLength(100)
                .nullable(true)
                .columnId(1)
                .build();
        
        assertThat(column.getColumnName()).isEqualTo("NAME");
        assertThat(column.getDataType()).isEqualTo("VARCHAR2");
        assertThat(column.getDataLength()).isEqualTo(100);
        assertThat(column.isNullable()).isTrue();
        assertThat(column.getColumnId()).isEqualTo(1);
        assertThat(column.getFullDataType()).isEqualTo("VARCHAR2(100)");
        assertThat(column.isCharacterType()).isTrue();
        assertThat(column.isNumericType()).isFalse();
    }
    
    @Test
    @DisplayName("Should create valid NUMBER column")
    void shouldCreateValidNumberColumn() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("PRICE")
                .dataType("NUMBER")
                .dataPrecision(10)
                .dataScale(2)
                .nullable(false)
                .columnId(2)
                .build();
        
        assertThat(column.getFullDataType()).isEqualTo("NUMBER(10,2)");
        assertThat(column.isNumericType()).isTrue();
        assertThat(column.isCharacterType()).isFalse();
        assertThat(column.isNullable()).isFalse();
    }
    
    @Test
    @DisplayName("Should create valid DATE column")
    void shouldCreateValidDateColumn() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("CREATED_DATE")
                .dataType("DATE")
                .nullable(false)
                .columnId(3)
                .build();
        
        assertThat(column.getFullDataType()).isEqualTo("DATE");
        assertThat(column.isTemporalType()).isTrue();
        assertThat(column.isCharacterType()).isFalse();
        assertThat(column.isNumericType()).isFalse();
    }
    
    @Test
    @DisplayName("Should create valid TIMESTAMP column")
    void shouldCreateValidTimestampColumn() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("LAST_UPDATED")
                .dataType("TIMESTAMP")
                .dataPrecision(6)
                .nullable(true)
                .columnId(4)
                .build();
        
        assertThat(column.getFullDataType()).isEqualTo("TIMESTAMP(6)");
        assertThat(column.isTemporalType()).isTrue();
    }
    
    @Test
    @DisplayName("Should create valid CLOB column")
    void shouldCreateValidClobColumn() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("DESCRIPTION")
                .dataType("CLOB")
                .nullable(true)
                .columnId(5)
                .build();
        
        assertThat(column.getFullDataType()).isEqualTo("CLOB");
        assertThat(column.isLobType()).isTrue();
        assertThat(column.isCharacterType()).isTrue();
    }
    
    @Test
    @DisplayName("Should validate column metadata successfully")
    void shouldValidateColumnMetadataSuccessfully() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("VALID_COLUMN")
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(1)
                .build();
        
        assertThatCode(column::validate).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should fail validation for null column name")
    void shouldFailValidationForNullColumnName() {
        // Lombok @NonNull prevents null values at build time, so we test with empty string
        assertThatThrownBy(() -> ColumnMetadata.builder()
                .columnName(null)
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(1)
                .build())
                .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    @DisplayName("Should fail validation for empty column name")
    void shouldFailValidationForEmptyColumnName() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("")
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(1)
                .build();
        
        assertThatThrownBy(column::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Column name cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should fail validation for empty data type")
    void shouldFailValidationForEmptyDataType() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("TEST_COLUMN")
                .dataType("")
                .nullable(true)
                .columnId(1)
                .build();
        
        assertThatThrownBy(column::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Data type cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should fail validation for invalid column ID")
    void shouldFailValidationForInvalidColumnId() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("TEST_COLUMN")
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(0)
                .build();
        
        assertThatThrownBy(column::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Column ID must be positive");
    }
    
    @Test
    @DisplayName("Should fail validation for negative precision")
    void shouldFailValidationForNegativePrecision() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("TEST_COLUMN")
                .dataType("NUMBER")
                .dataPrecision(-1)
                .nullable(true)
                .columnId(1)
                .build();
        
        assertThatThrownBy(column::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Numeric precision must be positive");
    }
    
    @Test
    @DisplayName("Should fail validation for scale exceeding precision")
    void shouldFailValidationForScaleExceedingPrecision() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("TEST_COLUMN")
                .dataType("NUMBER")
                .dataPrecision(5)
                .dataScale(10)
                .nullable(true)
                .columnId(1)
                .build();
        
        assertThatThrownBy(column::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Data scale cannot exceed precision");
    }
    
    @Test
    @DisplayName("Should handle equality correctly")
    void shouldHandleEqualityCorrectly() {
        ColumnMetadata column1 = ColumnMetadata.builder()
                .columnName("TEST")
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(1)
                .build();
        
        ColumnMetadata column2 = ColumnMetadata.builder()
                .columnName("TEST")
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(1)
                .build();
        
        ColumnMetadata column3 = ColumnMetadata.builder()
                .columnName("DIFFERENT")
                .dataType("VARCHAR2")
                .dataLength(50)
                .nullable(true)
                .columnId(1)
                .build();
        
        assertThat(column1).isEqualTo(column2);
        assertThat(column1).isNotEqualTo(column3);
        assertThat(column1.hashCode()).isEqualTo(column2.hashCode());
    }
    
    @Test
    @DisplayName("Should generate meaningful toString")
    void shouldGenerateMeaningfulToString() {
        ColumnMetadata column = ColumnMetadata.builder()
                .columnName("TEST_COLUMN")
                .dataType("VARCHAR2")
                .dataLength(100)
                .nullable(true)
                .columnId(1)
                .build();
        
        String toString = column.toString();
        assertThat(toString).contains("TEST_COLUMN");
        assertThat(toString).contains("VARCHAR2(100)");
        assertThat(toString).contains("nullable=true");
        assertThat(toString).contains("position=1");
    }
}
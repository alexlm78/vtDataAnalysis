package dev.kreaker.vtda.service.impl;

import dev.kreaker.vtda.model.DatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for Oracle data type mapping functionality.
 * Uses reflection to test the private mapOracleDataType method.
 */
@ExtendWith(MockitoExtension.class)
class OracleDataTypeMappingTest {
    
    private MetadataExtractionServiceImpl metadataService;
    private Method mapOracleDataTypeMethod;
    
    @BeforeEach
    void setUp() throws Exception {
        DatabaseConfig databaseConfig = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .connectionTimeout(30)
                .queryTimeout(300)
                .build();
        
        DataSource dataSource = mock(DataSource.class);
        metadataService = new MetadataExtractionServiceImpl(databaseConfig, dataSource);
        
        // Get the private method using reflection
        mapOracleDataTypeMethod = MetadataExtractionServiceImpl.class
                .getDeclaredMethod("mapOracleDataType", String.class, Integer.class, Integer.class, Integer.class, String.class);
        mapOracleDataTypeMethod.setAccessible(true);
    }
    
    @Test
    void testMapOracleDataType_VARCHAR2_WithByteSemantics() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "VARCHAR2", 100, null, null, "B");
        
        // Assert
        assertThat(result).isEqualTo("VARCHAR2(100 BYTE)");
    }
    
    @Test
    void testMapOracleDataType_VARCHAR2_WithCharSemantics() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "VARCHAR2", 50, null, null, "C");
        
        // Assert
        assertThat(result).isEqualTo("VARCHAR2(50 CHAR)");
    }
    
    @Test
    void testMapOracleDataType_VARCHAR2_WithoutLength() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "VARCHAR2", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("VARCHAR2");
    }
    
    @Test
    void testMapOracleDataType_NVARCHAR2() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "NVARCHAR2", 200, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("NVARCHAR2(200)");
    }
    
    @Test
    void testMapOracleDataType_CHAR_WithByteSemantics() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "CHAR", 10, null, null, "B");
        
        // Assert
        assertThat(result).isEqualTo("CHAR(10 BYTE)");
    }
    
    @Test
    void testMapOracleDataType_CHAR_WithCharSemantics() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "CHAR", 10, null, null, "C");
        
        // Assert
        assertThat(result).isEqualTo("CHAR(10 CHAR)");
    }
    
    @Test
    void testMapOracleDataType_NCHAR() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "NCHAR", 15, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("NCHAR(15)");
    }
    
    @Test
    void testMapOracleDataType_NUMBER_WithPrecisionAndScale() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "NUMBER", null, 10, 2, null);
        
        // Assert
        assertThat(result).isEqualTo("NUMBER(10,2)");
    }
    
    @Test
    void testMapOracleDataType_NUMBER_WithPrecisionOnly() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "NUMBER", null, 8, 0, null);
        
        // Assert
        assertThat(result).isEqualTo("NUMBER(8)");
    }
    
    @Test
    void testMapOracleDataType_NUMBER_WithoutPrecision() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "NUMBER", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("NUMBER");
    }
    
    @Test
    void testMapOracleDataType_FLOAT_WithPrecision() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "FLOAT", null, 126, null, null);
        
        // Assert
        assertThat(result).isEqualTo("FLOAT(126)");
    }
    
    @Test
    void testMapOracleDataType_BINARY_FLOAT() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "BINARY_FLOAT", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("BINARY_FLOAT");
    }
    
    @Test
    void testMapOracleDataType_BINARY_DOUBLE() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "BINARY_DOUBLE", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("BINARY_DOUBLE");
    }
    
    @Test
    void testMapOracleDataType_DATE() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "DATE", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("DATE");
    }
    
    @Test
    void testMapOracleDataType_TIMESTAMP_WithPrecision() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "TIMESTAMP", null, null, 6, null);
        
        // Assert
        assertThat(result).isEqualTo("TIMESTAMP(6)");
    }
    
    @Test
    void testMapOracleDataType_TIMESTAMP_WithoutPrecision() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "TIMESTAMP", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("TIMESTAMP");
    }
    
    @Test
    void testMapOracleDataType_TIMESTAMP_WITH_TIME_ZONE() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "TIMESTAMP WITH TIME ZONE", null, null, 9, null);
        
        // Assert
        assertThat(result).isEqualTo("TIMESTAMP(9) WITH TIME ZONE");
    }
    
    @Test
    void testMapOracleDataType_TIMESTAMP_WITH_LOCAL_TIME_ZONE() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "TIMESTAMP WITH LOCAL TIME ZONE", null, null, 3, null);
        
        // Assert
        assertThat(result).isEqualTo("TIMESTAMP(3) WITH LOCAL TIME ZONE");
    }
    
    @Test
    void testMapOracleDataType_INTERVAL_YEAR_TO_MONTH() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "INTERVAL YEAR TO MONTH", null, 2, null, null);
        
        // Assert
        assertThat(result).isEqualTo("INTERVAL YEAR(2) TO MONTH");
    }
    
    @Test
    void testMapOracleDataType_INTERVAL_DAY_TO_SECOND() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "INTERVAL DAY TO SECOND", null, 2, 6, null);
        
        // Assert
        assertThat(result).isEqualTo("INTERVAL DAY(2) TO SECOND(6)");
    }
    
    @Test
    void testMapOracleDataType_RAW() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "RAW", 2000, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("RAW(2000)");
    }
    
    @Test
    void testMapOracleDataType_LONG_RAW() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "LONG RAW", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("LONG RAW");
    }
    
    @Test
    void testMapOracleDataType_LONG() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "LONG", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("LONG");
    }
    
    @Test
    void testMapOracleDataType_CLOB() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "CLOB", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("CLOB");
    }
    
    @Test
    void testMapOracleDataType_NCLOB() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "NCLOB", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("NCLOB");
    }
    
    @Test
    void testMapOracleDataType_BLOB() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "BLOB", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("BLOB");
    }
    
    @Test
    void testMapOracleDataType_BFILE() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "BFILE", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("BFILE");
    }
    
    @Test
    void testMapOracleDataType_ROWID() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "ROWID", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("ROWID");
    }
    
    @Test
    void testMapOracleDataType_UROWID() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "UROWID", 4000, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("UROWID(4000)");
    }
    
    @Test
    void testMapOracleDataType_XMLTYPE() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "XMLTYPE", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("XMLTYPE");
    }
    
    @Test
    void testMapOracleDataType_UnknownType() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "CUSTOM_TYPE", null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("CUSTOM_TYPE");
    }
    
    @Test
    void testMapOracleDataType_NullType() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, null, null, null, null, null);
        
        // Assert
        assertThat(result).isEqualTo("UNKNOWN");
    }
    
    @Test
    void testMapOracleDataType_CaseInsensitive() throws Exception {
        // Act
        String result = (String) mapOracleDataTypeMethod.invoke(metadataService, "varchar2", 100, null, null, "B");
        
        // Assert
        assertThat(result).isEqualTo("VARCHAR2(100 BYTE)");
    }
}
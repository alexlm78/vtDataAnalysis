package dev.kreaker.vtda.service.impl;

import dev.kreaker.vtda.exception.DatabaseConnectionException;
import dev.kreaker.vtda.exception.MetadataExtractionException;
import dev.kreaker.vtda.exception.SchemaNotFoundException;
import dev.kreaker.vtda.model.*;
import dev.kreaker.vtda.service.MetadataExtractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MetadataExtractionServiceImpl.
 * Uses Mockito to mock database interactions and test business logic.
 */
@ExtendWith(MockitoExtension.class)
class MetadataExtractionServiceImplTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    private DatabaseConfig databaseConfig;
    private MetadataExtractionService metadataService;
    
    @BeforeEach
    void setUp() {
        databaseConfig = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .connectionTimeout(30)
                .queryTimeout(300)
                .build();
        
        metadataService = new MetadataExtractionServiceImpl(databaseConfig, dataSource);
    }
    
    @Test
    void testValidateConnection_Success() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(30)).thenReturn(true);
        
        // Act & Assert
        assertThatCode(() -> metadataService.validateConnection())
                .doesNotThrowAnyException();
        
        verify(connection).isValid(30);
        verify(connection).close();
    }
    
    @Test
    void testValidateConnection_Failure() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(30)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> metadataService.validateConnection())
                .isInstanceOf(DatabaseConnectionException.class)
                .hasMessageContaining("Database connection validation failed");
        
        verify(connection).close();
    }
    
    @Test
    void testValidateConnection_SQLException() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        
        // Act & Assert
        assertThatThrownBy(() -> metadataService.validateConnection())
                .isInstanceOf(DatabaseConnectionException.class)
                .hasMessageContaining("Failed to validate database connection")
                .hasCauseInstanceOf(SQLException.class);
    }
    
    @Test
    void testValidateSchema_Success() throws Exception {
        // Arrange
        Connection validationConn = mock(Connection.class);
        PreparedStatement validationStmt = mock(PreparedStatement.class);
        ResultSet validationRs = mock(ResultSet.class);
        
        when(dataSource.getConnection())
                .thenReturn(connection) // For connection validation
                .thenReturn(validationConn); // For schema validation
        
        when(connection.isValid(30)).thenReturn(true);
        when(validationConn.prepareStatement(contains("ALL_USERS"))).thenReturn(validationStmt);
        when(validationStmt.executeQuery()).thenReturn(validationRs);
        when(validationRs.next()).thenReturn(true);
        when(validationRs.getInt(1)).thenReturn(1);
        
        // Act & Assert
        assertThatCode(() -> metadataService.validateSchema("TESTSCHEMA"))
                .doesNotThrowAnyException();
        
        verify(validationStmt).setString(1, "TESTSCHEMA");
    }
    
    @Test
    void testValidateSchema_NotFound() throws Exception {
        // Arrange
        Connection validationConn = mock(Connection.class);
        PreparedStatement validationStmt = mock(PreparedStatement.class);
        ResultSet validationRs = mock(ResultSet.class);
        
        // Mock for getAvailableSchemas call
        Connection schemasConn = mock(Connection.class);
        PreparedStatement schemasStmt = mock(PreparedStatement.class);
        ResultSet schemasRs = mock(ResultSet.class);
        
        when(dataSource.getConnection())
                .thenReturn(connection) // For connection validation
                .thenReturn(validationConn) // For schema validation
                .thenReturn(connection) // For connection validation in getAvailableSchemas
                .thenReturn(schemasConn); // For getting available schemas
        
        when(connection.isValid(30)).thenReturn(true);
        when(validationConn.prepareStatement(contains("ALL_USERS"))).thenReturn(validationStmt);
        when(validationStmt.executeQuery()).thenReturn(validationRs);
        when(validationRs.next()).thenReturn(true);
        when(validationRs.getInt(1)).thenReturn(0); // Schema not found
        
        when(schemasConn.prepareStatement(contains("USERNAME"))).thenReturn(schemasStmt);
        when(schemasStmt.executeQuery()).thenReturn(schemasRs);
        when(schemasRs.next()).thenReturn(true, false);
        when(schemasRs.getString("USERNAME")).thenReturn("SCHEMA1");
        
        // Act & Assert
        assertThatThrownBy(() -> metadataService.validateSchema("NONEXISTENT"))
                .isInstanceOf(SchemaNotFoundException.class)
                .hasMessageContaining("NONEXISTENT");
        
        verify(validationStmt).setString(1, "NONEXISTENT");
    }
    
    @Test
    void testGetAvailableSchemas_Success() throws Exception {
        // Arrange
        Connection validationConn = mock(Connection.class);
        Connection queryConn = mock(Connection.class);
        PreparedStatement queryStmt = mock(PreparedStatement.class);
        ResultSet queryRs = mock(ResultSet.class);
        
        when(dataSource.getConnection())
                .thenReturn(validationConn) // For validation
                .thenReturn(queryConn); // For query
        
        when(validationConn.isValid(30)).thenReturn(true);
        when(queryConn.prepareStatement(contains("USERNAME"))).thenReturn(queryStmt);
        when(queryStmt.executeQuery()).thenReturn(queryRs);
        when(queryRs.next()).thenReturn(true, true, false);
        when(queryRs.getString("USERNAME")).thenReturn("SCHEMA1", "SCHEMA2");
        
        // Act
        List<String> schemas = metadataService.getAvailableSchemas();
        
        // Assert
        assertThat(schemas).containsExactly("SCHEMA1", "SCHEMA2");
        verify(queryStmt).setQueryTimeout(300);
    }
    
    @Test
    void testExtractTableMetadata_SchemaNotFound() throws Exception {
        // Arrange
        Connection validationConn = mock(Connection.class);
        PreparedStatement validationStmt = mock(PreparedStatement.class);
        ResultSet validationRs = mock(ResultSet.class);
        
        // Mock for getAvailableSchemas call
        Connection schemasConn = mock(Connection.class);
        PreparedStatement schemasStmt = mock(PreparedStatement.class);
        ResultSet schemasRs = mock(ResultSet.class);
        
        when(dataSource.getConnection())
                .thenReturn(connection) // For connection validation in extractTableMetadata
                .thenReturn(connection) // For connection validation in validateSchema
                .thenReturn(validationConn) // For schema validation
                .thenReturn(connection) // For connection validation in getAvailableSchemas
                .thenReturn(schemasConn); // For getting available schemas
        
        when(connection.isValid(30)).thenReturn(true);
        when(validationConn.prepareStatement(contains("ALL_USERS"))).thenReturn(validationStmt);
        when(validationStmt.executeQuery()).thenReturn(validationRs);
        when(validationRs.next()).thenReturn(true);
        when(validationRs.getInt(1)).thenReturn(0); // Schema not found
        
        when(schemasConn.prepareStatement(contains("USERNAME"))).thenReturn(schemasStmt);
        when(schemasStmt.executeQuery()).thenReturn(schemasRs);
        when(schemasRs.next()).thenReturn(true, false);
        when(schemasRs.getString("USERNAME")).thenReturn("SCHEMA1");
        
        FilterConfig filters = FilterConfig.includeAll();
        
        // Act & Assert
        assertThatThrownBy(() -> metadataService.extractTableMetadata("NONEXISTENT", filters))
                .isInstanceOf(SchemaNotFoundException.class)
                .hasMessageContaining("NONEXISTENT");
    }
    
    @Test
    void testGetDatabaseConfig() {
        // Act
        DatabaseConfig config = metadataService.getDatabaseConfig();
        
        // Assert
        assertThat(config).isEqualTo(databaseConfig);
    }
    
    @Test
    void testClose() {
        // Act & Assert
        assertThatCode(() -> metadataService.close())
                .doesNotThrowAnyException();
    }
    

}
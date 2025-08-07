package dev.kreaker.vtda.service.impl;

import dev.kreaker.vtda.model.DatabaseConfig;
import dev.kreaker.vtda.service.MetadataExtractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Basic unit tests for MetadataExtractionServiceImpl focusing on core functionality.
 */
class MetadataExtractionServiceBasicTest {
    
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
        
        DataSource dataSource = mock(DataSource.class);
        metadataService = new MetadataExtractionServiceImpl(databaseConfig, dataSource);
    }
    
    @Test
    void testGetDatabaseConfig() {
        // Act
        DatabaseConfig config = metadataService.getDatabaseConfig();
        
        // Assert
        assertThat(config).isEqualTo(databaseConfig);
        assertThat(config.getSchema()).isEqualTo("TESTSCHEMA");
        assertThat(config.getConnectionTimeout()).isEqualTo(30);
        assertThat(config.getQueryTimeout()).isEqualTo(300);
    }
    
    @Test
    void testServiceInstantiation() {
        // Assert
        assertThat(metadataService).isNotNull();
        assertThat(metadataService).isInstanceOf(MetadataExtractionServiceImpl.class);
    }
    
    @Test
    void testClose() {
        // Act & Assert - should not throw any exception
        metadataService.close();
    }
}
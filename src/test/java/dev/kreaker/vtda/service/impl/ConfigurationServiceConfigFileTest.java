package dev.kreaker.vtda.service.impl;

import dev.kreaker.vtda.exception.ConfigurationException;
import dev.kreaker.vtda.model.DatabaseConfig;
import dev.kreaker.vtda.model.ExportConfig;
import dev.kreaker.vtda.model.FilterConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.env.StandardEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for ConfigurationService configuration file loading functionality.
 */
class ConfigurationServiceConfigFileTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void shouldLoadConfigurationFromPropertiesFile() throws ConfigurationException, IOException {
        // Given
        Path configFile = tempDir.resolve("test.properties");
        Properties props = new Properties();
        props.setProperty("database.url", "jdbc:oracle:thin:@confighost:1521:configsid");
        props.setProperty("database.username", "configuser");
        props.setProperty("database.password", "configpass");
        props.setProperty("database.schema", "CONFIGSCHEMA");
        props.setProperty("database.connection-timeout", "45");
        props.setProperty("database.query-timeout", "450");
        props.setProperty("export.format", "JSON");
        props.setProperty("export.output", "output.json");
        props.setProperty("export.encoding", "ISO-8859-1");
        props.setProperty("filter.include", "USER_*, ACCOUNT_*");
        props.setProperty("filter.exclude", "TEMP_*, LOG_*");
        props.setProperty("filter.case-sensitive", "true");
        
        props.store(Files.newOutputStream(configFile), "Test configuration");
        
        String[] args = {"--config=" + configFile.toString()};
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When
        DatabaseConfig dbConfig = configurationService.getDatabaseConfig();
        ExportConfig exportConfig = configurationService.getExportConfig();
        FilterConfig filterConfig = configurationService.getFilterConfig();
        
        // Then
        assertThat(dbConfig.getUrl()).isEqualTo("jdbc:oracle:thin:@confighost:1521:configsid");
        assertThat(dbConfig.getUsername()).isEqualTo("configuser");
        assertThat(dbConfig.getPassword()).isEqualTo("configpass");
        assertThat(dbConfig.getSchema()).isEqualTo("CONFIGSCHEMA");
        assertThat(dbConfig.getConnectionTimeout()).isEqualTo(45);
        assertThat(dbConfig.getQueryTimeout()).isEqualTo(450);
        
        assertThat(exportConfig.getFormat()).isEqualTo(ExportConfig.ExportFormat.JSON);
        assertThat(exportConfig.getOutputPath()).isEqualTo("output.json");
        assertThat(exportConfig.getEncoding()).isEqualTo("ISO-8859-1");
        assertThat(exportConfig.isUseStandardOutput()).isFalse();
        
        assertThat(filterConfig.getIncludePatterns()).containsExactly("USER_*", "ACCOUNT_*");
        assertThat(filterConfig.getExcludePatterns()).containsExactly("TEMP_*", "LOG_*");
        assertThat(filterConfig.isCaseSensitive()).isTrue();
    }
    
    @Test
    void shouldPrioritizeCommandLineArgumentsOverConfigFile() throws ConfigurationException, IOException {
        // Given
        Path configFile = tempDir.resolve("test.properties");
        Properties props = new Properties();
        props.setProperty("database.url", "jdbc:oracle:thin:@confighost:1521:configsid");
        props.setProperty("database.username", "configuser");
        props.setProperty("database.schema", "CONFIGSCHEMA");
        props.setProperty("export.format", "XML");
        
        props.store(Files.newOutputStream(configFile), "Test configuration");
        
        String[] args = {
            "--config=" + configFile.toString(),
            "--username=cliuser",
            "--schema=CLISCHEMA",
            "--format=JSON"
        };
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When
        DatabaseConfig dbConfig = configurationService.getDatabaseConfig();
        ExportConfig exportConfig = configurationService.getExportConfig();
        
        // Then
        assertThat(dbConfig.getUrl()).isEqualTo("jdbc:oracle:thin:@confighost:1521:configsid"); // from config file
        assertThat(dbConfig.getUsername()).isEqualTo("cliuser"); // from CLI (higher priority)
        assertThat(dbConfig.getSchema()).isEqualTo("CLISCHEMA"); // from CLI (higher priority)
        
        assertThat(exportConfig.getFormat()).isEqualTo(ExportConfig.ExportFormat.JSON); // from CLI (higher priority)
    }
    
    @Test
    void shouldThrowExceptionWhenConfigurationFileNotFound() {
        // Given
        String[] args = {"--config=nonexistent.properties"};
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When & Then
        assertThatThrownBy(() -> configurationService.getDatabaseConfig())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Failed to build database configuration")
                .hasCauseInstanceOf(ConfigurationException.class);
    }
    
    @Test
    void shouldLoadYamlConfigurationFile() throws ConfigurationException {
        // Given
        String[] args = {
            "--config=src/test/resources/test-config.yml"
        };
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When
        DatabaseConfig databaseConfig = configurationService.getDatabaseConfig();
        ExportConfig exportConfig = configurationService.getExportConfig();
        FilterConfig filterConfig = configurationService.getFilterConfig();
        
        // Then - Database config
        assertThat(databaseConfig.getUrl()).isEqualTo("jdbc:oracle:thin:@localhost:1521:xe");
        assertThat(databaseConfig.getUsername()).isEqualTo("testuser");
        assertThat(databaseConfig.getPassword()).isEqualTo("testpass");
        assertThat(databaseConfig.getSchema()).isEqualTo("TESTSCHEMA");
        assertThat(databaseConfig.getConnectionTimeout()).isEqualTo(60);
        assertThat(databaseConfig.getQueryTimeout()).isEqualTo(600);
        
        // Then - Export config
        assertThat(exportConfig.getFormat()).isEqualTo(ExportConfig.ExportFormat.JSON);
        assertThat(exportConfig.getOutputPath()).isEqualTo("test-output.json");
        assertThat(exportConfig.isIncludeComments()).isFalse();
        assertThat(exportConfig.isPrettyPrint()).isTrue();
        
        // Then - Filter config
        
        // Now test the actual patterns
        assertThat(filterConfig.getIncludePatterns()).containsExactly("USER_*", "CUSTOMER_*");
        assertThat(filterConfig.getExcludePatterns()).containsExactly("TEMP_*");
        assertThat(filterConfig.isCaseSensitive()).isTrue();
        assertThat(filterConfig.isUseRegex()).isFalse();
    }
}
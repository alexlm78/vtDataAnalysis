package dev.kreaker.vtda.service.impl;

import dev.kreaker.vtda.exception.ConfigurationException;
import dev.kreaker.vtda.model.DatabaseConfig;
import dev.kreaker.vtda.model.ExportConfig;
import dev.kreaker.vtda.model.FilterConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for ConfigurationService using real Spring components.
 */
class ConfigurationServiceIntegrationTest {
    
    @Test
    void shouldCreateConfigurationServiceWithMinimalArguments() throws ConfigurationException {
        // Given
        String[] args = {
            "--url=jdbc:oracle:thin:@host:1521:sid",
            "--username=testuser",
            "--password=testpass",
            "--schema=TESTSCHEMA"
        };
        
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When
        DatabaseConfig dbConfig = configurationService.getDatabaseConfig();
        ExportConfig exportConfig = configurationService.getExportConfig();
        FilterConfig filterConfig = configurationService.getFilterConfig();
        
        // Then
        assertThat(dbConfig.getUrl()).isEqualTo("jdbc:oracle:thin:@host:1521:sid");
        assertThat(dbConfig.getUsername()).isEqualTo("testuser");
        assertThat(dbConfig.getPassword()).isEqualTo("testpass");
        assertThat(dbConfig.getSchema()).isEqualTo("TESTSCHEMA");
        
        assertThat(exportConfig.getFormat()).isEqualTo(ExportConfig.ExportFormat.CSV);
        assertThat(exportConfig.isUseStandardOutput()).isTrue();
        
        assertThat(filterConfig.getIncludePatterns()).isEmpty();
        assertThat(filterConfig.getExcludePatterns()).isEmpty();
    }
    
    @Test
    void shouldHandleVerboseMode() {
        // Given
        String[] args = {"--verbose"};
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When & Then
        assertThat(configurationService.isVerboseMode()).isTrue();
        assertThat(configurationService.isQuietMode()).isFalse();
    }
    
    @Test
    void shouldHandleQuietMode() {
        // Given
        String[] args = {"--quiet"};
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When & Then
        assertThat(configurationService.isQuietMode()).isTrue();
        assertThat(configurationService.isVerboseMode()).isFalse();
    }
    
    @Test
    void shouldHandleHelpRequest() {
        // Given
        String[] args = {"--help"};
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When & Then
        assertThat(configurationService.isHelpRequested()).isTrue();
    }
    
    @Test
    void shouldHandleExportConfiguration() throws ConfigurationException {
        // Given
        String[] args = {
            "--format=JSON",
            "--output=output.json",
            "--encoding=ISO-8859-1"
        };
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When
        ExportConfig config = configurationService.getExportConfig();
        
        // Then
        assertThat(config.getFormat()).isEqualTo(ExportConfig.ExportFormat.JSON);
        assertThat(config.getOutputPath()).isEqualTo("output.json");
        assertThat(config.getEncoding()).isEqualTo("ISO-8859-1");
        assertThat(config.isUseStandardOutput()).isFalse();
    }
    
    @Test
    void shouldHandleFilterConfiguration() throws ConfigurationException {
        // Given
        String[] args = {
            "--include=USER_*",
            "--include=ACCOUNT_*",
            "--exclude=TEMP_*",
            "--case-sensitive"
        };
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When
        FilterConfig config = configurationService.getFilterConfig();
        
        // Then
        assertThat(config.getIncludePatterns()).containsExactly("USER_*", "ACCOUNT_*");
        assertThat(config.getExcludePatterns()).containsExactly("TEMP_*");
        assertThat(config.isCaseSensitive()).isTrue();
    }
    
    @Test
    void shouldValidateConfigurationSuccessfully() throws ConfigurationException {
        // Given
        String[] args = {
            "--url=jdbc:oracle:thin:@host:1521:sid",
            "--username=testuser",
            "--password=testpass",
            "--schema=TESTSCHEMA"
        };
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When & Then
        assertThatCode(() -> configurationService.validateConfiguration()).doesNotThrowAnyException();
    }
    
    @Test
    void shouldFailValidationWhenBothVerboseAndQuietSpecified() {
        // Given
        String[] args = {
            "--url=jdbc:oracle:thin:@host:1521:sid",
            "--username=testuser",
            "--password=testpass",
            "--schema=TESTSCHEMA",
            "--verbose",
            "--quiet"
        };
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When & Then
        assertThatThrownBy(() -> configurationService.validateConfiguration())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Cannot specify both verbose and quiet modes simultaneously");
    }
    
    @Test
    void shouldReturnHelpText() {
        // Given
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(new String[]{});
        StandardEnvironment environment = new StandardEnvironment();
        
        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When
        String helpText = configurationService.getHelpText();
        
        // Then
        assertThat(helpText).isNotNull();
        assertThat(helpText).contains("Database Metadata Analyzer");
        assertThat(helpText).contains("--url");
        assertThat(helpText).contains("--username");
        assertThat(helpText).contains("--schema");
        assertThat(helpText).contains("--format");
        assertThat(helpText).contains("Examples:");
    }
}
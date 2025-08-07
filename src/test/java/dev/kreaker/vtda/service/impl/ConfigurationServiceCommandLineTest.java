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
 * Test class for command-line interface argument parsing and validation.
 * Tests requirement 4.1 and 4.5 - command-line argument parsing and validation.
 */
class ConfigurationServiceCommandLineTest {
    
    @Test
    void shouldParseAllDatabaseConnectionArguments() throws ConfigurationException {
        // Given - all required database connection arguments
        String[] args = {
            "--url=jdbc:oracle:thin:@localhost:1521:xe",
            "--username=dbuser",
            "--password=dbpass",
            "--schema=MYSCHEMA",
            "--connection-timeout=60",
            "--query-timeout=600"
        };
        
        ConfigurationServiceImpl service = createService(args);
        
        // When
        DatabaseConfig config = service.getDatabaseConfig();
        
        // Then
        assertThat(config.getUrl()).isEqualTo("jdbc:oracle:thin:@localhost:1521:xe");
        assertThat(config.getUsername()).isEqualTo("dbuser");
        assertThat(config.getPassword()).isEqualTo("dbpass");
        assertThat(config.getSchema()).isEqualTo("MYSCHEMA");
        assertThat(config.getConnectionTimeout()).isEqualTo(60);
        assertThat(config.getQueryTimeout()).isEqualTo(600);
    }
    
    @Test
    void shouldParseAllExportFormatArguments() throws ConfigurationException {
        // Given - all export format arguments
        String[] args = {
            "--format=JSON",
            "--output=/path/to/output.json",
            "--include-comments",
            "--pretty-print",
            "--append",
            "--encoding=UTF-16"
        };
        
        ConfigurationServiceImpl service = createService(args);
        
        // When
        ExportConfig config = service.getExportConfig();
        
        // Then
        assertThat(config.getFormat()).isEqualTo(ExportConfig.ExportFormat.JSON);
        assertThat(config.getOutputPath()).isEqualTo("/path/to/output.json");
        assertThat(config.isIncludeComments()).isTrue();
        assertThat(config.isPrettyPrint()).isTrue();
        assertThat(config.isAppendToFile()).isTrue();
        assertThat(config.getEncoding()).isEqualTo("UTF-16");
        assertThat(config.isUseStandardOutput()).isFalse();
    }
    
    @Test
    void shouldParseAllFilterArguments() throws ConfigurationException {
        // Given - all filter arguments
        String[] args = {
            "--include=USER_*",
            "--include=ACCOUNT_*",
            "--exclude=TEMP_*",
            "--exclude=LOG_*",
            "--case-sensitive",
            "--regex"
        };
        
        ConfigurationServiceImpl service = createService(args);
        
        // When
        FilterConfig config = service.getFilterConfig();
        
        // Then
        assertThat(config.getIncludePatterns()).containsExactly("USER_*", "ACCOUNT_*");
        assertThat(config.getExcludePatterns()).containsExactly("TEMP_*", "LOG_*");
        assertThat(config.isCaseSensitive()).isTrue();
        assertThat(config.isUseRegex()).isTrue();
    }
    
    @Test
    void shouldHandleVerboseAndQuietModeFlags() {
        // Test verbose mode
        ConfigurationServiceImpl verboseService = createService(new String[]{"--verbose"});
        assertThat(verboseService.isVerboseMode()).isTrue();
        assertThat(verboseService.isQuietMode()).isFalse();
        
        // Test quiet mode
        ConfigurationServiceImpl quietService = createService(new String[]{"--quiet"});
        assertThat(quietService.isQuietMode()).isTrue();
        assertThat(quietService.isVerboseMode()).isFalse();
    }
    
    @Test
    void shouldHandleHelpRequestFlags() {
        // Test help flag
        ConfigurationServiceImpl helpService = createService(new String[]{"--help"});
        assertThat(helpService.isHelpRequested()).isTrue();
    }
    
    @Test
    void shouldValidateRequiredArgumentsWhenNoConfigFile() {
        // Given - missing required arguments
        String[] args = {"--format=CSV"};
        ConfigurationServiceImpl service = createService(args);
        
        // When & Then
        assertThatThrownBy(() -> service.validateConfiguration())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Database URL is required")
                .hasMessageContaining("Database username is required")
                .hasMessageContaining("Database password is required")
                .hasMessageContaining("Database schema is required");
    }
    
    @Test
    void shouldValidateInvalidExportFormat() {
        // Given - invalid export format
        String[] args = {
            "--url=jdbc:oracle:thin:@host:1521:sid",
            "--username=user",
            "--password=pass",
            "--schema=SCHEMA",
            "--format=INVALID_FORMAT"
        };
        ConfigurationServiceImpl service = createService(args);
        
        // When & Then
        assertThatThrownBy(() -> service.getExportConfig())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Invalid export format 'INVALID_FORMAT'")
                .hasMessageContaining("Supported formats: CSV, JSON, XML, DDL");
    }
    
    @Test
    void shouldValidateConflictingVerboseAndQuietModes() {
        // Given - both verbose and quiet modes specified
        String[] args = {
            "--url=jdbc:oracle:thin:@host:1521:sid",
            "--username=user",
            "--password=pass",
            "--schema=SCHEMA",
            "--verbose",
            "--quiet"
        };
        ConfigurationServiceImpl service = createService(args);
        
        // When & Then
        assertThatThrownBy(() -> service.validateConfiguration())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Cannot specify both verbose and quiet modes simultaneously");
    }
    
    @Test
    void shouldValidateInvalidTimeoutValues() {
        // Given - invalid timeout values
        String[] args = {
            "--url=jdbc:oracle:thin:@host:1521:sid",
            "--username=user",
            "--password=pass",
            "--schema=SCHEMA",
            "--connection-timeout=0"
        };
        ConfigurationServiceImpl service = createService(args);
        
        // When & Then
        assertThatThrownBy(() -> service.getDatabaseConfig())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Value for connection-timeout must be positive");
    }
    
    @Test
    void shouldValidateNonNumericTimeoutValues() {
        // Given - non-numeric timeout values
        String[] args = {
            "--url=jdbc:oracle:thin:@host:1521:sid",
            "--username=user",
            "--password=pass",
            "--schema=SCHEMA",
            "--query-timeout=invalid"
        };
        ConfigurationServiceImpl service = createService(args);
        
        // When & Then
        assertThatThrownBy(() -> service.getDatabaseConfig())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Invalid integer value for query-timeout: 'invalid'");
    }
    
    @Test
    void shouldGenerateComprehensiveHelpText() {
        // Given
        ConfigurationServiceImpl service = createService(new String[]{});
        
        // When
        String helpText = service.getHelpText();
        
        // Then - verify help text contains all required sections
        assertThat(helpText).contains("Database Metadata Analyzer");
        assertThat(helpText).contains("Usage: java -jar vtda.jar [OPTIONS]");
        
        // Database connection options
        assertThat(helpText).contains("Database Connection Options:");
        assertThat(helpText).contains("--url <url>");
        assertThat(helpText).contains("--username <username>");
        assertThat(helpText).contains("--password <password>");
        assertThat(helpText).contains("--schema <schema>");
        
        // Export options
        assertThat(helpText).contains("Export Options:");
        assertThat(helpText).contains("--format <format>");
        assertThat(helpText).contains("--output <file>");
        assertThat(helpText).contains("--include-comments");
        assertThat(helpText).contains("--pretty-print");
        
        // Filter options
        assertThat(helpText).contains("Filter Options:");
        assertThat(helpText).contains("--include <pattern>");
        assertThat(helpText).contains("--exclude <pattern>");
        assertThat(helpText).contains("--case-sensitive");
        
        // Configuration options
        assertThat(helpText).contains("Configuration Options:");
        assertThat(helpText).contains("--config <file>");
        assertThat(helpText).contains("--connection-timeout");
        assertThat(helpText).contains("--query-timeout");
        
        // Output options
        assertThat(helpText).contains("Output Options:");
        assertThat(helpText).contains("-v, --verbose");
        assertThat(helpText).contains("-q, --quiet");
        assertThat(helpText).contains("-h, --help");
        
        // Examples
        assertThat(helpText).contains("Examples:");
        assertThat(helpText).contains("java -jar vtda.jar");
        
        // Configuration file formats
        assertThat(helpText).contains("Configuration File Format (properties):");
        assertThat(helpText).contains("Configuration File Format (YAML):");
    }
    
    @Test
    void shouldUseDefaultValuesWhenArgumentsNotProvided() throws ConfigurationException {
        // Given - minimal required arguments only
        String[] args = {
            "--url=jdbc:oracle:thin:@host:1521:sid",
            "--username=user",
            "--password=pass",
            "--schema=SCHEMA"
        };
        ConfigurationServiceImpl service = createService(args);
        
        // When
        DatabaseConfig dbConfig = service.getDatabaseConfig();
        ExportConfig exportConfig = service.getExportConfig();
        FilterConfig filterConfig = service.getFilterConfig();
        
        // Then - verify default values are used
        assertThat(dbConfig.getConnectionTimeout()).isEqualTo(30); // default
        assertThat(dbConfig.getQueryTimeout()).isEqualTo(300); // default
        
        assertThat(exportConfig.getFormat()).isEqualTo(ExportConfig.ExportFormat.CSV); // default
        assertThat(exportConfig.isUseStandardOutput()).isTrue(); // default when no output specified
        assertThat(exportConfig.isIncludeComments()).isTrue(); // default
        assertThat(exportConfig.isPrettyPrint()).isTrue(); // default
        assertThat(exportConfig.getEncoding()).isEqualTo("UTF-8"); // default
        
        assertThat(filterConfig.getIncludePatterns()).isEmpty(); // default
        assertThat(filterConfig.getExcludePatterns()).isEmpty(); // default
        assertThat(filterConfig.isCaseSensitive()).isFalse(); // default
        assertThat(filterConfig.isUseRegex()).isFalse(); // default
    }
    
    private ConfigurationServiceImpl createService(String[] args) {
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        StandardEnvironment environment = new StandardEnvironment();
        return new ConfigurationServiceImpl(applicationArguments, environment);
    }
}
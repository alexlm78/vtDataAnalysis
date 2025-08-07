package dev.kreaker.vtda.service.impl;

import dev.kreaker.vtda.exception.ConfigurationException;
import dev.kreaker.vtda.model.DatabaseConfig;
import dev.kreaker.vtda.model.ExportConfig;
import dev.kreaker.vtda.model.FilterConfig;
import dev.kreaker.vtda.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;

/**
 * Implementation of ConfigurationService that manages application settings from multiple sources.
 * Supports command-line arguments, configuration files, environment variables, and application properties.
 */
@Service
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {
    
    private final ApplicationArguments applicationArguments;
    private final Environment environment;
    
    // Cached configurations
    private DatabaseConfig databaseConfig;
    private ExportConfig exportConfig;
    private FilterConfig filterConfig;
    private Boolean verboseMode;
    private Boolean quietMode;
    private Boolean helpRequested;
    
    public ConfigurationServiceImpl(ApplicationArguments applicationArguments, Environment environment) {
        this.applicationArguments = applicationArguments;
        this.environment = environment;
    }
    
    @Override
    public DatabaseConfig getDatabaseConfig() throws ConfigurationException {
        if (databaseConfig == null) {
            databaseConfig = buildDatabaseConfig();
        }
        return databaseConfig;
    }
    
    @Override
    public ExportConfig getExportConfig() throws ConfigurationException {
        if (exportConfig == null) {
            exportConfig = buildExportConfig();
        }
        return exportConfig;
    }
    
    @Override
    public FilterConfig getFilterConfig() throws ConfigurationException {
        if (filterConfig == null) {
            filterConfig = buildFilterConfig();
        }
        return filterConfig;
    }
    
    @Override
    public boolean isVerboseMode() {
        if (verboseMode == null) {
            verboseMode = applicationArguments.containsOption("verbose") || 
                         applicationArguments.containsOption("v") ||
                         hasShortOption("v");
        }
        return verboseMode;
    }
    
    @Override
    public boolean isQuietMode() {
        if (quietMode == null) {
            quietMode = applicationArguments.containsOption("quiet") || 
                       applicationArguments.containsOption("q") ||
                       hasShortOption("q");
        }
        return quietMode;
    }
    
    @Override
    public boolean isHelpRequested() {
        if (helpRequested == null) {
            helpRequested = applicationArguments.containsOption("help") || 
                           applicationArguments.containsOption("h") ||
                           hasShortOption("h");
        }
        return helpRequested;
    }
    
    @Override
    public void validateConfiguration() throws ConfigurationException {
        List<String> validationErrors = new ArrayList<>();
        
        try {
            // Validate database configuration
            DatabaseConfig dbConfig = getDatabaseConfig();
            try {
                dbConfig.validate();
            } catch (IllegalStateException e) {
                validationErrors.add("Database configuration error: " + e.getMessage());
            }
            
            // Validate export configuration
            ExportConfig expConfig = getExportConfig();
            try {
                expConfig.validate();
            } catch (IllegalStateException e) {
                validationErrors.add("Export configuration error: " + e.getMessage());
            }
            
            // Validate filter configuration
            FilterConfig filtConfig = getFilterConfig();
            try {
                filtConfig.validate();
            } catch (IllegalStateException e) {
                validationErrors.add("Filter configuration error: " + e.getMessage());
            }
            
            // Check for conflicting options
            if (isVerboseMode() && isQuietMode()) {
                throw new ConfigurationException("Cannot specify both verbose and quiet modes simultaneously");
            }
            
            // Check for required arguments when no config file is provided
            String configFile = getStringArgument("config");
            if (configFile == null) {
                validateRequiredCliArguments(validationErrors);
            }
            
            // If there are validation errors, throw exception with all errors
            if (!validationErrors.isEmpty()) {
                String errorMessage = "Configuration validation failed with " + validationErrors.size() + " error(s):\n" +
                                    String.join("\n", validationErrors.stream()
                                            .map(error -> "  - " + error)
                                            .toArray(String[]::new));
                throw new ConfigurationException(errorMessage);
            }
            
            log.debug("Configuration validation completed successfully");
            
        } catch (ConfigurationException e) {
            throw e; // Re-throw configuration exceptions
        } catch (Exception e) {
            throw new ConfigurationException("Unexpected error during configuration validation: " + e.getMessage(), e);
        }
    }
    
    private void validateRequiredCliArguments(List<String> validationErrors) {
        // Check for required database connection parameters when no config file is provided
        if (getStringArgument("url") == null && 
            environment.getProperty("spring.datasource.url") == null) {
            validationErrors.add("Database URL is required (use --url or provide config file with --config)");
        }
        
        if (getStringArgument("username") == null && 
            environment.getProperty("spring.datasource.username") == null) {
            validationErrors.add("Database username is required (use --username or provide config file with --config)");
        }
        
        if (getStringArgument("password") == null && 
            environment.getProperty("spring.datasource.password") == null) {
            validationErrors.add("Database password is required (use --password or provide config file with --config)");
        }
        
        if (getStringArgument("schema") == null && 
            environment.getProperty("app.database.schema") == null) {
            validationErrors.add("Database schema is required (use --schema or provide config file with --config)");
        }
    }
    
    @Override
    public String getHelpText() {
        return """
                Database Metadata Analyzer - Extract and export Oracle database metadata
                
                Usage: java -jar vtda.jar [OPTIONS]
                
                Database Connection Options:
                  --url <url>              Oracle JDBC URL (required if not in config file)
                  --username <username>    Database username (required if not in config file)
                  --password <password>    Database password (required if not in config file)
                  --schema <schema>        Schema name to analyze (required)
                
                Export Options:
                  --format <format>        Export format: CSV, JSON, XML, DDL (default: CSV)
                  --output <file>          Output file path (default: standard output)
                  --include-comments       Include table and column comments (default: true)
                  --pretty-print           Format output for readability (default: true)
                  --append                 Append to existing file instead of overwriting
                  --encoding <encoding>    Character encoding for output files (default: UTF-8)
                
                Filter Options:
                  --include <pattern>      Include tables matching pattern (wildcards: *, ?)
                  --exclude <pattern>      Exclude tables matching pattern (wildcards: *, ?)
                  --case-sensitive         Use case-sensitive pattern matching
                  --regex                  Use regex patterns instead of wildcards
                
                Configuration Options:
                  --config <file>          Load configuration from properties or YAML file
                  --connection-timeout <s> Database connection timeout in seconds (default: 30)
                  --query-timeout <s>      Query execution timeout in seconds (default: 300)
                
                Output Options:
                  -v, --verbose            Enable verbose output
                  -q, --quiet              Suppress non-error output
                  -h, --help               Show this help message
                
                Examples:
                  # Export all tables from schema to CSV
                  java -jar vtda.jar --url=jdbc:oracle:thin:@host:1521:sid --username=user --password=pass --schema=MYSCHEMA
                
                  # Export specific tables to JSON file
                  java -jar vtda.jar --config=db.properties --schema=MYSCHEMA --include=USER_* --format=JSON --output=metadata.json
                
                  # Generate DDL for filtered tables
                  java -jar vtda.jar --config=db.properties --schema=MYSCHEMA --exclude=TEMP_* --format=DDL --output=schema.sql
                
                Configuration File Format (properties):
                  database.url=jdbc:oracle:thin:@host:1521:sid
                  database.username=myuser
                  database.password=mypass
                  database.schema=MYSCHEMA
                  export.format=JSON
                  export.output=metadata.json
                
                Configuration File Format (YAML):
                  database:
                    url: jdbc:oracle:thin:@host:1521:sid
                    username: myuser
                    password: mypass
                    schema: MYSCHEMA
                  export:
                    format: JSON
                    output: metadata.json
                  filter:
                    include:
                      - USER_*
                      - CUSTOMER_*
                    exclude:
                      - TEMP_*
                """;
    }
    
    private DatabaseConfig buildDatabaseConfig() throws ConfigurationException {
        try {
            // Load configuration file if specified
            Properties configProps;
            try {
                configProps = loadConfigurationFile();
            } catch (ConfigurationException e) {
                throw new ConfigurationException("Failed to build database configuration", e);
            }
            
            // Build database configuration with precedence: CLI args > config file > application.properties > defaults
            String defaultUrl = environment.getProperty("spring.datasource.url", "");
            String defaultUsername = environment.getProperty("spring.datasource.username", "");
            String defaultPassword = environment.getProperty("spring.datasource.password", "");
            String defaultSchemaValue = environment.getProperty("app.database.schema", "");
            int defaultConnectionTimeoutValue = environment.getProperty("app.database.connection-timeout", Integer.class, 30);
            int defaultQueryTimeoutValue = environment.getProperty("app.database.query-timeout", Integer.class, 300);
            
            String url = getConfigValue("url", "database.url", configProps, defaultUrl);
            String username = getConfigValue("username", "database.username", configProps, defaultUsername);
            String password = getConfigValue("password", "database.password", configProps, defaultPassword);
            String schema = getConfigValue("schema", "database.schema", configProps, defaultSchemaValue);
            
            int connectionTimeout = getIntConfigValue("connection-timeout", "database.connection-timeout", 
                                                    configProps, defaultConnectionTimeoutValue);
            int queryTimeout = getIntConfigValue("query-timeout", "database.query-timeout", 
                                               configProps, defaultQueryTimeoutValue);
            
            return DatabaseConfig.builder()
                    .url(url)
                    .username(username)
                    .password(password)
                    .schema(schema)
                    .connectionTimeout(connectionTimeout)
                    .queryTimeout(queryTimeout)
                    .build();
                    
        } catch (ConfigurationException e) {
            throw e; // Re-throw configuration exceptions
        } catch (Exception e) {
            throw new ConfigurationException("Failed to build database configuration: " + e.getMessage(), e);
        }
    }
    
    private ExportConfig buildExportConfig() throws ConfigurationException {
        try {
            Properties configProps = loadConfigurationFile();
            
            // Parse export format
            String formatStr = getConfigValue("format", "export.format", configProps, "CSV");
            ExportConfig.ExportFormat format;
            try {
                format = ExportConfig.ExportFormat.fromString(formatStr);
            } catch (IllegalArgumentException e) {
                throw new ConfigurationException("Invalid export format '" + formatStr + "'. " +
                        "Supported formats: CSV, JSON, XML, DDL", e);
            }
            
            String outputPath = getConfigValue("output", "export.output", configProps, null);
            boolean includeComments = getBooleanConfigValue("include-comments", "export.include-comments", 
                                                           configProps, true);
            boolean prettyPrint = getBooleanConfigValue("pretty-print", "export.pretty-print", 
                                                       configProps, true);
            boolean appendToFile = getBooleanConfigValue("append", "export.append", configProps, false);
            String encoding = getConfigValue("encoding", "export.encoding", configProps, "UTF-8");
            
            return ExportConfig.builder()
                    .format(format)
                    .outputPath(outputPath)
                    .includeComments(includeComments)
                    .prettyPrint(prettyPrint)
                    .useStandardOutput(outputPath == null)
                    .appendToFile(appendToFile)
                    .encoding(encoding)
                    .build();
                    
        } catch (ConfigurationException e) {
            throw e; // Re-throw configuration exceptions
        } catch (Exception e) {
            throw new ConfigurationException("Failed to build export configuration: " + e.getMessage(), e);
        }
    }
    
    private FilterConfig buildFilterConfig() throws ConfigurationException {
        try {
            Properties configProps = loadConfigurationFile();
            
            List<String> includePatterns = getListConfigValue("include", "filter.include", configProps);
            List<String> excludePatterns = getListConfigValue("exclude", "filter.exclude", configProps);
            boolean caseSensitive = getBooleanConfigValue("case-sensitive", "filter.case-sensitive", 
                                                         configProps, false);
            boolean useRegex = getBooleanConfigValue("regex", "filter.regex", configProps, false);
            
            FilterConfig.FilterConfigBuilder builder = FilterConfig.builder()
                    .caseSensitive(caseSensitive)
                    .useRegex(useRegex);
            
            if (includePatterns != null) {
                for (String pattern : includePatterns) {
                    if (pattern != null && !pattern.trim().isEmpty()) {
                        builder.includePattern(pattern.trim());
                    }
                }
            }
            
            if (excludePatterns != null) {
                for (String pattern : excludePatterns) {
                    if (pattern != null && !pattern.trim().isEmpty()) {
                        builder.excludePattern(pattern.trim());
                    }
                }
            }
            
            return builder.build();
            
        } catch (ConfigurationException e) {
            throw e; // Re-throw configuration exceptions
        } catch (Exception e) {
            throw new ConfigurationException("Failed to build filter configuration: " + e.getMessage(), e);
        }
    }
    
    private Properties loadConfigurationFile() throws ConfigurationException {
        String configFile = getStringArgument("config");
        if (configFile == null) {
            return new Properties(); // Return empty properties if no config file specified
        }
        
        try {
            Path configPath = Paths.get(configFile);
            if (!Files.exists(configPath)) {
                throw new ConfigurationException("Configuration file not found: " + configFile);
            }
            
            Properties props = new Properties();
            
            // Determine file type by extension
            String fileName = configPath.getFileName().toString().toLowerCase();
            if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
                // Load YAML file
                loadYamlProperties(configPath, props);
            } else {
                // Load as properties file
                try (InputStream inputStream = Files.newInputStream(configPath)) {
                    props.load(inputStream);
                }
            }
            
            log.debug("Loaded configuration from file: {} (type: {})", configFile, 
                     fileName.endsWith(".yml") || fileName.endsWith(".yaml") ? "YAML" : "Properties");
            return props;
            
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration file: " + configFile, e);
        }
    }
    
    private void loadYamlProperties(Path yamlPath, Properties props) throws ConfigurationException {
        try {
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            List<PropertySource<?>> propertySources = loader.load("config", new FileSystemResource(yamlPath.toFile()));
            
            if (propertySources.isEmpty()) {
                log.warn("No properties found in YAML file: {}", yamlPath);
                return;
            }
            
            // Flatten YAML properties into Properties object
            for (PropertySource<?> propertySource : propertySources) {
                if (propertySource.getSource() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> source = (Map<String, Object>) propertySource.getSource();
                    flattenMap("", source, props);
                }
            }
            
        } catch (IOException e) {
            throw new ConfigurationException("Failed to parse YAML configuration file: " + yamlPath, e);
        }
    }
    
    private void flattenMap(String prefix, Map<String, Object> map, Properties props) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flattenMap(key, nestedMap, props);
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) value;
                // Store list items as indexed properties (Spring Boot YAML style)
                for (int i = 0; i < list.size(); i++) {
                    props.setProperty(key + "[" + i + "]", list.get(i).toString());
                }
            } else if (value != null) {
                props.setProperty(key, value.toString());
            }
        }
    }
    
    private String getConfigValue(String cliOption, String configKey, Properties configProps, String defaultValue) {
        // Precedence: CLI argument > config file > environment > default
        String value = getStringArgument(cliOption);
        if (value != null) {
            return value;
        }
        
        value = configProps.getProperty(configKey);
        if (value != null) {
            return value;
        }
        
        value = environment.getProperty(configKey);
        if (value != null) {
            return value;
        }
        
        return defaultValue;
    }
    
    private int getIntConfigValue(String cliOption, String configKey, Properties configProps, int defaultValue) 
            throws ConfigurationException {
        String value = getConfigValue(cliOption, configKey, configProps, String.valueOf(defaultValue));
        try {
            int intValue = Integer.parseInt(value);
            if (intValue <= 0) {
                throw new ConfigurationException("Value for " + cliOption + " must be positive, got: " + value);
            }
            return intValue;
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid integer value for " + cliOption + ": '" + value + 
                    "'. Expected a positive integer.", e);
        }
    }
    
    private boolean getBooleanConfigValue(String cliOption, String configKey, Properties configProps, boolean defaultValue) {
        String value = getConfigValue(cliOption, configKey, configProps, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value) || applicationArguments.containsOption(cliOption);
    }
    
    private List<String> getListConfigValue(String cliOption, String configKey, Properties configProps) {
        List<String> values = new ArrayList<>();
        
        // Add values from command-line arguments
        List<String> cliValues = applicationArguments.getOptionValues(cliOption);
        if (cliValues != null && !cliValues.isEmpty()) {
            values.addAll(cliValues);
        }
        
        // Add values from config file (comma-separated)
        String configValue = configProps.getProperty(configKey);
        if (configValue != null && !configValue.trim().isEmpty()) {
            String[] parts = configValue.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    values.add(trimmed);
                }
            }
        }
        
        // Also check for indexed properties (from YAML lists)
        int index = 0;
        String indexedKey;
        while ((indexedKey = configProps.getProperty(configKey + "[" + index + "]")) != null) {
            values.add(indexedKey);
            index++;
        }
        
        return values.isEmpty() ? null : values;
    }
    
    private String getStringArgument(String option) {
        List<String> values = applicationArguments.getOptionValues(option);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
    
    /**
     * Helper method to check for short-form options (single character with single dash).
     * Spring Boot's ApplicationArguments doesn't handle single-dash options by default,
     * so we need to check the raw arguments.
     */
    private boolean hasShortOption(String shortOption) {
        String[] sourceArgs = applicationArguments.getSourceArgs();
        String shortForm = "-" + shortOption;
        
        for (String arg : sourceArgs) {
            if (shortForm.equals(arg)) {
                return true;
            }
            // Also check for combined short options like -vh
            if (arg.startsWith("-") && !arg.startsWith("--") && arg.length() > 2) {
                if (arg.substring(1).contains(shortOption)) {
                    return true;
                }
            }
        }
        return false;
    }
}
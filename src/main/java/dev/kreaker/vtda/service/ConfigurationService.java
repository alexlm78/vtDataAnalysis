package dev.kreaker.vtda.service;

import dev.kreaker.vtda.exception.ConfigurationException;
import dev.kreaker.vtda.model.DatabaseConfig;
import dev.kreaker.vtda.model.ExportConfig;
import dev.kreaker.vtda.model.FilterConfig;

/**
 * Service interface for managing application configuration from multiple sources.
 * Handles command-line arguments, configuration files, and environment variables.
 */
public interface ConfigurationService {
    
    /**
     * Gets the database configuration.
     * 
     * @return the database configuration
     * @throws ConfigurationException if configuration is invalid or missing
     */
    DatabaseConfig getDatabaseConfig() throws ConfigurationException;
    
    /**
     * Gets the export configuration.
     * 
     * @return the export configuration
     * @throws ConfigurationException if configuration is invalid
     */
    ExportConfig getExportConfig() throws ConfigurationException;
    
    /**
     * Gets the filter configuration.
     * 
     * @return the filter configuration
     * @throws ConfigurationException if configuration is invalid
     */
    FilterConfig getFilterConfig() throws ConfigurationException;
    
    /**
     * Determines if verbose mode is enabled.
     * 
     * @return true if verbose mode is enabled
     */
    boolean isVerboseMode();
    
    /**
     * Determines if quiet mode is enabled.
     * 
     * @return true if quiet mode is enabled
     */
    boolean isQuietMode();
    
    /**
     * Validates all configuration parameters.
     * 
     * @throws ConfigurationException if any configuration is invalid
     */
    void validateConfiguration() throws ConfigurationException;
    
    /**
     * Gets the help text for command-line usage.
     * 
     * @return formatted help text
     */
    String getHelpText();
    
    /**
     * Determines if help was requested.
     * 
     * @return true if help should be displayed
     */
    boolean isHelpRequested();
}
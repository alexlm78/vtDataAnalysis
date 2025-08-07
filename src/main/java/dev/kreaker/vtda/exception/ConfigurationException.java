package dev.kreaker.vtda.exception;

/**
 * Exception thrown when configuration-related operations fail.
 * This includes invalid command-line arguments, configuration file errors, and validation failures.
 */
public class ConfigurationException extends DatabaseMetadataException {
    
    /**
     * Constructs a new ConfigurationException with the specified message.
     * 
     * @param message the detail message
     */
    public ConfigurationException(String message) {
        super(ErrorCode.CONFIG_ERROR, message);
    }
    
    /**
     * Constructs a new ConfigurationException with the specified message and context.
     * 
     * @param message the detail message
     * @param context additional context information about the configuration error
     */
    public ConfigurationException(String message, String context) {
        super(ErrorCode.CONFIG_ERROR, message, context);
    }
    
    /**
     * Constructs a new ConfigurationException with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ConfigurationException(String message, Throwable cause) {
        super(ErrorCode.CONFIG_ERROR, message, cause);
    }
    
    /**
     * Constructs a new ConfigurationException with the specified message, context, and cause.
     * 
     * @param message the detail message
     * @param context additional context information about the configuration error
     * @param cause the cause of this exception
     */
    public ConfigurationException(String message, String context, Throwable cause) {
        super(ErrorCode.CONFIG_ERROR, message, context, cause);
    }
}
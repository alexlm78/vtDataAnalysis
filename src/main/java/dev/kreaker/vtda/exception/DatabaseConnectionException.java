package dev.kreaker.vtda.exception;

/**
 * Exception thrown when database connection operations fail.
 * This includes connection establishment, authentication, and connection validation failures.
 */
public class DatabaseConnectionException extends DatabaseMetadataException {
    
    /**
     * Constructs a new DatabaseConnectionException with the specified message.
     * 
     * @param message the detail message
     */
    public DatabaseConnectionException(String message) {
        super(ErrorCode.DB_CONNECTION_FAILED, message);
    }
    
    /**
     * Constructs a new DatabaseConnectionException with the specified message and context.
     * 
     * @param message the detail message
     * @param context additional context information about the connection failure
     */
    public DatabaseConnectionException(String message, String context) {
        super(ErrorCode.DB_CONNECTION_FAILED, message, context);
    }
    
    /**
     * Constructs a new DatabaseConnectionException with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(ErrorCode.DB_CONNECTION_FAILED, message, cause);
    }
    
    /**
     * Constructs a new DatabaseConnectionException with the specified message, context, and cause.
     * 
     * @param message the detail message
     * @param context additional context information about the connection failure
     * @param cause the cause of this exception
     */
    public DatabaseConnectionException(String message, String context, Throwable cause) {
        super(ErrorCode.DB_CONNECTION_FAILED, message, context, cause);
    }
}
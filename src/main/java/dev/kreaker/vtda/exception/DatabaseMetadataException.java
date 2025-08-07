package dev.kreaker.vtda.exception;

/**
 * Base exception class for all database metadata analysis operations.
 * Provides error code mapping and contextual information for error handling.
 */
public abstract class DatabaseMetadataException extends Exception {
    
    private final ErrorCode errorCode;
    private final String context;
    
    /**
     * Constructs a new DatabaseMetadataException with the specified error code and message.
     * 
     * @param errorCode the error code associated with this exception
     * @param message the detail message
     */
    protected DatabaseMetadataException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.context = null;
    }
    
    /**
     * Constructs a new DatabaseMetadataException with the specified error code, message, and context.
     * 
     * @param errorCode the error code associated with this exception
     * @param message the detail message
     * @param context additional context information about the error
     */
    protected DatabaseMetadataException(ErrorCode errorCode, String message, String context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context;
    }
    
    /**
     * Constructs a new DatabaseMetadataException with the specified error code, message, and cause.
     * 
     * @param errorCode the error code associated with this exception
     * @param message the detail message
     * @param cause the cause of this exception
     */
    protected DatabaseMetadataException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = null;
    }
    
    /**
     * Constructs a new DatabaseMetadataException with the specified error code, message, context, and cause.
     * 
     * @param errorCode the error code associated with this exception
     * @param message the detail message
     * @param context additional context information about the error
     * @param cause the cause of this exception
     */
    protected DatabaseMetadataException(ErrorCode errorCode, String message, String context, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = context;
    }
    
    /**
     * Gets the error code associated with this exception.
     * 
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the exit code that should be used when this exception causes application termination.
     * 
     * @return the exit code
     */
    public int getExitCode() {
        return errorCode.getExitCode();
    }
    
    /**
     * Gets additional context information about the error, if available.
     * 
     * @return the context information, or null if not provided
     */
    public String getContext() {
        return context;
    }
    
    /**
     * Returns a detailed error message including context information if available.
     * 
     * @return the detailed error message
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage());
        
        if (context != null && !context.trim().isEmpty()) {
            sb.append(" [Context: ").append(context).append("]");
        }
        
        sb.append(" (").append(errorCode.getDescription()).append(")");
        
        return sb.toString();
    }
    
    /**
     * Returns a string representation of this exception including error code and context.
     * 
     * @return string representation of the exception
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getDetailedMessage();
    }
}
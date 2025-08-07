package dev.kreaker.vtda.exception;

/**
 * Exception thrown when export operations fail.
 * This includes failures in formatting data, writing to files, or generating output in various formats.
 */
public class ExportException extends DatabaseMetadataException {
    
    /**
     * Constructs a new ExportException with the specified message.
     * 
     * @param message the detail message
     */
    public ExportException(String message) {
        super(ErrorCode.EXPORT_FAILED, message);
    }
    
    /**
     * Constructs a new ExportException with the specified message and context.
     * 
     * @param message the detail message
     * @param context additional context information about the export failure
     */
    public ExportException(String message, String context) {
        super(ErrorCode.EXPORT_FAILED, message, context);
    }
    
    /**
     * Constructs a new ExportException with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ExportException(String message, Throwable cause) {
        super(ErrorCode.EXPORT_FAILED, message, cause);
    }
    
    /**
     * Constructs a new ExportException with the specified message, context, and cause.
     * 
     * @param message the detail message
     * @param context additional context information about the export failure
     * @param cause the cause of this exception
     */
    public ExportException(String message, String context, Throwable cause) {
        super(ErrorCode.EXPORT_FAILED, message, context, cause);
    }
}
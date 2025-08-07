package dev.kreaker.vtda.exception;

/**
 * Exception thrown when DDL generation operations fail.
 * This includes failures in generating CREATE TABLE statements, handling data types, or formatting DDL output.
 */
public class DDLGenerationException extends DatabaseMetadataException {
    
    /**
     * Constructs a new DDLGenerationException with the specified message.
     * 
     * @param message the detail message
     */
    public DDLGenerationException(String message) {
        super(ErrorCode.DDL_GENERATION_FAILED, message);
    }
    
    /**
     * Constructs a new DDLGenerationException with the specified message and context.
     * 
     * @param message the detail message
     * @param context additional context information about the DDL generation failure
     */
    public DDLGenerationException(String message, String context) {
        super(ErrorCode.DDL_GENERATION_FAILED, message, context);
    }
    
    /**
     * Constructs a new DDLGenerationException with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public DDLGenerationException(String message, Throwable cause) {
        super(ErrorCode.DDL_GENERATION_FAILED, message, cause);
    }
    
    /**
     * Constructs a new DDLGenerationException with the specified message, context, and cause.
     * 
     * @param message the detail message
     * @param context additional context information about the DDL generation failure
     * @param cause the cause of this exception
     */
    public DDLGenerationException(String message, String context, Throwable cause) {
        super(ErrorCode.DDL_GENERATION_FAILED, message, context, cause);
    }
}
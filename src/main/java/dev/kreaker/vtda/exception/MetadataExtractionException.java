package dev.kreaker.vtda.exception;

/**
 * Exception thrown when metadata extraction operations fail.
 * This includes failures in querying database metadata, processing results, or schema validation.
 */
public class MetadataExtractionException extends DatabaseMetadataException {
    
    /**
     * Constructs a new MetadataExtractionException with the specified message.
     * 
     * @param message the detail message
     */
    public MetadataExtractionException(String message) {
        super(ErrorCode.METADATA_EXTRACTION_FAILED, message);
    }
    
    /**
     * Constructs a new MetadataExtractionException with the specified message and context.
     * 
     * @param message the detail message
     * @param context additional context information about the extraction failure
     */
    public MetadataExtractionException(String message, String context) {
        super(ErrorCode.METADATA_EXTRACTION_FAILED, message, context);
    }
    
    /**
     * Constructs a new MetadataExtractionException with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public MetadataExtractionException(String message, Throwable cause) {
        super(ErrorCode.METADATA_EXTRACTION_FAILED, message, cause);
    }
    
    /**
     * Constructs a new MetadataExtractionException with the specified message, context, and cause.
     * 
     * @param message the detail message
     * @param context additional context information about the extraction failure
     * @param cause the cause of this exception
     */
    public MetadataExtractionException(String message, String context, Throwable cause) {
        super(ErrorCode.METADATA_EXTRACTION_FAILED, message, context, cause);
    }
}
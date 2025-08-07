package dev.kreaker.vtda.exception;

/**
 * Exception thrown when file I/O operations fail.
 * This includes failures in reading configuration files, writing output files, or accessing the file system.
 */
public class FileIOException extends DatabaseMetadataException {
    
    private final String filePath;
    
    /**
     * Constructs a new FileIOException with the specified message.
     * 
     * @param message the detail message
     */
    public FileIOException(String message) {
        super(ErrorCode.FILE_IO_ERROR, message);
        this.filePath = null;
    }
    
    /**
     * Constructs a new FileIOException with the specified message and file path.
     * 
     * @param message the detail message
     * @param filePath the path of the file that caused the I/O error
     */
    public FileIOException(String message, String filePath) {
        super(ErrorCode.FILE_IO_ERROR, message, "File: " + filePath);
        this.filePath = filePath;
    }
    
    /**
     * Constructs a new FileIOException with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public FileIOException(String message, Throwable cause) {
        super(ErrorCode.FILE_IO_ERROR, message, cause);
        this.filePath = null;
    }
    
    /**
     * Constructs a new FileIOException with the specified message, file path, and cause.
     * 
     * @param message the detail message
     * @param filePath the path of the file that caused the I/O error
     * @param cause the cause of this exception
     */
    public FileIOException(String message, String filePath, Throwable cause) {
        super(ErrorCode.FILE_IO_ERROR, message, "File: " + filePath, cause);
        this.filePath = filePath;
    }
    
    /**
     * Gets the file path that caused the I/O error, if available.
     * 
     * @return the file path, or null if not provided
     */
    public String getFilePath() {
        return filePath;
    }
}
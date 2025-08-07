package dev.kreaker.vtda.exception;

/**
 * Enumeration of error codes with corresponding exit status codes for the application.
 * Each error code represents a specific type of failure that can occur during execution.
 */
public enum ErrorCode {
    /**
     * Operation completed successfully
     */
    SUCCESS(0, "Operation completed successfully"),
    
    /**
     * Invalid command-line arguments provided
     */
    INVALID_ARGS(1, "Invalid command-line arguments"),
    
    /**
     * Database connection failure
     */
    DB_CONNECTION_FAILED(2, "Database connection failure"),
    
    /**
     * Specified schema does not exist or is not accessible
     */
    SCHEMA_NOT_FOUND(3, "Specified schema does not exist"),
    
    /**
     * Export operation failed
     */
    EXPORT_FAILED(4, "Export operation failed"),
    
    /**
     * Configuration error
     */
    CONFIG_ERROR(5, "Configuration error"),
    
    /**
     * Metadata extraction failed
     */
    METADATA_EXTRACTION_FAILED(6, "Metadata extraction failed"),
    
    /**
     * DDL generation failed
     */
    DDL_GENERATION_FAILED(7, "DDL generation failed"),
    
    /**
     * File I/O operation failed
     */
    FILE_IO_ERROR(8, "File I/O operation failed"),
    
    /**
     * SQL execution error
     */
    SQL_EXECUTION_ERROR(9, "SQL execution error"),
    
    /**
     * Unexpected system error
     */
    UNEXPECTED_ERROR(99, "Unexpected system error");
    
    private final int exitCode;
    private final String description;
    
    ErrorCode(int exitCode, String description) {
        this.exitCode = exitCode;
        this.description = description;
    }
    
    /**
     * Gets the exit code associated with this error
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }
    
    /**
     * Gets the description of this error
     * @return the error description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns a formatted string representation of the error code
     * @return formatted error code string
     */
    @Override
    public String toString() {
        return String.format("%s (exit code: %d) - %s", name(), exitCode, description);
    }
}
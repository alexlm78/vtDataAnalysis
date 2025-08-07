package dev.kreaker.vtda.exception;

/**
 * Exception thrown when SQL execution operations fail.
 * This includes failures in executing metadata queries, handling SQL errors, or processing query results.
 */
public class SqlExecutionException extends DatabaseMetadataException {
    
    private final String sqlStatement;
    
    /**
     * Constructs a new SqlExecutionException with the specified message.
     * 
     * @param message the detail message
     */
    public SqlExecutionException(String message) {
        super(ErrorCode.SQL_EXECUTION_ERROR, message);
        this.sqlStatement = null;
    }
    
    /**
     * Constructs a new SqlExecutionException with the specified message and SQL statement.
     * 
     * @param message the detail message
     * @param sqlStatement the SQL statement that failed to execute
     */
    public SqlExecutionException(String message, String sqlStatement) {
        super(ErrorCode.SQL_EXECUTION_ERROR, message, "SQL: " + sqlStatement);
        this.sqlStatement = sqlStatement;
    }
    
    /**
     * Constructs a new SqlExecutionException with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SqlExecutionException(String message, Throwable cause) {
        super(ErrorCode.SQL_EXECUTION_ERROR, message, cause);
        this.sqlStatement = null;
    }
    
    /**
     * Constructs a new SqlExecutionException with the specified message, SQL statement, and cause.
     * 
     * @param message the detail message
     * @param sqlStatement the SQL statement that failed to execute
     * @param cause the cause of this exception
     */
    public SqlExecutionException(String message, String sqlStatement, Throwable cause) {
        super(ErrorCode.SQL_EXECUTION_ERROR, message, "SQL: " + sqlStatement, cause);
        this.sqlStatement = sqlStatement;
    }
    
    /**
     * Gets the SQL statement that failed to execute, if available.
     * 
     * @return the SQL statement, or null if not provided
     */
    public String getSqlStatement() {
        return sqlStatement;
    }
}
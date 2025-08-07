package dev.kreaker.vtda.exception;

/**
 * Exception thrown when invalid command-line arguments are provided.
 * This is a specialized configuration exception for argument parsing errors.
 */
public class InvalidArgumentException extends DatabaseMetadataException {
    
    private final String argumentName;
    
    /**
     * Constructs a new InvalidArgumentException with the specified message.
     * 
     * @param message the detail message
     */
    public InvalidArgumentException(String message) {
        super(ErrorCode.INVALID_ARGS, message);
        this.argumentName = null;
    }
    
    /**
     * Constructs a new InvalidArgumentException with the specified message and argument name.
     * 
     * @param message the detail message
     * @param argumentName the name of the invalid argument
     */
    public InvalidArgumentException(String message, String argumentName) {
        super(ErrorCode.INVALID_ARGS, message, "Argument: " + argumentName);
        this.argumentName = argumentName;
    }
    
    /**
     * Constructs a new InvalidArgumentException with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public InvalidArgumentException(String message, Throwable cause) {
        super(ErrorCode.INVALID_ARGS, message, cause);
        this.argumentName = null;
    }
    
    /**
     * Constructs a new InvalidArgumentException with the specified message, argument name, and cause.
     * 
     * @param message the detail message
     * @param argumentName the name of the invalid argument
     * @param cause the cause of this exception
     */
    public InvalidArgumentException(String message, String argumentName, Throwable cause) {
        super(ErrorCode.INVALID_ARGS, message, "Argument: " + argumentName, cause);
        this.argumentName = argumentName;
    }
    
    /**
     * Gets the name of the invalid argument, if available.
     * 
     * @return the argument name, or null if not provided
     */
    public String getArgumentName() {
        return argumentName;
    }
}
package dev.kreaker.vtda.exception;

/**
 * Exception thrown when a specified database schema does not exist or is not accessible.
 * This is a specialized exception for schema-related errors during metadata extraction.
 */
public class SchemaNotFoundException extends DatabaseMetadataException {
    
    private final String schemaName;
    
    /**
     * Constructs a new SchemaNotFoundException with the specified schema name.
     * 
     * @param schemaName the name of the schema that was not found
     */
    public SchemaNotFoundException(String schemaName) {
        super(ErrorCode.SCHEMA_NOT_FOUND, "Schema '" + schemaName + "' not found or not accessible");
        this.schemaName = schemaName;
    }
    
    /**
     * Constructs a new SchemaNotFoundException with the specified schema name and context.
     * 
     * @param schemaName the name of the schema that was not found
     * @param context additional context information about the schema access failure
     */
    public SchemaNotFoundException(String schemaName, String context) {
        super(ErrorCode.SCHEMA_NOT_FOUND, "Schema '" + schemaName + "' not found or not accessible", context);
        this.schemaName = schemaName;
    }
    
    /**
     * Constructs a new SchemaNotFoundException with the specified schema name and cause.
     * 
     * @param schemaName the name of the schema that was not found
     * @param cause the cause of this exception
     */
    public SchemaNotFoundException(String schemaName, Throwable cause) {
        super(ErrorCode.SCHEMA_NOT_FOUND, "Schema '" + schemaName + "' not found or not accessible", cause);
        this.schemaName = schemaName;
    }
    
    /**
     * Constructs a new SchemaNotFoundException with the specified schema name, context, and cause.
     * 
     * @param schemaName the name of the schema that was not found
     * @param context additional context information about the schema access failure
     * @param cause the cause of this exception
     */
    public SchemaNotFoundException(String schemaName, String context, Throwable cause) {
        super(ErrorCode.SCHEMA_NOT_FOUND, "Schema '" + schemaName + "' not found or not accessible", context, cause);
        this.schemaName = schemaName;
    }
    
    /**
     * Gets the name of the schema that was not found.
     * 
     * @return the schema name
     */
    public String getSchemaName() {
        return schemaName;
    }
}
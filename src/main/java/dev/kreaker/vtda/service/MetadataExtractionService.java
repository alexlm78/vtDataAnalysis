package dev.kreaker.vtda.service;

import dev.kreaker.vtda.exception.DatabaseConnectionException;
import dev.kreaker.vtda.exception.MetadataExtractionException;
import dev.kreaker.vtda.exception.SchemaNotFoundException;
import dev.kreaker.vtda.model.DatabaseConfig;
import dev.kreaker.vtda.model.FilterConfig;
import dev.kreaker.vtda.model.TableMetadata;

import java.util.List;

/**
 * Service interface for extracting database metadata from Oracle databases.
 * Provides methods for connecting to databases, validating schemas, and extracting
 * comprehensive table and column metadata.
 */
public interface MetadataExtractionService {
    
    /**
     * Extracts table metadata from the specified schema using the provided filter configuration.
     * 
     * @param schema the schema name to extract metadata from
     * @param filters the filter configuration for table selection
     * @return list of table metadata objects matching the filter criteria
     * @throws MetadataExtractionException if metadata extraction fails
     * @throws SchemaNotFoundException if the specified schema does not exist
     * @throws DatabaseConnectionException if database connection fails
     */
    List<TableMetadata> extractTableMetadata(String schema, FilterConfig filters) 
            throws MetadataExtractionException, SchemaNotFoundException, DatabaseConnectionException;
    
    /**
     * Gets a list of all available schemas that the current user can access.
     * 
     * @return list of schema names accessible to the current user
     * @throws DatabaseConnectionException if database connection fails
     * @throws MetadataExtractionException if schema enumeration fails
     */
    List<String> getAvailableSchemas() 
            throws DatabaseConnectionException, MetadataExtractionException;
    
    /**
     * Validates the database connection and ensures the service can connect successfully.
     * This method should be called before attempting metadata extraction operations.
     * 
     * @throws DatabaseConnectionException if connection validation fails
     */
    void validateConnection() throws DatabaseConnectionException;
    
    /**
     * Validates that the specified schema exists and is accessible to the current user.
     * 
     * @param schema the schema name to validate
     * @throws SchemaNotFoundException if the schema does not exist or is not accessible
     * @throws DatabaseConnectionException if database connection fails
     */
    void validateSchema(String schema) throws SchemaNotFoundException, DatabaseConnectionException;
    
    /**
     * Gets the database configuration used by this service.
     * 
     * @return the database configuration
     */
    DatabaseConfig getDatabaseConfig();
    
    /**
     * Closes any open database connections and releases resources.
     * This method should be called when the service is no longer needed.
     */
    void close();
}
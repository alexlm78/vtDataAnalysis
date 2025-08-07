package dev.kreaker.vtda.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Objects;

/**
 * Configuration class for database connection parameters.
 * Contains all necessary information to establish a connection to an Oracle database.
 */
@Data
@Builder
public class DatabaseConfig {
    
    /**
     * The JDBC URL for the Oracle database connection
     */
    private final String url;
    
    /**
     * The username for database authentication
     */
    private final String username;
    
    /**
     * The password for database authentication
     */
    private final String password;
    
    /**
     * The schema name to extract metadata from
     */
    private final String schema;
    
    /**
     * Connection timeout in seconds (default: 30)
     */
    @Builder.Default
    private final int connectionTimeout = 30;
    
    /**
     * Query timeout in seconds (default: 300)
     */
    @Builder.Default
    private final int queryTimeout = 300;
    
    /**
     * Validates the database configuration for completeness and correctness.
     * 
     * @throws IllegalStateException if the configuration is invalid
     */
    public void validate() {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("Database URL cannot be null or empty");
        }
        
        if (!url.toLowerCase().startsWith("jdbc:oracle:")) {
            throw new IllegalStateException("Database URL must be a valid Oracle JDBC URL starting with 'jdbc:oracle:'");
        }
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("Database username cannot be null or empty");
        }
        
        if (password == null) {
            throw new IllegalStateException("Database password cannot be null");
        }
        
        if (schema == null || schema.trim().isEmpty()) {
            throw new IllegalStateException("Database schema cannot be null or empty");
        }
        
        if (connectionTimeout <= 0) {
            throw new IllegalStateException("Connection timeout must be positive");
        }
        
        if (queryTimeout <= 0) {
            throw new IllegalStateException("Query timeout must be positive");
        }
    }
    
    /**
     * Gets the database URL with connection timeout parameter if not already present.
     * 
     * @return URL with timeout parameters
     */
    public String getUrlWithTimeout() {
        if (url.contains("connectTimeout")) {
            return url;
        }
        
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "connectTimeout=" + (connectionTimeout * 1000);
    }
    
    /**
     * Creates a masked version of the configuration for logging purposes.
     * The password is replaced with asterisks.
     * 
     * @return configuration with masked password
     */
    public DatabaseConfig getMaskedConfig() {
        return DatabaseConfig.builder()
                .url(url)
                .username(username)
                .password("***")
                .schema(schema)
                .connectionTimeout(connectionTimeout)
                .queryTimeout(queryTimeout)
                .build();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseConfig that = (DatabaseConfig) o;
        return connectionTimeout == that.connectionTimeout &&
               queryTimeout == that.queryTimeout &&
               Objects.equals(url, that.url) &&
               Objects.equals(username, that.username) &&
               Objects.equals(password, that.password) &&
               Objects.equals(schema, that.schema);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(url, username, password, schema, connectionTimeout, queryTimeout);
    }
    
    @Override
    public String toString() {
        return String.format("DatabaseConfig{url='%s', username='%s', password='***', schema='%s', connectionTimeout=%d, queryTimeout=%d}",
                           url, username, schema, connectionTimeout, queryTimeout);
    }
}
/**
 * Exception hierarchy for the Database Metadata Analyzer application.
 * 
 * <p>This package contains a comprehensive exception hierarchy that provides structured
 * error handling with error codes and exit status mapping. All exceptions extend from
 * the base {@link dev.kreaker.vtda.exception.DatabaseMetadataException} class.</p>
 * 
 * <h2>Exception Hierarchy</h2>
 * <ul>
 *   <li>{@link dev.kreaker.vtda.exception.DatabaseMetadataException} - Base exception class</li>
 *   <li>{@link dev.kreaker.vtda.exception.DatabaseConnectionException} - Database connection failures</li>
 *   <li>{@link dev.kreaker.vtda.exception.MetadataExtractionException} - Metadata extraction failures</li>
 *   <li>{@link dev.kreaker.vtda.exception.SchemaNotFoundException} - Schema not found or inaccessible</li>
 *   <li>{@link dev.kreaker.vtda.exception.ExportException} - Export operation failures</li>
 *   <li>{@link dev.kreaker.vtda.exception.ConfigurationException} - Configuration errors</li>
 *   <li>{@link dev.kreaker.vtda.exception.InvalidArgumentException} - Invalid command-line arguments</li>
 *   <li>{@link dev.kreaker.vtda.exception.DDLGenerationException} - DDL generation failures</li>
 *   <li>{@link dev.kreaker.vtda.exception.SqlExecutionException} - SQL execution errors</li>
 *   <li>{@link dev.kreaker.vtda.exception.FileIOException} - File I/O operation failures</li>
 * </ul>
 * 
 * <h2>Error Codes</h2>
 * <p>Each exception is associated with an {@link dev.kreaker.vtda.exception.ErrorCode} that
 * provides a standardized exit code for the application. This enables proper error handling
 * in batch processing and automation scenarios.</p>
 * 
 * <h2>Usage</h2>
 * <p>All exceptions provide contextual information and support chaining of underlying causes.
 * The base exception class provides methods to retrieve error codes, exit codes, and
 * detailed error messages with context.</p>
 * 
 * @since 1.0
 */
package dev.kreaker.vtda.exception;
package dev.kreaker.vtda.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Specific Exception Types Tests")
class SpecificExceptionsTest {

    @Nested
    @DisplayName("DatabaseConnectionException Tests")
    class DatabaseConnectionExceptionTest {

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessageOnly() {
            DatabaseConnectionException exception = new DatabaseConnectionException("Connection failed");
            
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DB_CONNECTION_FAILED);
            assertThat(exception.getExitCode()).isEqualTo(2);
            assertThat(exception.getMessage()).isEqualTo("Connection failed");
        }

        @Test
        @DisplayName("Should create with message and context")
        void shouldCreateWithMessageAndContext() {
            DatabaseConnectionException exception = new DatabaseConnectionException("Connection failed", "localhost:1521");
            
            assertThat(exception.getContext()).isEqualTo("localhost:1521");
        }
    }

    @Nested
    @DisplayName("SchemaNotFoundException Tests")
    class SchemaNotFoundExceptionTest {

        @Test
        @DisplayName("Should create with schema name")
        void shouldCreateWithSchemaName() {
            SchemaNotFoundException exception = new SchemaNotFoundException("TEST_SCHEMA");
            
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SCHEMA_NOT_FOUND);
            assertThat(exception.getSchemaName()).isEqualTo("TEST_SCHEMA");
            assertThat(exception.getMessage()).isEqualTo("Schema 'TEST_SCHEMA' not found or not accessible");
        }

        @Test
        @DisplayName("Should create with schema name and context")
        void shouldCreateWithSchemaNameAndContext() {
            SchemaNotFoundException exception = new SchemaNotFoundException("TEST_SCHEMA", "User lacks privileges");
            
            assertThat(exception.getSchemaName()).isEqualTo("TEST_SCHEMA");
            assertThat(exception.getContext()).isEqualTo("User lacks privileges");
        }
    }

    @Nested
    @DisplayName("SqlExecutionException Tests")
    class SqlExecutionExceptionTest {

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessageOnly() {
            SqlExecutionException exception = new SqlExecutionException("Query failed");
            
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SQL_EXECUTION_ERROR);
            assertThat(exception.getSqlStatement()).isNull();
        }

        @Test
        @DisplayName("Should create with message and SQL statement")
        void shouldCreateWithMessageAndSqlStatement() {
            String sql = "SELECT * FROM all_tables";
            SqlExecutionException exception = new SqlExecutionException("Query failed", sql);
            
            assertThat(exception.getSqlStatement()).isEqualTo(sql);
            assertThat(exception.getContext()).isEqualTo("SQL: " + sql);
        }
    }

    @Nested
    @DisplayName("FileIOException Tests")
    class FileIOExceptionTest {

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessageOnly() {
            FileIOException exception = new FileIOException("File not found");
            
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FILE_IO_ERROR);
            assertThat(exception.getFilePath()).isNull();
        }

        @Test
        @DisplayName("Should create with message and file path")
        void shouldCreateWithMessageAndFilePath() {
            String filePath = "/path/to/output.csv";
            FileIOException exception = new FileIOException("Cannot write file", filePath);
            
            assertThat(exception.getFilePath()).isEqualTo(filePath);
            assertThat(exception.getContext()).isEqualTo("File: " + filePath);
        }
    }

    @Nested
    @DisplayName("InvalidArgumentException Tests")
    class InvalidArgumentExceptionTest {

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessageOnly() {
            InvalidArgumentException exception = new InvalidArgumentException("Invalid format");
            
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_ARGS);
            assertThat(exception.getArgumentName()).isNull();
        }

        @Test
        @DisplayName("Should create with message and argument name")
        void shouldCreateWithMessageAndArgumentName() {
            InvalidArgumentException exception = new InvalidArgumentException("Invalid format", "--output-format");
            
            assertThat(exception.getArgumentName()).isEqualTo("--output-format");
            assertThat(exception.getContext()).isEqualTo("Argument: --output-format");
        }
    }

    @Nested
    @DisplayName("MetadataExtractionException Tests")
    class MetadataExtractionExceptionTest {

        @Test
        @DisplayName("Should have correct error code")
        void shouldHaveCorrectErrorCode() {
            MetadataExtractionException exception = new MetadataExtractionException("Extraction failed");
            
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.METADATA_EXTRACTION_FAILED);
            assertThat(exception.getExitCode()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("ExportException Tests")
    class ExportExceptionTest {

        @Test
        @DisplayName("Should have correct error code")
        void shouldHaveCorrectErrorCode() {
            ExportException exception = new ExportException("Export failed");
            
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXPORT_FAILED);
            assertThat(exception.getExitCode()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("ConfigurationException Tests")
    class ConfigurationExceptionTest {

        @Test
        @DisplayName("Should have correct error code")
        void shouldHaveCorrectErrorCode() {
            ConfigurationException exception = new ConfigurationException("Config error");
            
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONFIG_ERROR);
            assertThat(exception.getExitCode()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("DDLGenerationException Tests")
    class DDLGenerationExceptionTest {

        @Test
        @DisplayName("Should have correct error code")
        void shouldHaveCorrectErrorCode() {
            DDLGenerationException exception = new DDLGenerationException("DDL generation failed");
            
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DDL_GENERATION_FAILED);
            assertThat(exception.getExitCode()).isEqualTo(7);
        }
    }
}
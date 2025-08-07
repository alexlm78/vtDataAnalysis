package dev.kreaker.vtda.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DatabaseMetadataException Tests")
class DatabaseMetadataExceptionTest {

    // Concrete implementation for testing the abstract base class
    private static class TestException extends DatabaseMetadataException {
        public TestException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }
        
        public TestException(ErrorCode errorCode, String message, String context) {
            super(errorCode, message, context);
        }
        
        public TestException(ErrorCode errorCode, String message, Throwable cause) {
            super(errorCode, message, cause);
        }
        
        public TestException(ErrorCode errorCode, String message, String context, Throwable cause) {
            super(errorCode, message, context, cause);
        }
    }

    @Test
    @DisplayName("Should create exception with error code and message")
    void shouldCreateExceptionWithErrorCodeAndMessage() {
        TestException exception = new TestException(ErrorCode.CONFIG_ERROR, "Test message");
        
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONFIG_ERROR);
        assertThat(exception.getExitCode()).isEqualTo(5);
        assertThat(exception.getMessage()).isEqualTo("Test message");
        assertThat(exception.getContext()).isNull();
    }

    @Test
    @DisplayName("Should create exception with error code, message, and context")
    void shouldCreateExceptionWithErrorCodeMessageAndContext() {
        TestException exception = new TestException(ErrorCode.EXPORT_FAILED, "Export failed", "CSV format");
        
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXPORT_FAILED);
        assertThat(exception.getExitCode()).isEqualTo(4);
        assertThat(exception.getMessage()).isEqualTo("Export failed");
        assertThat(exception.getContext()).isEqualTo("CSV format");
    }

    @Test
    @DisplayName("Should create exception with error code, message, and cause")
    void shouldCreateExceptionWithErrorCodeMessageAndCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        TestException exception = new TestException(ErrorCode.SQL_EXECUTION_ERROR, "SQL failed", cause);
        
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SQL_EXECUTION_ERROR);
        assertThat(exception.getExitCode()).isEqualTo(9);
        assertThat(exception.getMessage()).isEqualTo("SQL failed");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getContext()).isNull();
    }

    @Test
    @DisplayName("Should create exception with all parameters")
    void shouldCreateExceptionWithAllParameters() {
        RuntimeException cause = new RuntimeException("Root cause");
        TestException exception = new TestException(ErrorCode.DDL_GENERATION_FAILED, "DDL failed", "Table: users", cause);
        
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DDL_GENERATION_FAILED);
        assertThat(exception.getExitCode()).isEqualTo(7);
        assertThat(exception.getMessage()).isEqualTo("DDL failed");
        assertThat(exception.getContext()).isEqualTo("Table: users");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Should generate detailed message without context")
    void shouldGenerateDetailedMessageWithoutContext() {
        TestException exception = new TestException(ErrorCode.METADATA_EXTRACTION_FAILED, "Extraction failed");
        
        String detailedMessage = exception.getDetailedMessage();
        assertThat(detailedMessage).isEqualTo("Extraction failed (Metadata extraction failed)");
    }

    @Test
    @DisplayName("Should generate detailed message with context")
    void shouldGenerateDetailedMessageWithContext() {
        TestException exception = new TestException(ErrorCode.FILE_IO_ERROR, "Cannot write file", "/path/to/file.csv");
        
        String detailedMessage = exception.getDetailedMessage();
        assertThat(detailedMessage).isEqualTo("Cannot write file [Context: /path/to/file.csv] (File I/O operation failed)");
    }

    @Test
    @DisplayName("Should generate detailed message with empty context")
    void shouldGenerateDetailedMessageWithEmptyContext() {
        TestException exception = new TestException(ErrorCode.CONFIG_ERROR, "Config invalid", "");
        
        String detailedMessage = exception.getDetailedMessage();
        assertThat(detailedMessage).isEqualTo("Config invalid (Configuration error)");
    }

    @Test
    @DisplayName("Should generate detailed message with whitespace-only context")
    void shouldGenerateDetailedMessageWithWhitespaceOnlyContext() {
        TestException exception = new TestException(ErrorCode.CONFIG_ERROR, "Config invalid", "   ");
        
        String detailedMessage = exception.getDetailedMessage();
        assertThat(detailedMessage).isEqualTo("Config invalid (Configuration error)");
    }

    @Test
    @DisplayName("Should format toString correctly")
    void shouldFormatToStringCorrectly() {
        TestException exception = new TestException(ErrorCode.DB_CONNECTION_FAILED, "Connection failed", "localhost:1521");
        
        String result = exception.toString();
        assertThat(result).startsWith("TestException: ");
        assertThat(result).contains("Connection failed");
        assertThat(result).contains("[Context: localhost:1521]");
        assertThat(result).contains("Database connection failure");
    }
}
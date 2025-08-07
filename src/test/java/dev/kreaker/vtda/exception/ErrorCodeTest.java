package dev.kreaker.vtda.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorCode Tests")
class ErrorCodeTest {

    @Test
    @DisplayName("Should have correct exit codes for all error types")
    void shouldHaveCorrectExitCodes() {
        assertThat(ErrorCode.SUCCESS.getExitCode()).isEqualTo(0);
        assertThat(ErrorCode.INVALID_ARGS.getExitCode()).isEqualTo(1);
        assertThat(ErrorCode.DB_CONNECTION_FAILED.getExitCode()).isEqualTo(2);
        assertThat(ErrorCode.SCHEMA_NOT_FOUND.getExitCode()).isEqualTo(3);
        assertThat(ErrorCode.EXPORT_FAILED.getExitCode()).isEqualTo(4);
        assertThat(ErrorCode.CONFIG_ERROR.getExitCode()).isEqualTo(5);
        assertThat(ErrorCode.METADATA_EXTRACTION_FAILED.getExitCode()).isEqualTo(6);
        assertThat(ErrorCode.DDL_GENERATION_FAILED.getExitCode()).isEqualTo(7);
        assertThat(ErrorCode.FILE_IO_ERROR.getExitCode()).isEqualTo(8);
        assertThat(ErrorCode.SQL_EXECUTION_ERROR.getExitCode()).isEqualTo(9);
        assertThat(ErrorCode.UNEXPECTED_ERROR.getExitCode()).isEqualTo(99);
    }

    @Test
    @DisplayName("Should have meaningful descriptions for all error types")
    void shouldHaveMeaningfulDescriptions() {
        assertThat(ErrorCode.SUCCESS.getDescription()).isEqualTo("Operation completed successfully");
        assertThat(ErrorCode.INVALID_ARGS.getDescription()).isEqualTo("Invalid command-line arguments");
        assertThat(ErrorCode.DB_CONNECTION_FAILED.getDescription()).isEqualTo("Database connection failure");
        assertThat(ErrorCode.SCHEMA_NOT_FOUND.getDescription()).isEqualTo("Specified schema does not exist");
        assertThat(ErrorCode.EXPORT_FAILED.getDescription()).isEqualTo("Export operation failed");
        assertThat(ErrorCode.CONFIG_ERROR.getDescription()).isEqualTo("Configuration error");
        assertThat(ErrorCode.METADATA_EXTRACTION_FAILED.getDescription()).isEqualTo("Metadata extraction failed");
        assertThat(ErrorCode.DDL_GENERATION_FAILED.getDescription()).isEqualTo("DDL generation failed");
        assertThat(ErrorCode.FILE_IO_ERROR.getDescription()).isEqualTo("File I/O operation failed");
        assertThat(ErrorCode.SQL_EXECUTION_ERROR.getDescription()).isEqualTo("SQL execution error");
        assertThat(ErrorCode.UNEXPECTED_ERROR.getDescription()).isEqualTo("Unexpected system error");
    }

    @Test
    @DisplayName("Should format toString correctly")
    void shouldFormatToStringCorrectly() {
        String result = ErrorCode.DB_CONNECTION_FAILED.toString();
        assertThat(result).contains("DB_CONNECTION_FAILED");
        assertThat(result).contains("exit code: 2");
        assertThat(result).contains("Database connection failure");
    }

    @Test
    @DisplayName("Should have unique exit codes for all error types except success")
    void shouldHaveUniqueExitCodes() {
        ErrorCode[] errorCodes = ErrorCode.values();
        
        // Check that all non-success error codes are unique
        for (int i = 0; i < errorCodes.length; i++) {
            for (int j = i + 1; j < errorCodes.length; j++) {
                if (errorCodes[i] != ErrorCode.SUCCESS && errorCodes[j] != ErrorCode.SUCCESS) {
                    assertThat(errorCodes[i].getExitCode())
                        .as("Exit codes should be unique: %s and %s", errorCodes[i], errorCodes[j])
                        .isNotEqualTo(errorCodes[j].getExitCode());
                }
            }
        }
    }
}
package dev.kreaker.vtda.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class ExportConfigTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should create export configuration with default values")
    void shouldCreateConfigWithDefaults() {
        ExportConfig config = ExportConfig.builder().build();

        assertThat(config.getFormat()).isEqualTo(ExportConfig.ExportFormat.CSV);
        assertThat(config.getOutputPath()).isNull();
        assertThat(config.isIncludeComments()).isTrue();
        assertThat(config.isPrettyPrint()).isTrue();
        assertThat(config.isUseStandardOutput()).isFalse();
        assertThat(config.isAppendToFile()).isFalse();
        assertThat(config.getEncoding()).isEqualTo("UTF-8");
    }

    @Test
    @DisplayName("Should create configuration with custom values")
    void shouldCreateConfigWithCustomValues() {
        ExportConfig config = ExportConfig.builder()
                .format(ExportConfig.ExportFormat.JSON)
                .outputPath("/tmp/output.json")
                .includeComments(false)
                .prettyPrint(false)
                .useStandardOutput(true)
                .appendToFile(true)
                .encoding("ISO-8859-1")
                .build();

        assertThat(config.getFormat()).isEqualTo(ExportConfig.ExportFormat.JSON);
        assertThat(config.getOutputPath()).isEqualTo("/tmp/output.json");
        assertThat(config.isIncludeComments()).isFalse();
        assertThat(config.isPrettyPrint()).isFalse();
        assertThat(config.isUseStandardOutput()).isTrue();
        assertThat(config.isAppendToFile()).isTrue();
        assertThat(config.getEncoding()).isEqualTo("ISO-8859-1");
    }

    @Test
    @DisplayName("Should validate configuration successfully for valid inputs")
    void shouldValidateSuccessfully() {
        ExportConfig config = ExportConfig.builder()
                .format(ExportConfig.ExportFormat.CSV)
                .outputPath(tempDir.resolve("output.csv").toString())
                .build();

        assertThatCode(config::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception for empty output path")
    void shouldThrowExceptionForEmptyOutputPath() {
        ExportConfig config = ExportConfig.builder()
                .outputPath("")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Output path cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception for null encoding")
    void shouldThrowExceptionForNullEncoding() {
        ExportConfig config = ExportConfig.builder()
                .encoding(null)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Encoding cannot be null or empty");
    }

    @Test
    @DisplayName("Should determine file output correctly")
    void shouldDetermineFileOutput() {
        ExportConfig fileConfig = ExportConfig.builder()
                .outputPath("/tmp/output.csv")
                .useStandardOutput(false)
                .build();

        ExportConfig stdoutConfig = ExportConfig.builder()
                .useStandardOutput(true)
                .build();

        ExportConfig nullPathConfig = ExportConfig.builder()
                .outputPath(null)
                .build();

        assertThat(fileConfig.isFileOutput()).isTrue();
        assertThat(stdoutConfig.isFileOutput()).isFalse();
        assertThat(nullPathConfig.isFileOutput()).isFalse();
    }

    @Test
    @DisplayName("Should get correct file extensions for different formats")
    void shouldGetCorrectFileExtensions() {
        assertThat(ExportConfig.builder().format(ExportConfig.ExportFormat.CSV).build().getFileExtension()).isEqualTo(".csv");
        assertThat(ExportConfig.builder().format(ExportConfig.ExportFormat.JSON).build().getFileExtension()).isEqualTo(".json");
        assertThat(ExportConfig.builder().format(ExportConfig.ExportFormat.XML).build().getFileExtension()).isEqualTo(".xml");
        assertThat(ExportConfig.builder().format(ExportConfig.ExportFormat.DDL).build().getFileExtension()).isEqualTo(".sql");
    }

    @Test
    @DisplayName("Should add file extension when not present")
    void shouldAddFileExtensionWhenNotPresent() {
        ExportConfig config = ExportConfig.builder()
                .format(ExportConfig.ExportFormat.JSON)
                .outputPath("/tmp/output")
                .build();

        assertThat(config.getOutputPathWithExtension()).isEqualTo("/tmp/output.json");
    }

    @Test
    @DisplayName("Should not add file extension when already present")
    void shouldNotAddFileExtensionWhenPresent() {
        ExportConfig config = ExportConfig.builder()
                .format(ExportConfig.ExportFormat.JSON)
                .outputPath("/tmp/output.json")
                .build();

        assertThat(config.getOutputPathWithExtension()).isEqualTo("/tmp/output.json");
    }

    @Test
    @DisplayName("Should return null for output path when using standard output")
    void shouldReturnNullForStandardOutput() {
        ExportConfig config = ExportConfig.builder()
                .useStandardOutput(true)
                .build();

        assertThat(config.getOutputPathWithExtension()).isNull();
    }

    @Test
    @DisplayName("Should create configuration for standard output")
    void shouldCreateStandardOutputConfig() {
        ExportConfig config = ExportConfig.forStandardOutput(ExportConfig.ExportFormat.JSON);

        assertThat(config.getFormat()).isEqualTo(ExportConfig.ExportFormat.JSON);
        assertThat(config.isUseStandardOutput()).isTrue();
        assertThat(config.getOutputPath()).isNull();
    }

    @Test
    @DisplayName("Should create configuration for file output")
    void shouldCreateFileOutputConfig() {
        ExportConfig config = ExportConfig.forFileOutput(ExportConfig.ExportFormat.XML, "/tmp/output.xml");

        assertThat(config.getFormat()).isEqualTo(ExportConfig.ExportFormat.XML);
        assertThat(config.getOutputPath()).isEqualTo("/tmp/output.xml");
        assertThat(config.isUseStandardOutput()).isFalse();
    }

    @Test
    @DisplayName("Should parse export format from string correctly")
    void shouldParseExportFormatFromString() {
        assertThat(ExportConfig.ExportFormat.fromString("csv")).isEqualTo(ExportConfig.ExportFormat.CSV);
        assertThat(ExportConfig.ExportFormat.fromString("CSV")).isEqualTo(ExportConfig.ExportFormat.CSV);
        assertThat(ExportConfig.ExportFormat.fromString("json")).isEqualTo(ExportConfig.ExportFormat.JSON);
        assertThat(ExportConfig.ExportFormat.fromString("JSON")).isEqualTo(ExportConfig.ExportFormat.JSON);
        assertThat(ExportConfig.ExportFormat.fromString("xml")).isEqualTo(ExportConfig.ExportFormat.XML);
        assertThat(ExportConfig.ExportFormat.fromString("XML")).isEqualTo(ExportConfig.ExportFormat.XML);
        assertThat(ExportConfig.ExportFormat.fromString("ddl")).isEqualTo(ExportConfig.ExportFormat.DDL);
        assertThat(ExportConfig.ExportFormat.fromString("DDL")).isEqualTo(ExportConfig.ExportFormat.DDL);
        assertThat(ExportConfig.ExportFormat.fromString("sql")).isEqualTo(ExportConfig.ExportFormat.DDL);
        assertThat(ExportConfig.ExportFormat.fromString("SQL")).isEqualTo(ExportConfig.ExportFormat.DDL);
    }

    @Test
    @DisplayName("Should return default format for null or empty string")
    void shouldReturnDefaultFormatForNullOrEmpty() {
        assertThat(ExportConfig.ExportFormat.fromString(null)).isEqualTo(ExportConfig.ExportFormat.CSV);
        assertThat(ExportConfig.ExportFormat.fromString("")).isEqualTo(ExportConfig.ExportFormat.CSV);
        assertThat(ExportConfig.ExportFormat.fromString("   ")).isEqualTo(ExportConfig.ExportFormat.CSV);
    }

    @Test
    @DisplayName("Should throw exception for unsupported format string")
    void shouldThrowExceptionForUnsupportedFormat() {
        assertThatThrownBy(() -> ExportConfig.ExportFormat.fromString("unsupported"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported export format: unsupported");
    }

    @Test
    @DisplayName("Should provide format descriptions")
    void shouldProvideFormatDescriptions() {
        assertThat(ExportConfig.ExportFormat.CSV.getDescription()).isEqualTo("Comma-Separated Values");
        assertThat(ExportConfig.ExportFormat.JSON.getDescription()).isEqualTo("JavaScript Object Notation");
        assertThat(ExportConfig.ExportFormat.XML.getDescription()).isEqualTo("Extensible Markup Language");
        assertThat(ExportConfig.ExportFormat.DDL.getDescription()).isEqualTo("Data Definition Language");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCode() {
        ExportConfig config1 = ExportConfig.builder()
                .format(ExportConfig.ExportFormat.JSON)
                .outputPath("/tmp/output.json")
                .includeComments(true)
                .build();

        ExportConfig config2 = ExportConfig.builder()
                .format(ExportConfig.ExportFormat.JSON)
                .outputPath("/tmp/output.json")
                .includeComments(true)
                .build();

        ExportConfig config3 = ExportConfig.builder()
                .format(ExportConfig.ExportFormat.CSV)
                .outputPath("/tmp/output.json")
                .includeComments(true)
                .build();

        assertThat(config1).isEqualTo(config2);
        assertThat(config1).isNotEqualTo(config3);
        assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
    }

    @Test
    @DisplayName("Should provide meaningful toString representation")
    void shouldProvideToString() {
        ExportConfig config = ExportConfig.builder()
                .format(ExportConfig.ExportFormat.JSON)
                .outputPath("/tmp/output.json")
                .includeComments(false)
                .build();

        String toString = config.toString();
        
        assertThat(toString).contains("ExportConfig");
        assertThat(toString).contains("JSON");
        assertThat(toString).contains("/tmp/output.json");
        assertThat(toString).contains("includeComments=false");
    }
}
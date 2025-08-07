package dev.kreaker.vtda.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Configuration class for export operations including output format and file settings.
 * Defines how metadata should be exported and where the output should be written.
 */
@Data
@Builder
public class ExportConfig {
    
    /**
     * The export format to use for output
     */
    @Builder.Default
    private final ExportFormat format = ExportFormat.CSV;
    
    /**
     * The output file path, null for standard output
     */
    private final String outputPath;
    
    /**
     * Whether to include table and column comments in the export
     */
    @Builder.Default
    private final boolean includeComments = true;
    
    /**
     * Whether to format output for readability (pretty print)
     */
    @Builder.Default
    private final boolean prettyPrint = true;
    
    /**
     * Whether to write output to standard output instead of a file
     */
    @Builder.Default
    private final boolean useStandardOutput = false;
    
    /**
     * Whether to append to existing file instead of overwriting
     */
    @Builder.Default
    private final boolean appendToFile = false;
    
    /**
     * Character encoding for output files (default: UTF-8)
     */
    @Builder.Default
    private final String encoding = "UTF-8";
    
    /**
     * Enumeration of supported export formats
     */
    public enum ExportFormat {
        CSV("csv", "Comma-Separated Values"),
        JSON("json", "JavaScript Object Notation"),
        XML("xml", "Extensible Markup Language"),
        DDL("sql", "Data Definition Language");
        
        private final String fileExtension;
        private final String description;
        
        ExportFormat(String fileExtension, String description) {
            this.fileExtension = fileExtension;
            this.description = description;
        }
        
        public String getFileExtension() {
            return fileExtension;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * Parses a string to determine the export format.
         * 
         * @param formatString the format string (case-insensitive)
         * @return the corresponding ExportFormat
         * @throws IllegalArgumentException if the format is not supported
         */
        public static ExportFormat fromString(String formatString) {
            if (formatString == null || formatString.trim().isEmpty()) {
                return CSV; // Default format
            }
            
            String normalized = formatString.trim().toUpperCase();
            return switch (normalized) {
                case "CSV" -> CSV;
                case "JSON" -> JSON;
                case "XML" -> XML;
                case "DDL", "SQL" -> DDL;
                default -> throw new IllegalArgumentException("Unsupported export format: " + formatString);
            };
        }
    }
    
    /**
     * Validates the export configuration for completeness and correctness.
     * 
     * @throws IllegalStateException if the configuration is invalid
     */
    public void validate() {
        if (format == null) {
            throw new IllegalStateException("Export format cannot be null");
        }
        
        if (outputPath != null && outputPath.trim().isEmpty()) {
            throw new IllegalStateException("Output path cannot be empty (use null for standard output)");
        }
        
        if (encoding == null || encoding.trim().isEmpty()) {
            throw new IllegalStateException("Encoding cannot be null or empty");
        }
        
        // Validate output path if specified
        if (outputPath != null && !useStandardOutput) {
            try {
                Path path = Paths.get(outputPath);
                Path parent = path.getParent();
                if (parent != null && !parent.toFile().exists()) {
                    throw new IllegalStateException("Output directory does not exist: " + parent);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Invalid output path: " + outputPath, e);
            }
        }
    }
    
    /**
     * Determines if output should be written to a file.
     * 
     * @return true if output should be written to a file
     */
    public boolean isFileOutput() {
        return !useStandardOutput && outputPath != null;
    }
    
    /**
     * Gets the suggested file extension for the current format.
     * 
     * @return file extension including the dot (e.g., ".csv")
     */
    public String getFileExtension() {
        return "." + format.getFileExtension();
    }
    
    /**
     * Gets the output path with the appropriate file extension if not already present.
     * 
     * @return output path with correct extension
     */
    public String getOutputPathWithExtension() {
        if (outputPath == null || useStandardOutput) {
            return null;
        }
        
        String extension = getFileExtension();
        if (outputPath.toLowerCase().endsWith(extension)) {
            return outputPath;
        }
        
        return outputPath + extension;
    }
    
    /**
     * Creates a configuration for standard output with the specified format.
     * 
     * @param format the export format to use
     * @return configuration for standard output
     */
    public static ExportConfig forStandardOutput(ExportFormat format) {
        return ExportConfig.builder()
                .format(format)
                .useStandardOutput(true)
                .outputPath(null)
                .build();
    }
    
    /**
     * Creates a configuration for file output with the specified format and path.
     * 
     * @param format the export format to use
     * @param outputPath the file path for output
     * @return configuration for file output
     */
    public static ExportConfig forFileOutput(ExportFormat format, String outputPath) {
        return ExportConfig.builder()
                .format(format)
                .outputPath(outputPath)
                .useStandardOutput(false)
                .build();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportConfig that = (ExportConfig) o;
        return includeComments == that.includeComments &&
               prettyPrint == that.prettyPrint &&
               useStandardOutput == that.useStandardOutput &&
               appendToFile == that.appendToFile &&
               format == that.format &&
               Objects.equals(outputPath, that.outputPath) &&
               Objects.equals(encoding, that.encoding);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(format, outputPath, includeComments, prettyPrint, useStandardOutput, appendToFile, encoding);
    }
    
    @Override
    public String toString() {
        return String.format("ExportConfig{format=%s, outputPath='%s', includeComments=%s, prettyPrint=%s, useStandardOutput=%s}",
                           format, outputPath, includeComments, prettyPrint, useStandardOutput);
    }
}
package dev.kreaker.vtda.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Configuration class for table filtering and pattern matching.
 * Defines which tables should be included or excluded from metadata extraction.
 */
@Data
@Builder
public class FilterConfig {
    
    /**
     * List of patterns for tables to include (wildcards supported)
     */
    @Singular
    private final List<String> includePatterns;
    
    /**
     * List of patterns for tables to exclude (wildcards supported)
     */
    @Singular
    private final List<String> excludePatterns;
    
    /**
     * Whether pattern matching should be case-sensitive
     */
    @Builder.Default
    private final boolean caseSensitive = false;
    
    /**
     * Whether to use regex patterns instead of simple wildcards
     */
    @Builder.Default
    private final boolean useRegex = false;
    
    /**
     * Validates the filter configuration for correctness.
     * 
     * @throws IllegalStateException if the configuration is invalid
     */
    public void validate() {
        // Validate include patterns
        if (includePatterns != null) {
            for (String pattern : includePatterns) {
                validatePattern(pattern, "include");
            }
        }
        
        // Validate exclude patterns
        if (excludePatterns != null) {
            for (String pattern : excludePatterns) {
                validatePattern(pattern, "exclude");
            }
        }
    }
    
    private void validatePattern(String pattern, String type) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalStateException(type + " pattern cannot be null or empty");
        }
        
        if (useRegex) {
            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalStateException("Invalid regex " + type + " pattern: " + pattern, e);
            }
        }
    }
    
    /**
     * Determines if a table name matches the filter criteria.
     * 
     * @param tableName the table name to test
     * @return true if the table should be included based on the filter rules
     */
    public boolean matches(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return false;
        }
        
        // If exclude patterns are specified and table matches any, exclude it
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            for (String excludePattern : excludePatterns) {
                if (matchesPattern(tableName, excludePattern)) {
                    return false;
                }
            }
        }
        
        // If include patterns are specified, table must match at least one
        if (includePatterns != null && !includePatterns.isEmpty()) {
            for (String includePattern : includePatterns) {
                if (matchesPattern(tableName, includePattern)) {
                    return true;
                }
            }
            return false; // No include pattern matched
        }
        
        // If no include patterns specified, include by default (unless excluded above)
        return true;
    }
    
    private boolean matchesPattern(String tableName, String pattern) {
        if (useRegex) {
            if (caseSensitive) {
                return Pattern.matches(pattern, tableName);
            } else {
                // For case-insensitive regex, use Pattern.CASE_INSENSITIVE flag
                return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(tableName).matches();
            }
        } else {
            String testPattern = caseSensitive ? pattern : pattern.toUpperCase();
            String testName = caseSensitive ? tableName : tableName.toUpperCase();
            return matchesWildcard(testName, testPattern);
        }
    }
    
    private boolean matchesWildcard(String text, String pattern) {
        // Convert wildcard pattern to regex
        String regexPattern = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        
        return Pattern.matches(regexPattern, text);
    }
    
    /**
     * Determines if any filters are configured.
     * 
     * @return true if include or exclude patterns are specified
     */
    public boolean hasFilters() {
        return (includePatterns != null && !includePatterns.isEmpty()) ||
               (excludePatterns != null && !excludePatterns.isEmpty());
    }
    
    /**
     * Gets the total number of filter patterns configured.
     * 
     * @return total count of include and exclude patterns
     */
    public int getPatternCount() {
        int count = 0;
        if (includePatterns != null) {
            count += includePatterns.size();
        }
        if (excludePatterns != null) {
            count += excludePatterns.size();
        }
        return count;
    }
    
    /**
     * Creates a filter configuration that includes all tables.
     * 
     * @return configuration with no filters
     */
    public static FilterConfig includeAll() {
        return FilterConfig.builder().build();
    }
    
    /**
     * Creates a filter configuration with a single include pattern.
     * 
     * @param pattern the pattern to include
     * @param caseSensitive whether matching should be case-sensitive
     * @return configuration with the specified include pattern
     */
    public static FilterConfig includePattern(String pattern, boolean caseSensitive) {
        return FilterConfig.builder()
                .includePattern(pattern)
                .caseSensitive(caseSensitive)
                .build();
    }
    
    /**
     * Creates a filter configuration with a single exclude pattern.
     * 
     * @param pattern the pattern to exclude
     * @param caseSensitive whether matching should be case-sensitive
     * @return configuration with the specified exclude pattern
     */
    public static FilterConfig excludePattern(String pattern, boolean caseSensitive) {
        return FilterConfig.builder()
                .excludePattern(pattern)
                .caseSensitive(caseSensitive)
                .build();
    }
    
    /**
     * Creates a filter configuration for system tables exclusion.
     * 
     * @return configuration that excludes common Oracle system tables
     */
    public static FilterConfig excludeSystemTables() {
        return FilterConfig.builder()
                .excludePattern("SYS_*")
                .excludePattern("SYSTEM_*")
                .excludePattern("APEX_*")
                .excludePattern("FLOWS_*")
                .excludePattern("MDSYS_*")
                .excludePattern("CTXSYS_*")
                .excludePattern("XDB_*")
                .excludePattern("WMSYS_*")
                .caseSensitive(false)
                .build();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterConfig that = (FilterConfig) o;
        return caseSensitive == that.caseSensitive &&
               useRegex == that.useRegex &&
               Objects.equals(includePatterns, that.includePatterns) &&
               Objects.equals(excludePatterns, that.excludePatterns);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(includePatterns, excludePatterns, caseSensitive, useRegex);
    }
    
    @Override
    public String toString() {
        return String.format("FilterConfig{includePatterns=%s, excludePatterns=%s, caseSensitive=%s, useRegex=%s}",
                           includePatterns, excludePatterns, caseSensitive, useRegex);
    }
}
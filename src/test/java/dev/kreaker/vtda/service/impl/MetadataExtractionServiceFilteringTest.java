package dev.kreaker.vtda.service.impl;

import dev.kreaker.vtda.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for table filtering functionality using FilterConfig.
 * Tests various filtering scenarios including wildcard patterns, case sensitivity, and include/exclude logic.
 * This test focuses on the FilterConfig logic rather than the full service integration.
 */
class MetadataExtractionServiceFilteringTest {
    
    @Test
    @DisplayName("Should include all tables when no filters are configured")
    void shouldIncludeAllTablesWithNoFilters() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER_ACCOUNTS", "CUSTOMER_ORDERS", "PRODUCT_CATALOG", "SYSTEM_LOG");
        FilterConfig filters = FilterConfig.includeAll();
        
        // Act
        List<String> result = filterTableNames(tableNames, filters);
        
        // Assert
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly("USER_ACCOUNTS", "CUSTOMER_ORDERS", "PRODUCT_CATALOG", "SYSTEM_LOG");
    }
    
    @Test
    @DisplayName("Should filter tables using include patterns")
    void shouldFilterTablesWithIncludePatterns() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER_ACCOUNTS", "USER_PROFILES", "CUSTOMER_ORDERS", "PRODUCT_CATALOG");
        FilterConfig filters = FilterConfig.builder()
                .includePattern("USER_*")
                .build();
        
        // Act
        List<String> result = filterTableNames(tableNames, filters);
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("USER_ACCOUNTS", "USER_PROFILES");
    }
    
    @Test
    @DisplayName("Should filter tables using exclude patterns")
    void shouldFilterTablesWithExcludePatterns() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER_ACCOUNTS", "CUSTOMER_ORDERS", "SYSTEM_LOG", "TEMP_DATA");
        FilterConfig filters = FilterConfig.builder()
                .excludePattern("SYSTEM_*")
                .excludePattern("TEMP_*")
                .build();
        
        // Act
        List<String> result = filterTableNames(tableNames, filters);
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("USER_ACCOUNTS", "CUSTOMER_ORDERS");
    }
    
    @Test
    @DisplayName("Should apply exclude patterns before include patterns")
    void shouldApplyExcludeBeforeInclude() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER_ACCOUNTS", "USER_TEMP", "CUSTOMER_ORDERS", "CUSTOMER_TEMP");
        FilterConfig filters = FilterConfig.builder()
                .includePattern("USER_*")
                .includePattern("CUSTOMER_*")
                .excludePattern("*_TEMP")
                .build();
        
        // Act
        List<String> result = filterTableNames(tableNames, filters);
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("USER_ACCOUNTS", "CUSTOMER_ORDERS");
    }
    
    @Test
    @DisplayName("Should handle case-sensitive filtering")
    void shouldHandleCaseSensitiveFiltering() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER_ACCOUNTS", "user_profiles", "CUSTOMER_ORDERS");
        FilterConfig caseSensitiveFilters = FilterConfig.builder()
                .includePattern("USER_*")
                .caseSensitive(true)
                .build();
        
        // Act
        List<String> result = filterTableNames(tableNames, caseSensitiveFilters);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("USER_ACCOUNTS");
    }
    
    @Test
    @DisplayName("Should handle case-insensitive filtering")
    void shouldHandleCaseInsensitiveFiltering() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER_ACCOUNTS", "user_profiles", "CUSTOMER_ORDERS");
        FilterConfig caseInsensitiveFilters = FilterConfig.builder()
                .includePattern("USER_*")
                .caseSensitive(false)
                .build();
        
        // Act
        List<String> result = filterTableNames(tableNames, caseInsensitiveFilters);
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("USER_ACCOUNTS", "user_profiles");
    }
    
    @Test
    @DisplayName("Should handle wildcard patterns with question mark")
    void shouldHandleWildcardPatternsWithQuestionMark() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER1_ACCOUNTS", "USER12_ACCOUNTS", "CUSTOMER_ORDERS");
        FilterConfig filters = FilterConfig.builder()
                .includePattern("USER?_ACCOUNTS")
                .build();
        
        // Act
        List<String> result = filterTableNames(tableNames, filters);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("USER1_ACCOUNTS");
    }
    
    @Test
    @DisplayName("Should handle regex patterns when enabled")
    void shouldHandleRegexPatterns() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER123_ACCOUNTS", "USER_ACCOUNTS", "CUSTOMER_ORDERS");
        FilterConfig filters = FilterConfig.builder()
                .includePattern("USER\\d+_ACCOUNTS")
                .useRegex(true)
                .build();
        
        // Act
        List<String> result = filterTableNames(tableNames, filters);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("USER123_ACCOUNTS");
    }
    
    @Test
    @DisplayName("Should exclude system tables using predefined filter")
    void shouldExcludeSystemTables() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER_ACCOUNTS", "SYS_OBJECTS", "SYSTEM_TABLES", "APEX_APPLICATIONS", "CUSTOMER_ORDERS");
        FilterConfig filters = FilterConfig.excludeSystemTables();
        
        // Act
        List<String> result = filterTableNames(tableNames, filters);
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("USER_ACCOUNTS", "CUSTOMER_ORDERS");
    }
    
    @Test
    @DisplayName("Should handle multiple include patterns")
    void shouldHandleMultipleIncludePatterns() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER_ACCOUNTS", "CUSTOMER_ORDERS", "PRODUCT_CATALOG", "ORDER_ITEMS");
        FilterConfig filters = FilterConfig.builder()
                .includePattern("USER_*")
                .includePattern("CUSTOMER_*")
                .includePattern("ORDER_*")
                .build();
        
        // Act
        List<String> result = filterTableNames(tableNames, filters);
        
        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("USER_ACCOUNTS", "CUSTOMER_ORDERS", "ORDER_ITEMS");
    }
    
    @Test
    @DisplayName("Should return empty list when no tables match filters")
    void shouldReturnEmptyListWhenNoTablesMatch() {
        // Arrange
        List<String> tableNames = Arrays.asList("USER_ACCOUNTS", "CUSTOMER_ORDERS", "PRODUCT_CATALOG");
        FilterConfig filters = FilterConfig.builder()
                .includePattern("NONEXISTENT_*")
                .build();
        
        // Act
        List<String> result = filterTableNames(tableNames, filters);
        
        // Assert
        assertThat(result).isEmpty();
    }
    
    /**
     * Helper method to simulate the filtering logic that would be applied in the service.
     * This tests the FilterConfig.matches() method which is the core filtering functionality.
     */
    private List<String> filterTableNames(List<String> tableNames, FilterConfig filters) {
        if (filters == null || !filters.hasFilters()) {
            return tableNames;
        }
        
        return tableNames.stream()
                .filter(filters::matches)
                .collect(Collectors.toList());
    }
}
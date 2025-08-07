package dev.kreaker.vtda.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FilterConfigTest {

    @Test
    @DisplayName("Should create filter configuration with default values")
    void shouldCreateConfigWithDefaults() {
        FilterConfig config = FilterConfig.builder().build();

        assertThat(config.getIncludePatterns()).isNullOrEmpty();
        assertThat(config.getExcludePatterns()).isNullOrEmpty();
        assertThat(config.isCaseSensitive()).isFalse();
        assertThat(config.isUseRegex()).isFalse();
    }

    @Test
    @DisplayName("Should create configuration with include and exclude patterns")
    void shouldCreateConfigWithPatterns() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("USER_*")
                .includePattern("CUSTOMER_*")
                .excludePattern("*_TEMP")
                .excludePattern("*_BAK")
                .caseSensitive(true)
                .useRegex(false)
                .build();

        assertThat(config.getIncludePatterns()).containsExactly("USER_*", "CUSTOMER_*");
        assertThat(config.getExcludePatterns()).containsExactly("*_TEMP", "*_BAK");
        assertThat(config.isCaseSensitive()).isTrue();
        assertThat(config.isUseRegex()).isFalse();
    }

    @Test
    @DisplayName("Should validate configuration successfully for valid patterns")
    void shouldValidateSuccessfully() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("USER_*")
                .excludePattern("*_TEMP")
                .build();

        assertThatCode(config::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate regex patterns successfully")
    void shouldValidateRegexPatterns() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("USER_\\d+")
                .excludePattern(".*_TEMP$")
                .useRegex(true)
                .build();

        assertThatCode(config::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception for null or empty patterns")
    void shouldThrowExceptionForInvalidPatterns() {
        FilterConfig configWithEmptyExclude = FilterConfig.builder()
                .excludePattern("")
                .build();

        assertThatThrownBy(configWithEmptyExclude::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exclude pattern cannot be null or empty");
        
        FilterConfig configWithWhitespaceInclude = FilterConfig.builder()
                .includePattern("   ")
                .build();

        assertThatThrownBy(configWithWhitespaceInclude::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("include pattern cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for invalid regex patterns")
    void shouldThrowExceptionForInvalidRegex() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("[invalid")
                .useRegex(true)
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid regex include pattern");
    }

    @Test
    @DisplayName("Should match tables with include patterns")
    void shouldMatchTablesWithIncludePatterns() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("USER_*")
                .includePattern("CUSTOMER_*")
                .build();

        assertThat(config.matches("USER_ACCOUNTS")).isTrue();
        assertThat(config.matches("CUSTOMER_ORDERS")).isTrue();
        assertThat(config.matches("PRODUCT_CATALOG")).isFalse();
        assertThat(config.matches("user_accounts")).isTrue(); // case insensitive by default
    }

    @Test
    @DisplayName("Should exclude tables with exclude patterns")
    void shouldExcludeTablesWithExcludePatterns() {
        FilterConfig config = FilterConfig.builder()
                .excludePattern("*_TEMP")
                .excludePattern("*_BAK")
                .build();

        assertThat(config.matches("USER_ACCOUNTS")).isTrue();
        assertThat(config.matches("USER_ACCOUNTS_TEMP")).isFalse();
        assertThat(config.matches("CUSTOMER_BAK")).isFalse();
        assertThat(config.matches("customer_bak")).isFalse(); // case insensitive by default
    }

    @Test
    @DisplayName("Should apply exclude patterns before include patterns")
    void shouldApplyExcludeBeforeInclude() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("USER_*")
                .excludePattern("*_TEMP")
                .build();

        assertThat(config.matches("USER_ACCOUNTS")).isTrue();
        assertThat(config.matches("USER_ACCOUNTS_TEMP")).isFalse(); // excluded despite matching include
        assertThat(config.matches("CUSTOMER_ACCOUNTS")).isFalse(); // doesn't match include
    }

    @Test
    @DisplayName("Should handle case sensitivity correctly")
    void shouldHandleCaseSensitivity() {
        FilterConfig caseSensitiveConfig = FilterConfig.builder()
                .includePattern("USER_*")
                .caseSensitive(true)
                .build();

        FilterConfig caseInsensitiveConfig = FilterConfig.builder()
                .includePattern("USER_*")
                .caseSensitive(false)
                .build();

        assertThat(caseSensitiveConfig.matches("USER_ACCOUNTS")).isTrue();
        assertThat(caseSensitiveConfig.matches("user_accounts")).isFalse();

        assertThat(caseInsensitiveConfig.matches("USER_ACCOUNTS")).isTrue();
        assertThat(caseInsensitiveConfig.matches("user_accounts")).isTrue();
    }

    @Test
    @DisplayName("Should handle regex patterns correctly")
    void shouldHandleRegexPatterns() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("USER_\\d+")
                .excludePattern(".*_TEMP$")
                .useRegex(true)
                .build();

        assertThat(config.matches("USER_123")).isTrue();
        assertThat(config.matches("USER_ABC")).isFalse(); // doesn't match regex
        assertThat(config.matches("USER_123_TEMP")).isFalse(); // excluded by regex
    }

    @Test
    @DisplayName("Should handle wildcard patterns correctly")
    void shouldHandleWildcardPatterns() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("USER_*")
                .includePattern("CUST?MER_*")
                .build();

        assertThat(config.matches("USER_ACCOUNTS")).isTrue();
        assertThat(config.matches("USER_")).isTrue();
        assertThat(config.matches("CUSTOMER_ORDERS")).isTrue();
        assertThat(config.matches("CUSTAMER_ORDERS")).isTrue();
        assertThat(config.matches("CUSTOMMER_ORDERS")).isFalse(); // ? matches single character
    }

    @Test
    @DisplayName("Should return true for all tables when no filters configured")
    void shouldReturnTrueWhenNoFilters() {
        FilterConfig config = FilterConfig.builder().build();

        assertThat(config.matches("ANY_TABLE")).isTrue();
        assertThat(config.matches("ANOTHER_TABLE")).isTrue();
    }

    @Test
    @DisplayName("Should return false for null or empty table names")
    void shouldReturnFalseForInvalidTableNames() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("*")
                .build();

        assertThat(config.matches(null)).isFalse();
        assertThat(config.matches("")).isFalse();
        assertThat(config.matches("   ")).isFalse();
    }

    @Test
    @DisplayName("Should determine if filters are configured")
    void shouldDetermineIfFiltersConfigured() {
        FilterConfig noFilters = FilterConfig.builder().build();
        FilterConfig withInclude = FilterConfig.builder().includePattern("USER_*").build();
        FilterConfig withExclude = FilterConfig.builder().excludePattern("*_TEMP").build();

        assertThat(noFilters.hasFilters()).isFalse();
        assertThat(withInclude.hasFilters()).isTrue();
        assertThat(withExclude.hasFilters()).isTrue();
    }

    @Test
    @DisplayName("Should count total patterns correctly")
    void shouldCountPatternsCorrectly() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("USER_*")
                .includePattern("CUSTOMER_*")
                .excludePattern("*_TEMP")
                .build();

        assertThat(config.getPatternCount()).isEqualTo(3);

        FilterConfig noPatterns = FilterConfig.builder().build();
        assertThat(noPatterns.getPatternCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should create include-all configuration")
    void shouldCreateIncludeAllConfig() {
        FilterConfig config = FilterConfig.includeAll();

        assertThat(config.hasFilters()).isFalse();
        assertThat(config.matches("ANY_TABLE")).isTrue();
    }

    @Test
    @DisplayName("Should create single include pattern configuration")
    void shouldCreateSingleIncludePatternConfig() {
        FilterConfig config = FilterConfig.includePattern("USER_*", true);

        assertThat(config.getIncludePatterns()).containsExactly("USER_*");
        assertThat(config.isCaseSensitive()).isTrue();
        assertThat(config.matches("USER_ACCOUNTS")).isTrue();
        assertThat(config.matches("user_accounts")).isFalse();
    }

    @Test
    @DisplayName("Should create single exclude pattern configuration")
    void shouldCreateSingleExcludePatternConfig() {
        FilterConfig config = FilterConfig.excludePattern("*_TEMP", false);

        assertThat(config.getExcludePatterns()).containsExactly("*_TEMP");
        assertThat(config.isCaseSensitive()).isFalse();
        assertThat(config.matches("USER_ACCOUNTS")).isTrue();
        assertThat(config.matches("USER_ACCOUNTS_TEMP")).isFalse();
    }

    @Test
    @DisplayName("Should create system tables exclusion configuration")
    void shouldCreateSystemTablesExclusionConfig() {
        FilterConfig config = FilterConfig.excludeSystemTables();

        assertThat(config.getExcludePatterns()).isNotEmpty();
        assertThat(config.isCaseSensitive()).isFalse();
        
        // Test some common system table patterns
        assertThat(config.matches("USER_ACCOUNTS")).isTrue();
        assertThat(config.matches("SYS_OBJECTS")).isFalse();
        assertThat(config.matches("SYSTEM_TABLES")).isFalse();
        assertThat(config.matches("APEX_APPLICATIONS")).isFalse();
        assertThat(config.matches("sys_objects")).isFalse(); // case insensitive
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCode() {
        FilterConfig config1 = FilterConfig.builder()
                .includePattern("USER_*")
                .excludePattern("*_TEMP")
                .caseSensitive(true)
                .build();

        FilterConfig config2 = FilterConfig.builder()
                .includePattern("USER_*")
                .excludePattern("*_TEMP")
                .caseSensitive(true)
                .build();

        FilterConfig config3 = FilterConfig.builder()
                .includePattern("CUSTOMER_*")
                .excludePattern("*_TEMP")
                .caseSensitive(true)
                .build();

        assertThat(config1).isEqualTo(config2);
        assertThat(config1).isNotEqualTo(config3);
        assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
    }

    @Test
    @DisplayName("Should provide meaningful toString representation")
    void shouldProvideToString() {
        FilterConfig config = FilterConfig.builder()
                .includePattern("USER_*")
                .excludePattern("*_TEMP")
                .caseSensitive(true)
                .useRegex(false)
                .build();

        String toString = config.toString();
        
        assertThat(toString).contains("FilterConfig");
        assertThat(toString).contains("USER_*");
        assertThat(toString).contains("*_TEMP");
        assertThat(toString).contains("caseSensitive=true");
        assertThat(toString).contains("useRegex=false");
    }
}
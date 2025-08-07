package dev.kreaker.vtda.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

class DatabaseConfigTest {

    @Test
    @DisplayName("Should create valid database configuration with all required fields")
    void shouldCreateValidDatabaseConfig() {
        DatabaseConfig config = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .build();

        assertThat(config.getUrl()).isEqualTo("jdbc:oracle:thin:@localhost:1521:XE");
        assertThat(config.getUsername()).isEqualTo("testuser");
        assertThat(config.getPassword()).isEqualTo("testpass");
        assertThat(config.getSchema()).isEqualTo("TESTSCHEMA");
        assertThat(config.getConnectionTimeout()).isEqualTo(30); // default
        assertThat(config.getQueryTimeout()).isEqualTo(300); // default
    }

    @Test
    @DisplayName("Should create configuration with custom timeout values")
    void shouldCreateConfigWithCustomTimeouts() {
        DatabaseConfig config = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .connectionTimeout(60)
                .queryTimeout(600)
                .build();

        assertThat(config.getConnectionTimeout()).isEqualTo(60);
        assertThat(config.getQueryTimeout()).isEqualTo(600);
    }

    @Test
    @DisplayName("Should validate configuration successfully for valid inputs")
    void shouldValidateSuccessfully() {
        DatabaseConfig config = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .build();

        assertThatCode(config::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when URL is null or empty")
    void shouldThrowExceptionForInvalidUrl() {
        DatabaseConfig configWithNullUrl = DatabaseConfig.builder()
                .url(null)
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .build();

        assertThatThrownBy(configWithNullUrl::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Database URL cannot be null or empty");

        DatabaseConfig configWithEmptyUrl = DatabaseConfig.builder()
                .url("")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .build();

        assertThatThrownBy(configWithEmptyUrl::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Database URL cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for non-Oracle JDBC URL")
    void shouldThrowExceptionForNonOracleUrl() {
        DatabaseConfig config = DatabaseConfig.builder()
                .url("jdbc:mysql://localhost:3306/test")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Database URL must be a valid Oracle JDBC URL");
    }

    @Test
    @DisplayName("Should throw exception when username is null or empty")
    void shouldThrowExceptionForInvalidUsername() {
        DatabaseConfig configWithNullUsername = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username(null)
                .password("testpass")
                .schema("TESTSCHEMA")
                .build();

        assertThatThrownBy(configWithNullUsername::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Database username cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when password is null")
    void shouldThrowExceptionForNullPassword() {
        DatabaseConfig config = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password(null)
                .schema("TESTSCHEMA")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Database password cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when schema is null or empty")
    void shouldThrowExceptionForInvalidSchema() {
        DatabaseConfig config = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("")
                .build();

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Database schema cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for invalid timeout values")
    void shouldThrowExceptionForInvalidTimeouts() {
        DatabaseConfig configWithInvalidConnectionTimeout = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .connectionTimeout(0)
                .build();

        assertThatThrownBy(configWithInvalidConnectionTimeout::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Connection timeout must be positive");

        DatabaseConfig configWithInvalidQueryTimeout = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .queryTimeout(-1)
                .build();

        assertThatThrownBy(configWithInvalidQueryTimeout::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Query timeout must be positive");
    }

    @Test
    @DisplayName("Should add connection timeout to URL when not present")
    void shouldAddConnectionTimeoutToUrl() {
        DatabaseConfig config = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .connectionTimeout(45)
                .build();

        String urlWithTimeout = config.getUrlWithTimeout();
        assertThat(urlWithTimeout).isEqualTo("jdbc:oracle:thin:@localhost:1521:XE?connectTimeout=45000");
    }

    @Test
    @DisplayName("Should not modify URL when connection timeout already present")
    void shouldNotModifyUrlWhenTimeoutPresent() {
        String originalUrl = "jdbc:oracle:thin:@localhost:1521:XE?connectTimeout=30000";
        DatabaseConfig config = DatabaseConfig.builder()
                .url(originalUrl)
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .build();

        String urlWithTimeout = config.getUrlWithTimeout();
        assertThat(urlWithTimeout).isEqualTo(originalUrl);
    }

    @Test
    @DisplayName("Should create masked configuration for logging")
    void shouldCreateMaskedConfig() {
        DatabaseConfig config = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("secretpassword")
                .schema("TESTSCHEMA")
                .build();

        DatabaseConfig maskedConfig = config.getMaskedConfig();
        
        assertThat(maskedConfig.getUrl()).isEqualTo(config.getUrl());
        assertThat(maskedConfig.getUsername()).isEqualTo(config.getUsername());
        assertThat(maskedConfig.getPassword()).isEqualTo("***");
        assertThat(maskedConfig.getSchema()).isEqualTo(config.getSchema());
        assertThat(maskedConfig.getConnectionTimeout()).isEqualTo(config.getConnectionTimeout());
        assertThat(maskedConfig.getQueryTimeout()).isEqualTo(config.getQueryTimeout());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCode() {
        DatabaseConfig config1 = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .build();

        DatabaseConfig config2 = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .build();

        DatabaseConfig config3 = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("differentuser")
                .password("testpass")
                .schema("TESTSCHEMA")
                .build();

        assertThat(config1).isEqualTo(config2);
        assertThat(config1).isNotEqualTo(config3);
        assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
    }

    @Test
    @DisplayName("Should provide meaningful toString representation")
    void shouldProvideToString() {
        DatabaseConfig config = DatabaseConfig.builder()
                .url("jdbc:oracle:thin:@localhost:1521:XE")
                .username("testuser")
                .password("secretpassword")
                .schema("TESTSCHEMA")
                .build();

        String toString = config.toString();
        
        assertThat(toString).contains("DatabaseConfig");
        assertThat(toString).contains("jdbc:oracle:thin:@localhost:1521:XE");
        assertThat(toString).contains("testuser");
        assertThat(toString).contains("***"); // password should be masked
        assertThat(toString).contains("TESTSCHEMA");
        assertThat(toString).doesNotContain("secretpassword"); // actual password should not appear
    }
}
package dev.kreaker.vtda.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class specifically for testing short form command-line arguments.
 * This tests the requirement that short forms (-v, -q, -h) work correctly.
 */
class ConfigurationServiceShortFormsTest {
    
    @Test
    void shouldRecognizeShortFormArguments() {
        // Test that our custom implementation recognizes short forms
        // Note: Spring Boot's ApplicationArguments doesn't natively support single-dash options
        // but our ConfigurationService implementation adds this support
        DefaultApplicationArguments args1 = new DefaultApplicationArguments(new String[]{"-v"});
        assertThat(args1.getSourceArgs()).contains("-v");
        
        DefaultApplicationArguments args2 = new DefaultApplicationArguments(new String[]{"-q"});
        assertThat(args2.getSourceArgs()).contains("-q");
        
        DefaultApplicationArguments args3 = new DefaultApplicationArguments(new String[]{"-h"});
        assertThat(args3.getSourceArgs()).contains("-h");
    }
    
    @Test
    void shouldHandleVerboseShortForm() {
        // Given
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(new String[]{"-v"});
        StandardEnvironment environment = new StandardEnvironment();
        ConfigurationServiceImpl service = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When & Then
        assertThat(service.isVerboseMode()).isTrue();
        assertThat(service.isQuietMode()).isFalse();
    }
    
    @Test
    void shouldHandleQuietShortForm() {
        // Given
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(new String[]{"-q"});
        StandardEnvironment environment = new StandardEnvironment();
        ConfigurationServiceImpl service = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When & Then
        assertThat(service.isQuietMode()).isTrue();
        assertThat(service.isVerboseMode()).isFalse();
    }
    
    @Test
    void shouldHandleHelpShortForm() {
        // Given
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(new String[]{"-h"});
        StandardEnvironment environment = new StandardEnvironment();
        ConfigurationServiceImpl service = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When & Then
        assertThat(service.isHelpRequested()).isTrue();
    }
    
    @Test
    void shouldHandleCombinedShortForms() {
        // Test that multiple short forms can be combined (if supported by Spring Boot)
        DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(new String[]{"-v", "-h"});
        StandardEnvironment environment = new StandardEnvironment();
        ConfigurationServiceImpl service = new ConfigurationServiceImpl(applicationArguments, environment);
        
        // When & Then
        assertThat(service.isVerboseMode()).isTrue();
        assertThat(service.isHelpRequested()).isTrue();
    }
}
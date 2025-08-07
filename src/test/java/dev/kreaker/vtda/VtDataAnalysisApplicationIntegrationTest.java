package dev.kreaker.vtda;

import dev.kreaker.vtda.service.ConfigurationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for command-line interface with the main application.
 * Tests requirement 4.1 and 4.5 - command-line argument parsing integration.
 */
@SpringBootTest(args = {"--help"})
@TestPropertySource(properties = {
    "spring.main.web-application-type=none",
    "logging.level.root=ERROR"
})
class VtDataAnalysisApplicationIntegrationTest {
    
    @Autowired
    private ConfigurationService configurationService;
    
    @Test
    void shouldRecognizeHelpArgumentInSpringBootContext() {
        // When the application is started with --help argument
        // Then the ConfigurationService should recognize it
        assertThat(configurationService.isHelpRequested()).isTrue();
    }
    
    @Test
    void shouldProvideHelpTextInSpringBootContext() {
        // When help text is requested
        String helpText = configurationService.getHelpText();
        
        // Then it should contain all required sections
        assertThat(helpText).isNotNull();
        assertThat(helpText).contains("Database Metadata Analyzer");
        assertThat(helpText).contains("Usage: java -jar vtda.jar [OPTIONS]");
        assertThat(helpText).contains("Database Connection Options:");
        assertThat(helpText).contains("Export Options:");
        assertThat(helpText).contains("Filter Options:");
        assertThat(helpText).contains("Configuration Options:");
        assertThat(helpText).contains("Output Options:");
        assertThat(helpText).contains("Examples:");
        assertThat(helpText).contains("Configuration File Format");
    }
}
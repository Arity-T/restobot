package dev.tishenko.restobot.integration.tripadvisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Spring configuration for TripAdvisor API integration */
@Configuration
public class TripAdvisorConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TripAdvisorConfiguration.class);

    private static final String API_KEY_PROPERTY = "tripadvisor.api.key";
    private static final String API_KEY_ENV_VAR = "TRIPADVISOR_API_KEY";
    private static final String API_HOST_PROPERTY = "tripadvisor.api.host";
    private static final String DEFAULT_API_HOST = "https://api.content.tripadvisor.com/api/v1";

    /**
     * Creates a TripAdvisor client bean using the API key from Spring environment or system
     * environment variables
     *
     * @param env Spring Environment
     * @return TripAdvisorClient bean
     */
    @Bean
    public TripAdvisorClient tripAdvisorClient(Environment env) {
        // Try to get API key from Spring environment first
        String apiKey = env.getProperty(API_KEY_PROPERTY);

        // If not found, try system environment variable
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv(API_KEY_ENV_VAR);
        }

        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("TripAdvisor API key not found in properties or environment variables");
            throw new IllegalStateException(
                    "TripAdvisor API key not found. Set the "
                            + API_KEY_PROPERTY
                            + " property or "
                            + API_KEY_ENV_VAR
                            + " environment variable.");
        }

        // Get base URL from properties with default fallback
        String baseUrl = env.getProperty(API_HOST_PROPERTY, DEFAULT_API_HOST);
        logger.info("Configuring TripAdvisor client with baseUrl: {}", baseUrl);

        return new TripAdvisorClient(apiKey, baseUrl);
    }
}

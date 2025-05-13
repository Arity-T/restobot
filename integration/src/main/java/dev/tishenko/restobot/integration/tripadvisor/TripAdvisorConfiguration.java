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
    private static final String API_HOST_PROPERTY = "tripadvisor.api.host";
    private static final String DEFAULT_API_HOST = "https://api.content.tripadvisor.com/api/v1";
    private static final String API_LANGUAGE_PROPERTY = "tripadvisor.api.language";
    private static final String DEFAULT_API_LANGUAGE = "ru";

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

        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("TripAdvisor API key not found in properties");
            throw new IllegalStateException(
                    "TripAdvisor API key not found. Set the " + API_KEY_PROPERTY + " property");
        }

        logger.info("TripAdvisor API key found in property: {}", API_KEY_PROPERTY);

        // Get base URL from properties with default fallback
        String baseUrl = env.getProperty(API_HOST_PROPERTY, DEFAULT_API_HOST);
        logger.info("Configuring TripAdvisor client with baseUrl: {}", baseUrl);

        String language = env.getProperty(API_LANGUAGE_PROPERTY, DEFAULT_API_LANGUAGE);
        logger.info("Configuring TripAdvisor client with language: {}", language);

        return new TripAdvisorClient(apiKey, baseUrl, language);
    }
}

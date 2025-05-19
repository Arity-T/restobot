package dev.tishenko.restobot.integration.tripadvisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration for TripAdvisor API integration */
@Configuration
public class TripAdvisorConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TripAdvisorConfiguration.class);

    /**
     * Creates a TripAdvisor client bean using the API key from Spring environment or system
     * environment variables
     *
     * @param env Spring Environment
     * @return TripAdvisorClient bean
     */
    @Bean
    public TripAdvisorClient tripAdvisorClient(
            @Value("${tripadvisor.api.key}") String apiKey,
            @Value("${tripadvisor.api.host}") String baseUrl,
            @Value("${tripadvisor.api.language}") String language,
            TripAdvisorTracker tracker) {

        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("TripAdvisor API key not found in properties");
            throw new IllegalStateException("TripAdvisor API key not found");
        }

        logger.info("Configuring TripAdvisor client with baseUrl: {}", baseUrl);
        logger.info("Configuring TripAdvisor client with language: {}", language);

        return new TripAdvisorClient(apiKey, baseUrl, language, tracker);
    }
}

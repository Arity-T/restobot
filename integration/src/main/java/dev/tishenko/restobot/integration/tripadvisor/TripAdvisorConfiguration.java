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
    public TripAdvisorClient tripAdvisorClient(
            @Value("${tripadvisor.api.key}") String apiKey,
            @Value("${tripadvisor.api.host}") String baseUrl,
            @Value("${tripadvisor.api.language}") String language,
            TripAdvisorTracker tracker) {
        return new TripAdvisorClient(apiKey, baseUrl, language, tracker);
    }
}

package dev.tishenko.restobot.integration.tripadvisor;

import com.google.gson.Gson;
import dev.tishenko.restobot.integration.tripadvisor.exception.TripAdvisorApiException;
import dev.tishenko.restobot.integration.tripadvisor.model.*;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public class TripAdvisorClient {
    private static final Logger logger = LoggerFactory.getLogger(TripAdvisorClient.class);

    private final WebClient webClient;
    private final String apiKey;
    private final Gson gson;
    private final String language;

    public TripAdvisorClient(String apiKey, String baseUrl, String language) {
        this.apiKey = apiKey;
        this.gson = new Gson();
        this.language = language;

        logger.info("Initializing TripAdvisor client with base URL: {}", baseUrl);

        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Get location details by location ID
     *
     * @param locationId The TripAdvisor location ID
     * @return Mono with location details
     */
    public Mono<LocationDetails> getLocationDetails(String locationId) {
        logger.debug("Fetching location details for ID: {}", locationId);

        return webClient
                .get()
                .uri(
                        uriBuilder -> {
                            URI uri =
                                    uriBuilder
                                            .path("/location/{locationId}/details")
                                            .queryParam("key", apiKey)
                                            .queryParam("language", language)
                                            .build(locationId);
                            logger.info(
                                    "Requesting TripAdvisor URL: {}",
                                    uri.toString().replaceAll(apiKey, "****"));
                            return uri;
                        })
                .retrieve()
                .bodyToMono(String.class)
                .map(
                        response -> {
                            logger.debug(
                                    "Received location details response for ID: {}", locationId);
                            logger.trace("Response: {}", response);
                            return gson.fromJson(response, LocationDetails.class);
                        })
                .onErrorResume(
                        WebClientResponseException.class,
                        e -> {
                            logger.error(
                                    "Error fetching location details for ID {}: {} - {}",
                                    locationId,
                                    e.getStatusCode().value(),
                                    e.getMessage());
                            return Mono.error(
                                    new TripAdvisorApiException(
                                            "Error fetching location details: " + e.getMessage(),
                                            e.getStatusCode().value(),
                                            e));
                        });
    }
}

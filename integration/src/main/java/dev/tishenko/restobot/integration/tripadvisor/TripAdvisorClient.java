package dev.tishenko.restobot.integration.tripadvisor;

import com.google.gson.Gson;
import dev.tishenko.restobot.integration.tripadvisor.exception.TripAdvisorApiException;
import dev.tishenko.restobot.integration.tripadvisor.model.*;
import java.net.URI;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

public class TripAdvisorClient {
    private static final Logger logger = LoggerFactory.getLogger(TripAdvisorClient.class);

    private final WebClient webClient;
    private final String apiKey;
    private final Gson gson;
    private final String language;
    private final String category = "restaurants";

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

        return executeRequest(
                "/location/{locationId}/details",
                uriBuilder -> uriBuilder.build(locationId),
                response -> gson.fromJson(response, LocationDetails.class),
                "Error fetching location details",
                "location details",
                locationId);
    }

    /**
     * Get location reviews by location ID
     *
     * @param locationId The TripAdvisor location ID
     * @return Mono with location reviews
     */
    public Mono<LocationReviews> getLocationReviews(String locationId) {
        logger.debug("Fetching location reviews for ID: {}", locationId);

        return executeRequest(
                "/location/{locationId}/reviews",
                uriBuilder -> uriBuilder.build(locationId),
                response -> gson.fromJson(response, LocationReviews.class),
                "Error fetching location reviews",
                "location reviews",
                locationId);
    }

    /**
     * Search for locations by search query
     *
     * @param searchQuery The search query
     * @return Mono with search results
     */
    public Mono<LocationSearch> searchLocations(String searchQuery) {
        logger.debug("Searching locations with query: {}", searchQuery);

        return executeRequest(
                "/location/search",
                uriBuilder -> {
                    uriBuilder.queryParam("searchQuery", searchQuery);

                    uriBuilder.queryParam("category", category);
                    return uriBuilder.build();
                },
                response -> gson.fromJson(response, LocationSearch.class),
                "Error searching locations",
                "location search",
                searchQuery);
    }

    /**
     * Search for nearby locations
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param radius Optional radius in km (default is 5)
     * @return Mono with nearby search results
     */
    public Mono<LocationSearch> searchNearbyLocations(
            double latitude, double longitude, Double radius, String radiusUnit) {
        logger.debug(
                "Searching nearby locations at lat: {}, long: {}, radius: {}, radiusUnit: {}",
                latitude,
                longitude,
                radius,
                radiusUnit);

        return executeRequest(
                "/location/nearby_search",
                uriBuilder -> {
                    uriBuilder.queryParam("latLong", latitude + "," + longitude);

                    if (radius != null) {
                        uriBuilder.queryParam("radius", radius);
                    }

                    if (radiusUnit != null && !radiusUnit.isEmpty()) {
                        uriBuilder.queryParam("radiusUnit", radiusUnit);
                    }

                    uriBuilder.queryParam("category", category);
                    return uriBuilder.build();
                },
                response -> gson.fromJson(response, LocationSearch.class),
                "Error searching nearby locations",
                "nearby location search",
                latitude + "," + longitude);
    }

    /**
     * Generic method to execute API requests with common error handling and response mapping
     *
     * @param path API endpoint path
     * @param uriCustomizer Function to customize URI builder with path parameters and query
     *     parameters
     * @param responseMapper Function to map the response to a domain object
     * @param errorPrefix Prefix for error messages
     * @param operationName Name of the operation for logging
     * @param identifier Identifier for the request (location ID, search query, etc.)
     * @return Mono with the mapped response
     */
    private <R> Mono<R> executeRequest(
            String path,
            Function<UriBuilder, URI> uriCustomizer,
            Function<String, R> responseMapper,
            String errorPrefix,
            String operationName,
            String identifier) {

        return webClient
                .get()
                .uri(
                        uriBuilder -> {
                            // Add common parameters
                            UriBuilder builder =
                                    uriBuilder
                                            .path(path)
                                            .queryParam("key", apiKey)
                                            .queryParam("language", language);

                            // Apply custom URI building
                            URI uri = uriCustomizer.apply(builder);

                            // Log the URI with masked API key
                            logger.info(
                                    "Requesting TripAdvisor URL: {}",
                                    uri.toString().replaceAll(apiKey, "****"));

                            return uri;
                        })
                .retrieve()
                .bodyToMono(String.class)
                .map(
                        response -> {
                            logger.debug("Received {} response for: {}", operationName, identifier);
                            logger.trace("Response: {}", response);
                            return responseMapper.apply(response);
                        })
                .onErrorResume(
                        WebClientResponseException.class,
                        e -> {
                            logger.error(
                                    "Error {} for {}: {} - {}",
                                    operationName,
                                    identifier,
                                    e.getStatusCode().value(),
                                    e.getMessage());
                            return Mono.error(
                                    new TripAdvisorApiException(
                                            errorPrefix + ": " + e.getMessage(),
                                            e.getStatusCode().value(),
                                            e));
                        });
    }
}

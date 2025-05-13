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
    public Mono<LocationDetails> getLocationDetails(Integer locationId) {
        logger.debug("Fetching location details for ID: {}", locationId);

        return executeRequest(
                "/location/{locationId}/details",
                uriBuilder -> uriBuilder.build(locationId),
                LocationDetails.class);
    }

    /**
     * Get location reviews by location ID
     *
     * @param locationId The TripAdvisor location ID
     * @param limit The number of results to return (optional)
     * @param offset The index of the first result (optional)
     * @return Mono with location reviews
     */
    public Mono<LocationReviews> getLocationReviews(
            Integer locationId, Integer limit, Integer offset) {
        logger.debug("Fetching location reviews for ID: {}", locationId);

        return executeRequest(
                "/location/{locationId}/reviews",
                uriBuilder -> {
                    if (limit != null) {
                        uriBuilder.queryParam("limit", limit);
                    }
                    if (offset != null) {
                        uriBuilder.queryParam("offset", offset);
                    }
                    return uriBuilder.build(locationId);
                },
                LocationReviews.class);
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
                LocationSearch.class);
    }

    /**
     * Search for nearby locations
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param radius Optional radius
     * @param radiusUnit Optional radius unit
     * @return Mono with nearby search results
     */
    public Mono<LocationSearch> searchNearbyLocations(
            double latitude, double longitude, Double radius, RadiusUnit radiusUnit) {
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

                    if (radiusUnit != null) {
                        uriBuilder.queryParam("radiusUnit", radiusUnit);
                    }

                    uriBuilder.queryParam("category", category);
                    return uriBuilder.build();
                },
                LocationSearch.class);
    }

    /**
     * Generic method to execute API requests with common error handling and response mapping
     *
     * @param path API endpoint path
     * @param uriCustomizer Function to customize URI builder with path parameters and query
     *     parameters
     * @param responseType Type of the response
     * @return Mono with the mapped response
     */
    private <R> Mono<R> executeRequest(
            String path, Function<UriBuilder, URI> uriCustomizer, Class<R> responseType) {

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
                            logger.debug("Received response: {}", path);
                            logger.trace("Response: {}", response);

                            // Check if response contains error object
                            if (response.contains("\"error\":")) {
                                ErrorResponse errorResponse =
                                        gson.fromJson(response, ErrorResponse.class);
                                if (errorResponse != null && errorResponse.getError() != null) {
                                    ErrorResponse.Error error = errorResponse.getError();
                                    logger.error(
                                            "TripAdvisor API error: type={}, code={}, message={}",
                                            error.getType(),
                                            error.getCode(),
                                            error.getMessage());
                                    throw new TripAdvisorApiException(
                                            "TripAdvisor API error: " + error.getMessage(),
                                            error.getCode() != null ? error.getCode() : 0);
                                }
                            }

                            return gson.fromJson(response, responseType);
                        })
                .onErrorResume(
                        WebClientResponseException.class,
                        e -> {
                            logger.error(
                                    "Error {}: {} - {}",
                                    path,
                                    e.getStatusCode().value(),
                                    e.getMessage());

                            // Try to parse error response from the error body
                            try {
                                String responseBody = e.getResponseBodyAsString();
                                if (responseBody != null && !responseBody.isEmpty()) {
                                    ErrorResponse errorResponse =
                                            gson.fromJson(responseBody, ErrorResponse.class);
                                    if (errorResponse != null && errorResponse.getError() != null) {
                                        ErrorResponse.Error error = errorResponse.getError();
                                        logger.error(
                                                "TripAdvisor API error details: type={}, code={}, message={}",
                                                error.getType(),
                                                error.getCode(),
                                                error.getMessage());
                                        return Mono.error(
                                                new TripAdvisorApiException(
                                                        "TripAdvisor API error: "
                                                                + error.getMessage(),
                                                        error.getCode() != null
                                                                ? error.getCode()
                                                                : e.getStatusCode().value(),
                                                        e));
                                    }
                                }
                            } catch (Exception parseException) {
                                logger.debug(
                                        "Could not parse error response: {}",
                                        parseException.getMessage());
                            }

                            return Mono.error(
                                    new TripAdvisorApiException(
                                            "Error while fetching from "
                                                    + path
                                                    + ": "
                                                    + e.getMessage(),
                                            e.getStatusCode().value(),
                                            e));
                        });
    }
}

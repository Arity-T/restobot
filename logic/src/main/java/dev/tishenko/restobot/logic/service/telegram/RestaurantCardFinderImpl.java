package dev.tishenko.restobot.logic.service.telegram;

import dev.tishenko.restobot.integration.tripadvisor.RadiusUnit;
import dev.tishenko.restobot.integration.tripadvisor.TripAdvisorClient;
import dev.tishenko.restobot.integration.tripadvisor.model.LocationDetails;
import dev.tishenko.restobot.integration.tripadvisor.model.LocationSearch;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.CityRecord;
import dev.tishenko.restobot.logic.service.CityService;
import dev.tishenko.restobot.telegram.services.RestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.RestaurantCardFinder;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RestaurantCardFinderImpl implements RestaurantCardFinder {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantCardFinderImpl.class);
    private final TripAdvisorClient tripAdvisorClient;
    private final CityService cityService;

    public RestaurantCardFinderImpl(TripAdvisorClient tripAdvisorClient, CityService cityService) {
        this.tripAdvisorClient = tripAdvisorClient;
        this.cityService = cityService;
        logger.info("RestaurantCardFinderImpl initialized");
    }

    @Override
    public List<RestaurantCardDTO> getRestaurantCardByGeolocation(double latitude, double longitude)
            throws MalformedURLException, URISyntaxException {
        logger.debug("Searching restaurants by geolocation: lat={}, long={}", latitude, longitude);
        LocationSearch locations =
                tripAdvisorClient
                        .searchNearbyLocations(latitude, longitude, 1.0, RadiusUnit.KM)
                        .block();
        if (locations == null || locations.getData() == null) {
            logger.debug("No locations found for the given coordinates");
            return List.of();
        }

        logger.debug("Found {} locations near the given coordinates", locations.getData().size());
        return locations.getData().stream()
                .map(
                        location -> {
                            logger.debug(
                                    "Fetching details for location ID: {}",
                                    location.getLocationId());
                            LocationDetails details =
                                    tripAdvisorClient
                                            .getLocationDetails(location.getLocationId())
                                            .block();
                            if (details == null) {
                                logger.warn(
                                        "Could not fetch details for location ID: {}",
                                        location.getLocationId());
                                return null;
                            }

                            URL websiteUrl = null;
                            try {
                                websiteUrl =
                                        details.getWebsite() != null
                                                ? new URI(details.getWebsite()).toURL()
                                                : null;
                            } catch (MalformedURLException | URISyntaxException e) {
                                logger.warn(
                                        "Invalid website URL for location {}: {}",
                                        details.getName(),
                                        details.getWebsite());
                                // ignore, leave as null
                            }

                            return new RestaurantCardDTO(
                                    details.getLocationId(),
                                    details.getName(),
                                    details.getAddressObj().getAddressString(),
                                    details.getRating() != null ? details.getRating() : 0.0,
                                    websiteUrl,
                                    details.getDescription(),
                                    details.getLatitude() != null ? details.getLatitude() : 0.0,
                                    details.getLongitude() != null ? details.getLongitude() : 0.0,
                                    details.getAddressObj().getCity());
                        })
                .filter(card -> card != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantCardDTO> getRestaurantCardByParams(
            String city,
            List<String> kitchenTypes,
            List<String> priceCategories,
            List<String> keyWords)
            throws MalformedURLException, URISyntaxException {
        logger.debug(
                "Searching restaurants with parameters: city={}, kitchenTypes={}, priceCategories={}, keyWords={}",
                city,
                kitchenTypes,
                priceCategories,
                keyWords);

        // Combine all search parameters into a single search query
        StringBuilder searchQuery = new StringBuilder();

        CityRecord cityRecord =
                city != null && !city.isEmpty()
                        ? cityService.getCityByName(city).orElse(null)
                        : null;

        Double latitude = cityRecord != null ? cityRecord.getLatitude() : null;
        Double longitude = cityRecord != null ? cityRecord.getLongitude() : null;
        Double radius = cityRecord != null ? cityRecord.getRadius() : null;
        RadiusUnit radiusUnit = RadiusUnit.M;

        if (kitchenTypes != null && !kitchenTypes.isEmpty()) {
            searchQuery.append(String.join(" ", kitchenTypes)).append(" ");
        }
        if (keyWords != null && !keyWords.isEmpty()) {
            searchQuery.append(String.join(" ", keyWords));
        }

        String finalQuery =
                searchQuery.toString().trim().isEmpty()
                        ? "ресторан"
                        : searchQuery.toString().trim();
        logger.debug("Constructed search query: '{}'", finalQuery);

        LocationSearch locations =
                tripAdvisorClient
                        .searchLocations(finalQuery, latitude, longitude, radius, radiusUnit)
                        .block();
        if (locations == null || locations.getData() == null) {
            logger.debug("No locations found for the search query");
            return List.of();
        }

        logger.debug("Found {} locations for the search query", locations.getData().size());
        return locations.getData().stream()
                .map(
                        location -> {
                            logger.debug(
                                    "Fetching details for location ID: {}",
                                    location.getLocationId());
                            LocationDetails details =
                                    tripAdvisorClient
                                            .getLocationDetails(location.getLocationId())
                                            .block();
                            if (details == null) {
                                logger.warn(
                                        "Could not fetch details for location ID: {}",
                                        location.getLocationId());
                                return null;
                            }

                            URL websiteUrl = null;
                            try {
                                websiteUrl =
                                        details.getWebsite() != null
                                                ? new URI(details.getWebsite()).toURL()
                                                : null;
                            } catch (MalformedURLException | URISyntaxException e) {
                                logger.warn(
                                        "Invalid website URL for location {}: {}",
                                        details.getName(),
                                        details.getWebsite());
                                // ignore, leave as null
                            }

                            String description =
                                    details.getDescription() != null
                                            ? details.getDescription()
                                            : "";

                            return new RestaurantCardDTO(
                                    details.getLocationId(),
                                    details.getName(),
                                    details.getAddressObj().getAddressString(),
                                    details.getRating() != null ? details.getRating() : 0.0,
                                    websiteUrl,
                                    description,
                                    details.getLatitude() != null ? details.getLatitude() : 0.0,
                                    details.getLongitude() != null ? details.getLongitude() : 0.0,
                                    details.getAddressObj().getCity());
                        })
                .filter(card -> card != null)
                .collect(Collectors.toList());
    }
}

package dev.tishenko.restobot.logic.service.telegram;

import dev.tishenko.restobot.integration.tripadvisor.RadiusUnit;
import dev.tishenko.restobot.integration.tripadvisor.TripAdvisorClient;
import dev.tishenko.restobot.integration.tripadvisor.model.LocationDetails;
import dev.tishenko.restobot.integration.tripadvisor.model.LocationSearch;
import dev.tishenko.restobot.telegram.services.RestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.RestaurantCardFinder;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RestaurantCardFinderImpl implements RestaurantCardFinder {

    private final TripAdvisorClient tripAdvisorClient;

    public RestaurantCardFinderImpl(TripAdvisorClient tripAdvisorClient) {
        this.tripAdvisorClient = tripAdvisorClient;
    }

    @Override
    public List<RestaurantCardDTO> getRestaurantCardByGeolocation(double latitude, double longitude)
            throws MalformedURLException, URISyntaxException {
        LocationSearch locations =
                tripAdvisorClient
                        .searchNearbyLocations(latitude, longitude, 1.0, RadiusUnit.KM)
                        .block();
        if (locations == null || locations.getData() == null) {
            return List.of();
        }

        return locations.getData().stream()
                .map(
                        location -> {
                            LocationDetails details =
                                    tripAdvisorClient
                                            .getLocationDetails(location.getLocationId())
                                            .block();
                            if (details == null) {
                                return null;
                            }

                            URL websiteUrl = null;
                            try {
                                websiteUrl =
                                        details.getWebsite() != null
                                                ? new URI(details.getWebsite()).toURL()
                                                : null;
                            } catch (MalformedURLException | URISyntaxException e) {
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
        // Combine all search parameters into a single search query
        StringBuilder searchQuery = new StringBuilder();
        if (city != null && !city.isEmpty()) {
            searchQuery.append(city).append(" ");
        }
        if (kitchenTypes != null && !kitchenTypes.isEmpty()) {
            searchQuery.append(String.join(" ", kitchenTypes)).append(" ");
        }
        if (keyWords != null && !keyWords.isEmpty()) {
            searchQuery.append(String.join(" ", keyWords));
        }

        LocationSearch locations =
                tripAdvisorClient
                        .searchLocations(searchQuery.toString().trim(), null, null, null, null)
                        .block();
        if (locations == null || locations.getData() == null) {
            return List.of();
        }

        return locations.getData().stream()
                .map(
                        location -> {
                            LocationDetails details =
                                    tripAdvisorClient
                                            .getLocationDetails(location.getLocationId())
                                            .block();
                            if (details == null) {
                                return null;
                            }

                            // Filter by price category if specified
                            if (priceCategories != null && !priceCategories.isEmpty()) {
                                String priceLevel = details.getPriceLevel();
                                if (priceLevel == null || !priceCategories.contains(priceLevel)) {
                                    return null;
                                }
                            }

                            URL websiteUrl = null;
                            try {
                                websiteUrl =
                                        details.getWebsite() != null
                                                ? new URI(details.getWebsite()).toURL()
                                                : null;
                            } catch (MalformedURLException | URISyntaxException e) {
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
}

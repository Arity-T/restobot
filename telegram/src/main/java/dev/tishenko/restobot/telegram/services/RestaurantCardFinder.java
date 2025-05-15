package dev.tishenko.restobot.telegram.services;




import java.util.List;

/**
 * Service interface for locating restaurant cards based on various search criteria.
 * Provides methods to retrieve restaurant data by geographic coordinates or by custom parameters.
 *
 * Parameter "Любые" means that the user can choose any restaurant according to this criterion.
 * Parameter "Отключено" means that this criterion is not taken into account in the search.
 *
 */
public interface RestaurantCardFinder {

    /**
     * Retrieves a list of restaurant cards near the specified geographic location.
     *
     * @param latitude  the latitude coordinate to search around
     * @param longitude the longitude coordinate to search around
     * @return a list of {@link RestaurantCardDTO} objects located near the given coordinates
     */
    List<RestaurantCardDTO> getRestaurantCardByGeolocation(double latitude, double longitude);

    /**
     * Retrieves a list of restaurant cards matching the given parameters.
     *
     * @param city            the name of the city to filter restaurants in
     * @param kitchenTypes    a list of kitchen or cuisine types to filter by
     * @param priceCategories a list of price category identifiers to filter by
     * @param keyWords        a list of keywords to match against restaurant descriptions or names
     * @return a list of {@link RestaurantCardDTO} objects that match the specified criteria
     */
    List<RestaurantCardDTO> getRestaurantCardByParams(
            String city,
            List<String> kitchenTypes,
            List<String> priceCategories,
            List<String> keyWords
    );
}
package dev.tishenko.restobot.telegram.services;

import java.util.List;

/**
 * Data Transfer Object representing a user profile with preferences and favorite restaurants.
 *
 * <p>Contains user identification, location, preferred kitchen types, price categories, search
 * keywords, and a list of favorite restaurant cards mapped to their visited status.
 *
 * @param chatID unique identifier for the user's chat session
 * @param nickName the user's display name or nickname
 * @param city the city where the user is located or searching in
 * @param kitchenTypes list of preferred kitchen or cuisine types
 * @param priceCategories list of preferred price category identifiers
 * @param keyWords list of search keywords used by the user
 * @param favoriteList list of entries mapping each {@link RestaurantCardDTO}
 */
public record UserDTO(
        long chatID,
        String nickName,
        String city,
        List<String> kitchenTypes,
        List<String> priceCategories,
        List<String> keyWords,
        List<FavoriteRestaurantCardDTO> favoriteList) {}

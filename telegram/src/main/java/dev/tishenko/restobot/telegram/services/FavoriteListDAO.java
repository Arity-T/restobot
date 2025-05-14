package dev.tishenko.restobot.telegram.services;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * DAO for managing the user's favorite restaurants list.
 * Provides methods to add and remove restaurants from the favorites,
 * set the visited status, and retrieve the complete favorites collection.
 */
@Service
public interface FavoriteListDAO {

    /**
     * Adds a restaurant card to the favorites list.
     *
     * @param restaurantId the identifier of the restaurant to add to favorites
     */
    void addRestaurantCardToFavoriteList(int restaurantId);

    /**
     * Removes a restaurant card from the favorites list.
     *
     * @param restaurantId the identifier of the restaurant to remove from favorites
     */
    void removeRestaurantCardToFavoriteList(int restaurantId);

    /**
     * Sets or clears the visited flag for a restaurant card in the favorites list.
     *
     * @param restaurantId the identifier of the restaurant whose status is being changed
     * @param isVisited    {@code true} if the restaurant has been visited; {@code false} otherwise
     */
    void setVisitedStatus(int restaurantId, boolean isVisited);

    /**
     * Retrieves the full list of favorite restaurants along with their visited status.
     *
     * @return a list of entries where each entry maps a {@link RestaurantCardRecord}
     * to a {@link Boolean} flag indicating visit status ({@code true} if the restaurant has been visited)
     */
    List<Map<RestaurantCardRecord, Boolean>> getFavouriteList();
}


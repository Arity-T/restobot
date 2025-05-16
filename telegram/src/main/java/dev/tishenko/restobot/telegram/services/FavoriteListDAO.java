package dev.tishenko.restobot.telegram.services;

import java.util.List;
import java.util.Map;

/**
 * Data Access Object for managing users' favorite restaurant lists in the database.
 *
 * <p>Allows adding and removing restaurants from a user's favorites, setting visit status, and
 * retrieving the list of favorite restaurants along with their visit status.
 */
public interface FavoriteListDAO {

    /**
     * Adds a restaurant card to the specified user's favorites list.
     *
     * @param chatId the unique identifier of the user
     * @param tripadvisorId the unique identifier of the restaurant to add to favorites
     */
    void addRestaurantCardToFavoriteList(long chatId, int tripadvisorId);

    /**
     * Removes a restaurant card from the specified user's favorites list.
     *
     * @param chatId the unique identifier of the user
     * @param tripadvisorId the unique identifier of the restaurant to remove from favorites
     */
    void removeRestaurantCardToFavoriteList(long chatId, int tripadvisorId);

    /**
     * Sets or clears the visited flag for a restaurant card in the specified user's favorites list.
     *
     * @param chatId the unique identifier of the user
     * @param tripadvisorId the unique identifier of the restaurant whose status is being changed
     * @param isVisited {@code true} if the restaurant has been visited; {@code false} otherwise
     */
    void setVisitedStatus(long chatId, int tripadvisorId, boolean isVisited);

    /**
     * Retrieves the full favorites list for the specified user, including visit status for each
     * restaurant.
     *
     * @param chatId the unique identifier of the user
     * @return a list of entries mapping each {@link RestaurantCardDTO} to a {@link Boolean} flag
     *     indicating visit status ({@code true} if the restaurant has been visited)
     */
    List<Map<RestaurantCardDTO, Boolean>> getFavouriteList(long chatId);
}

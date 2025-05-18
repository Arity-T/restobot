package dev.tishenko.restobot.telegram.services;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for user-related operations.
 * Provides methods to create, retrieve, and update user records in the database.
 */
public interface UserDAO {

    /**
     * Persists a new user record.
     *
     * @param userDTO
     *        the {@link UserDTO} containing all necessary user details to store.
     */
    void addUserToDB(UserDTO userDTO);

    /**
     * Retrieves a user record by its chat identifier.
     *
     * @param chatId
     *        the unique Telegram chat identifier of the user.
     * @return an {@link Optional} containing the {@link UserDTO} if found,
     *         or an empty {@code Optional} if no matching record exists.
     */
    Optional<UserDTO> getUserFromDB(long chatId);

    /**
     * Updates the stored city name for an existing user.
     *
     * @param chatId
     *        the unique Telegram chat identifier of the user whose city will be updated.
     * @param city
     *        the new city value to assign.
     */
    void setNewUserCity(long chatId, String city);

    /**
     * Updates the preferred kitchen or cuisine types for an existing user.
     *
     * @param chatId
     *        the unique Telegram chat identifier of the user.
     * @param kitchenTypes
     *        a {@link List} of cuisine type names to store as the user's preferences.
     */
    void setNewUserKitchenTypes(long chatId, List<String> kitchenTypes);

    /**
     * Updates the preferred price categories for an existing user.
     *
     * @param chatId
     *        the unique Telegram chat identifier of the user.
     * @param priceCategories
     *        a {@link List} of price category identifiers to store as the user's preferences.
     */
    void setNewUserPriceCategories(long chatId, List<String> priceCategories);

    /**
     * Updates the search keywords associated with an existing user.
     *
     * @param chatId
     *        the unique Telegram chat identifier of the user.
     * @param keyWords
     *        a {@link List} of keyword strings to store for search filtering.
     */
    void setNewUserKeyWords(long chatId, List<String> keyWords);

    /**
     * Sets the internal state value for a user.
     *
     * @param chatId
     *        the unique Telegram chat identifier of the user.
     * @param state
     *        a non-null, non-empty string representing the user's current workflow state.
     */
    void setUserState(long chatId, String state);

}
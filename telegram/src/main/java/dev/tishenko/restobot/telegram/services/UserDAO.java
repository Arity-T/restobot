package dev.tishenko.restobot.telegram.services;

import java.util.List;
import java.util.Optional;

/**
 * Service Data Access Object for managing user records in the database.
 *
 * <p>Provides methods to add a new user, check for user existence, and update user preferences such
 * as city, kitchen types, price categories, and search keywords.
 */
public interface UserDAO {

    /**
     * Adds a new user record to the database.
     *
     * @param userDTO the {@link UserDTO} containing user details to persist
     */
    void addUserToDB(UserDTO userDTO);

    /**
     * Checks whether a user exists in the database and retrieves their profile if present.
     *
     * @param chatId the unique identifier of the user to look up
     * @return an {@link Optional} containing the {@link UserDTO} if the user exists; otherwise, an
     *     empty {@code Optional}
     */
    Optional<UserDTO> getUserFromDB(int chatId);

    /**
     * Updates the city for an existing user.
     *
     * @param chatId the unique identifier of the user whose city is to be updated
     * @param city the new city name to assign to the user
     */
    void setNewUserCity(int chatId, String city);

    /**
     * Updates the list of preferred kitchen types for an existing user.
     *
     * @param chatId the unique identifier of the user whose preferences are updated
     * @param kitchenTypes a list of kitchen or cuisine types to assign to the user
     */
    void setNewUserKitchenTypes(int chatId, List<String> kitchenTypes);

    /**
     * Updates the list of preferred price categories for an existing user.
     *
     * @param chatId the unique identifier of the user whose preferences are updated
     * @param priceCategories a list of price category identifiers to assign to the user
     */
    void setNewUserPriceCategories(int chatId, List<String> priceCategories);

    /**
     * Updates the list of search keywords for an existing user.
     *
     * @param chatId the unique identifier of the user whose keywords are updated
     * @param keyWords a list of search keywords to assign to the user
     */
    void setNewUserKeyWords(int chatId, List<String> keyWords);
}

package dev.tishenko.restobot.telegram.config;

import dev.tishenko.restobot.telegram.services.*;

/** Configuration class for Telegram bot within a Spring context. */
public class BotConfig {
    private final String botToken;
    private final String botUsername;
    private final FavoriteListDAO favoriteListDAO;
    private final RestaurantCardFinder restaurantCardFinder;
    private final UserDAO userDAO;
    private final UserParamsValidator userParamsValidator;
    private final SearchParametersService searchParametersService;

    public BotConfig(
            String botToken,
            String botUsername,
            FavoriteListDAO favoriteListDAO,
            RestaurantCardFinder restaurantCardFinder,
            UserDAO userDAO,
            UserParamsValidator userParamsValidator,
            SearchParametersService searchParametersService) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.favoriteListDAO = favoriteListDAO;
        this.restaurantCardFinder = restaurantCardFinder;
        this.userDAO = userDAO;
        this.userParamsValidator = userParamsValidator;
        this.searchParametersService = searchParametersService;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public FavoriteListDAO getFavoriteListDAO() {
        return favoriteListDAO;
    }

    public RestaurantCardFinder getRestaurantCardFinder() {
        return restaurantCardFinder;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public UserParamsValidator getUserParamsValidator() {
        return userParamsValidator;
    }

    public SearchParametersService getSearchParametersService() {
        return searchParametersService;
    }
}

package dev.tishenko.restobot.telegram.config;

import dev.tishenko.restobot.telegram.RestoBot;
import dev.tishenko.restobot.telegram.services.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Spring configuration for the RestoBot Telegram bot. This class sets up the Spring beans needed
 * for the bot to work. It can be imported into a Spring application to enable the Telegram bot
 * functionality.
 */
@Configuration
public class BotFactoryConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /** Creates a BotConfig bean that holds all the configuration for the bot. */
    @Bean
    public BotConfig botConfig(
            @Value("${TELEGRAM_BOT_TOKEN}") String botToken,
            @Value("${TELEGRAM_BOT_USERNAME}") String botUsername,
            FavoriteListDAO favoriteListDAO,
            RestaurantCardFinder restaurantCardFinder,
            UserDAO userDAO,
            SearchParametersService searchParametersService) {

        return new BotConfig(
                botToken,
                botUsername,
                favoriteListDAO,
                restaurantCardFinder,
                userDAO,
                searchParametersService);
    }

    @Bean
    public RestoBotUserHandler restoBotUserHandler(
            FavoriteListDAO favoriteListDAO,
            RestaurantCardFinder restaurantCardFinder,
            UserDAO userDAO,
            SearchParametersService searchParametersService)
            throws Exception {
        return new RestoBotUserHandler(
                favoriteListDAO, restaurantCardFinder, userDAO, searchParametersService);
    }

    @Bean
    public RestoBot restoBot(BotConfig botConfig) {
        return new RestoBot(botConfig);
    }
}

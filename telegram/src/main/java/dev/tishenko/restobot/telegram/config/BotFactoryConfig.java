package dev.tishenko.restobot.telegram.config;

import dev.tishenko.restobot.telegram.RestoBot;
import dev.tishenko.restobot.telegram.services.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan("dev.tishenko.restobot.telegram")
public class BotFactoryConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public RestoBot restoBot(
            @Value("${TELEGRAM_BOT_TOKEN}") String botToken,
            @Value("${TELEGRAM_BOT_USERNAME}") String botUsername,
            FavoriteListDAO favoriteListDAO,
            RestaurantCardFinder restaurantCardFinder,
            UserDAO userDAO,
            UserParamsValidator userParamsValidator,
            SearchParametersService searchParametersService) {
        return new RestoBot(
                botToken,
                botUsername,
                favoriteListDAO,
                restaurantCardFinder,
                userDAO,
                userParamsValidator,
                searchParametersService);
    }
}

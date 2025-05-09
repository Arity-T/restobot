package dev.tishenko.restobot.telegram.RestoBot.config;
import dev.tishenko.restobot.telegram.RestoBot.RestoBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;


@Configuration
@ComponentScan(basePackages = {
        "dev.tishenko.restobot.telegram",
        "dev.tishenko.restobot.telegram.config",
        "dev.tishenko.restobot.telegram.RestoBot.config",
})
public class BotFactoryConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public RestoBot restoBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            RestoBotConfig restoBotConfig
    ) {
        return new RestoBot(botToken, botUsername, restoBotConfig);
    }
}

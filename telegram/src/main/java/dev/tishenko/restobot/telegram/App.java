package dev.tishenko.restobot.telegram;

import dev.tishenko.restobot.telegram.config.BotFactoryConfig;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@Component
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Starting telegram bot application...");

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(BotFactoryConfig.class);

        String botToken =
                context.getEnvironment()
                        .getProperty("TELEGRAM_BOT_TOKEN");

        if (botToken == null || botToken.isEmpty()) {
            logger.error("TELEGRAM_BOT_TOKEN is not set");
            throw new RuntimeException("TELEGRAM_BOT_TOKEN is not set");
        }


        RestoBot bot = context.getBean(RestoBot.class);
        try (TelegramBotsLongPollingApplication botsApplication =
                new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, bot);
            logger.info(bot.getBotUserName() + " successfully started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Error starting bot: {}", e.getMessage());
        }
        context.close();
    }
}

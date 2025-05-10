package dev.tishenko.restobot.telegram;

import dev.tishenko.restobot.telegram.RestoBot.RestoBot;
import dev.tishenko.restobot.telegram.RestoBot.config.BotFactoryConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.util.Objects;


@Component
public class App {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BotFactoryConfig.class);

        String botToken = context.getEnvironment().getProperty("TELEGRAM_BOT_TOKEN", Objects.requireNonNull(context.getEnvironment()
                .getProperty("TELEGRAM_BOT_TOKEN")));

        RestoBot bot = context.getBean(RestoBot.class);
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, bot);
            System.out.println(bot.getUsername() + " successfully started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.close();
    }
}

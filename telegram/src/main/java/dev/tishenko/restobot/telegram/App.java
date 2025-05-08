package dev.tishenko.restobot.telegram;

import dev.tishenko.restobot.telegram.RestoBot.RestoBot;
import dev.tishenko.restobot.telegram.RestoBot.config.BotFactoryConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class App {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BotFactoryConfig.class);

        try {
            RestoBot bot = context.getBean(RestoBot.class);
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.close();
    }
}

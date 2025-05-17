package dev.tishenko.restobot.telegram.example;

import dev.tishenko.restobot.telegram.RestoBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AppExample {
    private static final Logger logger = LoggerFactory.getLogger(AppExample.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfigExample.class);
        logger.info("Starting telegram bot application...");
        RestoBot restoBot = context.getBean(RestoBot.class);
        restoBot.start();
    }
}

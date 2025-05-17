package dev.tishenko.restobot.telegram.example;

import dev.tishenko.restobot.telegram.RestoBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Example application that demonstrates how to start the RestoBot. This uses Spring context with
 * the provided example implementations.
 */
public class AppExample {
    private static final Logger logger = LoggerFactory.getLogger(AppExample.class);

    public static void main(String[] args) {
        logger.info("Starting telegram bot application with Spring context...");

        try (AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfigExample.class)) {
            RestoBot restoBot = context.getBean(RestoBot.class);
            restoBot.start();
        }
    }
}

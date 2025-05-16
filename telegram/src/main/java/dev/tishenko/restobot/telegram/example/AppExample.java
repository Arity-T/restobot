package dev.tishenko.restobot.telegram.example;

import dev.tishenko.restobot.telegram.RestoBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppExample {
    private static final Logger logger = LoggerFactory.getLogger(AppExample.class);

    public static void main(String[] args) {
        logger.info("Starting telegram bot application...");
        RestoBot.start();
    }
}

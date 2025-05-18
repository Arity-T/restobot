package dev.tishenko.restobot.telegram.example;

import dev.tishenko.restobot.telegram.config.BotFactoryConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Example Spring configuration for the Telegram bot. This configuration imports the
 * BotFactoryConfig and scans the example implementations.
 */
@Configuration
@Import(BotFactoryConfig.class)
@ComponentScan("dev.tishenko.restobot.telegram.example.impl")
public class AppConfigExample {}

package dev.tishenko.restobot.logic.example.config;

import dev.tishenko.restobot.api.example.ApiAppConfigExample;
import dev.tishenko.restobot.logic.config.LogicConfig;
import dev.tishenko.restobot.telegram.config.BotFactoryConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ApiAppConfigExample.class, LogicConfig.class, BotFactoryConfig.class})
public class AppConfig {}

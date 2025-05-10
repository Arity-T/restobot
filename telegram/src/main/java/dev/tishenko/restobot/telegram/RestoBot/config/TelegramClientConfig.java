package dev.tishenko.restobot.telegram.RestoBot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class TelegramClientConfig {

    @Bean
    public TelegramClient telegramClient(@Value("${TELEGRAM_BOT_TOKEN}") String token) {
        return new OkHttpTelegramClient(token);
    }

}

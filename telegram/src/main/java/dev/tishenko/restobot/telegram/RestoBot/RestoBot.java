package dev.tishenko.restobot.telegram.RestoBot;

import dev.tishenko.restobot.telegram.RestoBot.config.RestoBotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Component
public class RestoBot implements LongPollingUpdateConsumer {
    private Executor updatesProcessorExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final String botToken;
    private final String botUsername;

    private RestoBotConfig botConfig;
    private final TelegramClient telegramClient;


    public RestoBot(@Value("${telegram.bot.token}") String botToken,
                        @Value("${telegram.bot.username}") String botUsername,
                        RestoBotConfig botConfig) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.botConfig = botConfig;
        telegramClient = new OkHttpTelegramClient(botToken);
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, this);
            System.out.println(botUsername + " successfully started!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(update -> updatesProcessorExecutor.execute(() -> consume(update)));
    }

    public void consume(Update update){

    }
}

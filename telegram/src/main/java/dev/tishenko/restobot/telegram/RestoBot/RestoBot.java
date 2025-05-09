package dev.tishenko.restobot.telegram.RestoBot;

import dev.tishenko.restobot.telegram.RestoBot.config.RestoBotConfig;
import dev.tishenko.restobot.telegram.config.UserData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.lang.Math.toIntExact;


@Component
public class RestoBot implements LongPollingUpdateConsumer {
    private Executor updatesProcessorExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final String botToken;
    private final String botUsername;
    private RestoBotConfig botConfig;
    private final TelegramClient telegramClient;
    private UserData userData;

    public String getUsername() {
        return botUsername;
    }

    public RestoBot(@Value("${telegram.bot.token}") String botToken,
                    @Value("${telegram.bot.username}") String botUsername,
                    RestoBotConfig botConfig) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.botConfig = botConfig;
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(update -> updatesProcessorExecutor.execute(() -> consume(update)));
    }

    public void consume(Update update) {
        System.out.println("update");
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (update.getMessage().getText().equals("/start")) {
                userData = new UserData(update.getMessage().getChatId(), update.getMessage().getChat().getUserName());
                SendMessage greetingString = RestoBotConfig.greetingMessage(userData);
                try {
                    telegramClient.execute(greetingString);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else if (update.hasCallbackQuery()) {
            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            if (call_data.equals("backButton")) {
                String answer = "Updated message text";
                EditMessageText new_message = EditMessageText.builder()
                        .chatId(chat_id)
                        .messageId(toIntExact(message_id))
                        .text(answer)
                        .build();
                try {
                    telegramClient.execute(new_message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

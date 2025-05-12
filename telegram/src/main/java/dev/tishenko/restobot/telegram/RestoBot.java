package dev.tishenko.restobot.telegram;

import dev.tishenko.restobot.telegram.config.RestoBotConfig;
import dev.tishenko.restobot.telegram.config.UserData;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class RestoBot implements LongPollingUpdateConsumer {
    private static final Logger logger = LoggerFactory.getLogger(RestoBot.class);

    private Executor updatesProcessorExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final String botToken;
    private final String botUsername;
    private RestoBotConfig botConfig;
    private final TelegramClient telegramClient;
    private UserData userData;
    private long lastMessageId;

    public String getUsername() {
        return botUsername;
    }

    public RestoBot(
            @Value("${TELEGRAM_BOT_TOKEN}") String botToken,
            @Value("${TELEGRAM_BOT_USERNAME}") String botUsername,
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (update.getMessage().getText().equals("/start") && userData == null) {
                userData =
                        new UserData(
                                update.getMessage().getChatId(),
                                update.getMessage().getChat().getUserName());
                SendMessage greetingString = RestoBotConfig.greetingMessage(userData);
                try {
                    telegramClient.execute(greetingString);
                } catch (TelegramApiException e) {
                    logger.error("Error sending greeting message: {}", e.getMessage());
                }
            } else if (RestoBotConfig.isSettingUserParams()) {
                logger.debug("Setting user params: {}", update.getMessage().getText());
                EditMessageText editMessageText =
                        RestoBotConfig.nextState(update, lastMessageId, true, userData);
                DeleteMessage deleteMessage =
                        DeleteMessage.builder()
                                .chatId(userData.getChatID())
                                .messageId(update.getMessage().getMessageId())
                                .build();
                try {
                    telegramClient.execute(editMessageText);
                    telegramClient.execute(deleteMessage);
                } catch (TelegramApiException e) {
                    logger.error("Error editing message: {}", e.getMessage());
                }
            } else {
                DeleteMessage deleteMessage =
                        DeleteMessage.builder()
                                .chatId(userData.getChatID())
                                .messageId(update.getMessage().getMessageId())
                                .build();
                try {
                    telegramClient.execute(deleteMessage);
                } catch (TelegramApiException e) {
                    logger.error("Error deleting message: {}", e.getMessage());
                }
            }
        } else if (update.hasCallbackQuery()) {
            logger.debug("Callback query: {}", update.getCallbackQuery().getData());
            lastMessageId = update.getCallbackQuery().getMessage().getMessageId();
            EditMessageText editMessageText =
                    RestoBotConfig.nextState(update, lastMessageId, false, userData);
            if (!editMessageText.getText().equals("Incorrect state")) {
                try {
                    telegramClient.execute(editMessageText);
                } catch (TelegramApiException e) {
                    logger.error("Error editing message: {}", e.getMessage());
                }
            }
        } else if (update.getMessage().hasLocation() && RestoBotConfig.isSettingLocation()) {
            EditMessageText editMessageText =
                    RestoBotConfig.nextState(update, lastMessageId, true, userData);
            DeleteMessage deleteMessage =
                    DeleteMessage.builder()
                            .chatId(userData.getChatID())
                            .messageId(update.getMessage().getMessageId())
                            .build();
            if (!editMessageText.getText().equals("Incorrect state")) {
                try {
                    telegramClient.execute(editMessageText);
                    telegramClient.execute(deleteMessage);
                } catch (TelegramApiException e) {
                    logger.error("Error editing message: {}", e.getMessage());
                }
            }
        } else {
            DeleteMessage deleteMessage =
                    DeleteMessage.builder()
                            .chatId(userData.getChatID())
                            .messageId(update.getMessage().getMessageId())
                            .build();
            try {
                telegramClient.execute(deleteMessage);
            } catch (TelegramApiException e) {
                logger.error("Error deleting message: {}", e.getMessage());
            }
        }
    }
}

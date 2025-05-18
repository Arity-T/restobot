package dev.tishenko.restobot.telegram;

import dev.tishenko.restobot.telegram.config.BotConfig;
import dev.tishenko.restobot.telegram.config.RestoBotUserHandler;
import dev.tishenko.restobot.telegram.config.UserData;
import dev.tishenko.restobot.telegram.services.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Main class for the RestoBot Telegram bot. Handles all interactions with the Telegram API and
 * routes user messages to the appropriate handlers.
 */
public class RestoBot implements LongPollingUpdateConsumer, BotFacade {
    private static final Logger logger = LoggerFactory.getLogger(RestoBot.class);

    private final Executor updatesProcessorExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final String botToken;
    private final String botUsername;
    private final TelegramClient telegramClient;

    private final Map<Long, UserData> userData = new ConcurrentHashMap<>();
    private final Map<Long, Integer> lastMessageId = new ConcurrentHashMap<>();
    private final Map<Long, RestoBotUserHandler> botConfig = new ConcurrentHashMap<>();

    private final FavoriteListDAO favoriteListDAO;
    private final RestaurantCardFinder restaurantCardFinder;
    private final UserDAO userDAO;
    private final SearchParametersService searchParametersService;

    /**
     * Creates a new RestoBot instance using the provided configuration.
     *
     * @param config The bot configuration
     */
    public RestoBot(BotConfig config) {
        this.botToken = config.getBotToken();
        this.botUsername = config.getBotUsername();
        this.favoriteListDAO = config.getFavoriteListDAO();
        this.restaurantCardFinder = config.getRestaurantCardFinder();
        this.userDAO = config.getUserDAO();
        this.searchParametersService = config.getSearchParametersService();

        telegramClient = new OkHttpTelegramClient(botToken);
    }

    /** Returns the bot username. */
    public String getBotUserName() {
        return botUsername;
    }

    /** Starts the bot and listens for incoming messages. */
    public void start() {
        try (TelegramBotsLongPollingApplication botsApplication =
                new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, this);
            logger.info(botUsername + " successfully started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Error starting bot: {}", e.getMessage());
        }
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(
                update ->
                        updatesProcessorExecutor.execute(
                                () -> {
                                    try {
                                        consume(update);
                                    } catch (MalformedURLException | URISyntaxException e) {
                                        logger.error("Error URL handle: {}", e.getMessage());
                                    }
                                }));
    }

    private void consume(Update update) throws MalformedURLException, URISyntaxException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            logger.debug("Updated by user {}", update.getMessage().getChatId());
            long chatId = update.getMessage().getChatId();
            if (update.getMessage().getText().equals("/start")) {
                Optional<UserDTO> userDTO = userDAO.getUserFromDB(chatId);
                if (userDTO.isEmpty()) {
                    userData.put(
                            chatId,
                            new UserData(
                                    chatId,
                                    update.getMessage().getChat().getUserName(),
                                    userDAO,
                                    favoriteListDAO,
                                    searchParametersService));
                    userDAO.addUserToDB(userData.get(chatId).toUserDTO());
                } else {
                    if (!userData.containsKey(chatId)) {
                        userData.put(
                                chatId,
                                new UserData(
                                        chatId,
                                        update.getMessage().getChat().getUserName(),
                                        userDTO.get(),
                                        userDAO,
                                        favoriteListDAO,
                                        searchParametersService));
                    }
                    else {
                        userData.replace(chatId, new UserData(
                                chatId,
                                update.getMessage().getChat().getUserName(),
                                userDTO.get(),
                                userDAO,
                                favoriteListDAO,
                                searchParametersService));
                    }
                }

                botConfig.put(
                        chatId,
                        new RestoBotUserHandler(
                                favoriteListDAO,
                                restaurantCardFinder,
                                userDAO,
                                searchParametersService));
                SendMessage greetingString =
                        botConfig.get(chatId).greetingMessage(userData.get(chatId));
                logger.debug("User {} was registered", update.getMessage().getChat().getUserName());
                try {
                    telegramClient.execute(greetingString);
                } catch (TelegramApiException e) {
                    logger.error("Error sending greeting message: {}", e.getMessage());
                }
            } else if (botConfig.get(chatId).isSettingUserParams()) {
                logger.debug("Setting user params: {}", update.getMessage().getText());
                EditMessageText editMessageText =
                        botConfig
                                .get(chatId)
                                .nextState(
                                        update,
                                        lastMessageId.get(chatId),
                                        true,
                                        userData.get(chatId));
                DeleteMessage deleteMessage =
                        DeleteMessage.builder()
                                .chatId(chatId)
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
                                .chatId(chatId)
                                .messageId(update.getMessage().getMessageId())
                                .build();
                try {
                    telegramClient.execute(deleteMessage);
                } catch (TelegramApiException e) {
                    logger.error("Error deleting message: {}", e.getMessage());
                }
            }
        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            logger.debug(
                    "Callback query: {} by user {}", update.getCallbackQuery().getData(), chatId);
            lastMessageId.put(chatId, update.getCallbackQuery().getMessage().getMessageId());
            EditMessageText editMessageText =
                    botConfig
                            .get(chatId)
                            .nextState(
                                    update, lastMessageId.get(chatId), false, userData.get(chatId));
            if (!editMessageText.getText().equals("Incorrect state")) {
                try {
                    telegramClient.execute(editMessageText);
                } catch (TelegramApiException e) {
                    logger.error("Error editing message: {}", e.getMessage());
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasLocation()
                && botConfig.get(update.getMessage().getChatId()).isSettingLocation()) {
            long chatId = update.getMessage().getChatId();
            EditMessageText editMessageText =
                    botConfig
                            .get(chatId)
                            .nextState(
                                    update, lastMessageId.get(chatId), true, userData.get(chatId));
            DeleteMessage deleteMessage =
                    DeleteMessage.builder()
                            .chatId(chatId)
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
            if(update.getMessage() != null){
                long chatId = update.getMessage().getChatId();
                DeleteMessage deleteMessage =
                        DeleteMessage.builder()
                                .chatId(chatId)
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
}

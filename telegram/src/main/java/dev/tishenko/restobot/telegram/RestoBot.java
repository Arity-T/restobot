package dev.tishenko.restobot.telegram;

import dev.tishenko.restobot.telegram.config.BotFactoryConfig;
import dev.tishenko.restobot.telegram.config.RestoBotUserHandlerConfig;
import dev.tishenko.restobot.telegram.config.UserData;
import dev.tishenko.restobot.telegram.services.*;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
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

    private static String botToken;
    private final String botUsername;
    private final TelegramClient telegramClient;

    private Map<Long, UserData> userData;
    private Map<Long, Integer> lastMessageId;
    private Map<Long, RestoBotUserHandlerConfig> botConfig;

    private FavoriteListDAO favoriteListDAO;
    private RestaurantCardFinder restaurantCardFinder;
    private UserDAO userDAO;
    private UserParamsValidator userParamsValidator;
    private SearchParametersService searchParametersService;

    public String getBotUserName() {
        return botUsername;
    }

    public static void start() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(BotFactoryConfig.class);

        RestoBot bot = context.getBean(RestoBot.class);
        try (TelegramBotsLongPollingApplication botsApplication =
                new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, bot);
            logger.info(bot.getBotUserName() + " successfully started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Error starting bot: {}", e.getMessage());
        }
    }

    public RestoBot(
            @Value("${TELEGRAM_BOT_TOKEN}") String botToken,
            @Value("${TELEGRAM_BOT_USERNAME}") String botUsername,
            RestoBotUserHandlerConfig botConfig,
            FavoriteListDAO favoriteListDAO,
            RestaurantCardFinder restaurantCardFinder,
            UserDAO userDAO,
            UserParamsValidator userParamsValidator,
            SearchParametersService searchParametersService ) {

        this.botToken = botToken;
        this.botUsername = botUsername;
        this.botConfig = new ConcurrentHashMap<>();
        this.userData = new ConcurrentHashMap<>();
        this.lastMessageId = new ConcurrentHashMap<>();

        this.favoriteListDAO = favoriteListDAO;
        this.restaurantCardFinder = restaurantCardFinder;
        this.userDAO = userDAO;
        this.userParamsValidator = userParamsValidator;
        this.searchParametersService = searchParametersService;

        telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(
                update ->
                        updatesProcessorExecutor.execute(
                                () -> {
                                    try {
                                        consume(update);
                                    } catch (MalformedURLException e) {
                                        logger.error("Error URL handle: {}", e.getMessage());
                                    }
                                }));
    }

    private void consume(Update update) throws MalformedURLException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            logger.debug("Updated by user {}", update.getMessage().getChatId());
            long chatId = update.getMessage().getChatId();
            if (update.getMessage().getText().equals("/start") && !userData.containsKey(chatId)) {
                Optional<UserDTO> userDTO = userDAO.getUserFromDB(chatId);
                if(userDTO.isEmpty()){
                    userData.put(
                            chatId, new UserData(chatId, update.getMessage().getChat().getUserName()));
                    userDAO.addUserToDB(userData.get(chatId).toUserDTO());
                }
                else {
                    userData.put(
                            chatId, new UserData(chatId, update.getMessage().getChat().getUserName(), userDTO.get()));
                }

                botConfig.put(chatId, new RestoBotUserHandlerConfig());
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
        } else if (update.getMessage().hasLocation()
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

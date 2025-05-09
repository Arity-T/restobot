package dev.tishenko.restobot.telegram.RestoBot.config;

import dev.tishenko.restobot.telegram.config.RestaurantCard;
import dev.tishenko.restobot.telegram.config.UserData;
import org.springframework.context.annotation.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.toIntExact;


@Configuration
@Scope("singleton")
public class RestoBotConfig {

    private static String actualState;
    private static String lastParams;
    private static Map<String, List<String>> states;


    private void initStates() {
        states = new HashMap<>();
        states.put("/start", List.of("goToUserParamsButton")); // Greeting
        states.put("goToUserParamsButton", List.of(
                "setCityInUserParamsButton",
                "setKitchenTypesInUserParamsButton",
                "setPriceCategoriesInUserParamsButton",
                "setKeyWordsInUserParamsButton",
                "goToMenuButton")); // User params
        states.put("setCityInUserParamsButton", List.of(
                "goToUserParamsButton",
                "setCityInUserParamsButton")); // Set city (userData params)
        states.put("setKitchenTypesInUserParamsButton", List.of(
                "goToUserParamsButton",
                "setKitchenTypesInUserParamsButton")); // Set kitchen type (userData params)
        states.put("setPriceCategoriesInUserParamsButton", List.of(
                "goToUserParamsButton",
                "setPriceCategoriesInUserParamsButton")); // Set price category (userData params)
        states.put("setKeyWordsInUserParamsButton", List.of(
                "goToUserParamsButton")); // Set keywords (userData params)
        states.put("goToMenuButton", List.of(
                "goToUserParamsButton",
                "goToFavouriteListButton",
                "randomRestaurantButton",
                "restaurantSearchButton")); // Menu
        states.put("goToFavouriteListButton", List.of(
                "goToMenuButton",
                "removeFromFavouriteListButton")); // Fav list
        states.put("removeFromFavouriteListButton", List.of(
                "nextRestaurantFavouriteListButton")); // Remove from Fav list
        states.put("nextRestaurantFavouriteListButton", List.of(
                "goToFavouriteListButton"));
        states.put("setAsVisitedButton", List.of(
                "goToFavouriteListButton")); // Set as visited/non-visited
        states.put("randomRestaurantButton", List.of(
                "goToUserParamsButton")); // Random rest
        states.put("randomRestaurantSearch", List.of(
                "randomRestaurantButton",
                "nextRandomRestaurantButton",
                "addRandomRestaurantToFavouriteListButton"));// Rest card (Random rest)
        states.put("nextRandomRestaurantButton", List.of(
                "randomRestaurantSearch")); // Next rest card (Random rest)
        states.put("restaurantSearchButton", List.of(
                "goToMenuButton",
                "setCityRestaurantSearchButton",
                "setKitchenTypesRestaurantSearchButton",
                "setPriceCategoriesRestaurantSearchButton",
                "setKeyWordsRestaurantSearchButton",
                "searchButton")); // Search crit
        states.put("setCityRestaurantSearchButton", List.of(
                "restaurantSearchButton",
                "setCityRestaurantSearchButton",
                "setDefaultButton",
                "setDisabledButton")); // Set city (Search crit)
        states.put("setKitchenTypesRestaurantSearchButton", List.of(
                "restaurantSearchButton",
                "setKitchenTypesRestaurantSearchButton",
                "setDefaultButton",
                "setDisabledButton"));// Set kitchen type (Search crit),
        states.put("setPriceCategoriesRestaurantSearchButton", List.of(
                "restaurantSearchButton",
                "setPriceCategoriesRestaurantSearchButton",
                "setDefaultButton",
                "setDisabledButton")); // Set price category (Search crit)
        states.put("setKeyWordsRestaurantSearchButton", List.of(
                "restaurantSearchButton",
                "setDefaultButton",
                "setDisabledButton")); // Set keywords (Search crit)
        states.put("searchButton", List.of(
                "restaurantSearchButton",
                "nextRestaurantSearchButton",
                "addRestaurantSearchToFavouriteListButton")); // Rest card (Search crit)
        states.put("nextRestaurantSearchButton", List.of(
                "restaurantSearchButton")); // Next rest card (Search crit)
    }

    public RestoBotConfig() {
        initStates();
        actualState = "/start";
    }

    public static SendMessage greetingMessage(UserData userData) {
        actualState = "goToUserParamsButton";
        return SendMessage
                .builder()
                .chatId(userData.getChatID())
                .text("Здравствуйте!" + "\n" +
                        "Я Ваш личный помощник в подборе ресторана. Вы можете задать параметры поиска ресторана или же пропустить этот шаг." + "\n" +
                        "Ваши настройки: \n" + userData.userParamsToString())
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(setCityInUserParamsButton))
                        .keyboardRow(new InlineKeyboardRow(setKitchenTypesInUserParamsButton))
                        .keyboardRow(new InlineKeyboardRow(setCityInUserParamsButton))
                        .keyboardRow(new InlineKeyboardRow(setCityInUserParamsButton))
                        .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                        .build())
                .build();
    }

    public static EditMessageText nextState(String message, long messageId, boolean isText, UserData userData) {
        long chatId = userData.getChatID();
        if (isText) {

        } else if (states.get(actualState).contains(message)) {
            actualState = message;
            return actionChose(message, messageId, chatId, userData);
        }
        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(toIntExact(messageId))
                .text("Ошибка ввода")
                .build();
    }

    public static EditMessageText actionChose(String message, long messageId, long chatId, UserData userData) {
        switch (message) {
            case "goToUserParamsButton" -> {
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text("Выберите настройки, которые хотите изменить. \n" +
                                "Ваши настройки: \n" + userData.userParamsToString())
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(new InlineKeyboardRow(setCityInUserParamsButton))
                                .keyboardRow(new InlineKeyboardRow(setKitchenTypesInUserParamsButton))
                                .keyboardRow(new InlineKeyboardRow(setPriceCategoriesInUserParamsButton))
                                .keyboardRow(new InlineKeyboardRow(setKeyWordsInUserParamsButton))
                                .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                .build())
                        .build();
            }
            case "setCityInUserParamsButton" -> {
                lastParams = "City";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text("Ведите город.")
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(new InlineKeyboardRow(cancelUserParamsButton))
                                .build())
                        .build();
            }
            case "setKitchenTypesInUserParamsButton" -> {
                lastParams = "KitchenTypes";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text("Введите типы кухни.")
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(new InlineKeyboardRow(cancelUserParamsButton))
                                .build())
                        .build();
            }
            case "setPriceCategoriesInUserParamsButton" -> {
                lastParams = "PriceCategories";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text("Введите ценовые категории.")
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(new InlineKeyboardRow(cancelUserParamsButton))
                                .build())
                        .build();
            }
            case "setKeyWordsInUserParamsButton" -> {
                lastParams = "KeyWords";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text("Введите ключевые слова.")
                        .build();
            }
            case "goToMenuButton" -> {
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text("Выберите действие:")
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(new InlineKeyboardRow(goToUserParamsButton))
                                .keyboardRow(new InlineKeyboardRow(goToFavouriteListButton))
                                .keyboardRow(new InlineKeyboardRow(randomRestaurantButton))
                                .keyboardRow(new InlineKeyboardRow(restaurantSearchButton))
                                .build())
                        .build();
            }
            case "goToFavouriteListButton" -> {
                if (userData.getFavoriteList() == null || userData.getFavoriteList().isEmpty()){
                    return EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(toIntExact(messageId))
                            .text("Список пуст")
                            .replyMarkup(InlineKeyboardMarkup
                                    .builder()
                                    .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                    .build())
                            .build();
                }
                RestaurantCard restaurantCard= userData.nextRestaurantFromFavoriteList();
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(restaurantCard.restaurantCardToString())
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(new InlineKeyboardRow(nextRestaurantFavouriteListButton))
                                .keyboardRow(new InlineKeyboardRow(restaurantCard.isVisited() ? setAsNonVisitedButton : setAsVisitedButton))
                                .keyboardRow(new InlineKeyboardRow(removeFromFavouriteListButton))
                                .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                .build())
                        .build();
            }
            case "removeFromFavouriteListButton" -> {
                userData.removeRestaurantFromFavouriteListByIndex();
                RestaurantCard restaurantCard= userData.nextRestaurantFromFavoriteList();
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(restaurantCard.restaurantCardToString())
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(new InlineKeyboardRow(nextRestaurantFavouriteListButton))
                                .keyboardRow(new InlineKeyboardRow(restaurantCard.isVisited() ? setAsNonVisitedButton : setAsVisitedButton))
                                .keyboardRow(new InlineKeyboardRow(removeFromFavouriteListButton))
                                .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                .build())
                        .build();
            }
            case "setAsVisitedButton" -> {
                userData.getRestaurantFromFavouriteListByIndex().changeIsVisited();
                RestaurantCard restaurantCard = userData.getRestaurantFromFavouriteListByIndex();
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(restaurantCard.restaurantCardToString())
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(new InlineKeyboardRow(nextRestaurantFavouriteListButton))
                                .keyboardRow(new InlineKeyboardRow(restaurantCard.isVisited() ? setAsNonVisitedButton : setAsVisitedButton))
                                .keyboardRow(new InlineKeyboardRow(removeFromFavouriteListButton))
                                .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                .build())
                        .build();
            }

            default -> {
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text("Incorrect state")
                        .build();
            }
        }
    }


    public static InlineKeyboardButton setCityInUserParamsButton = InlineKeyboardButton
            .builder()
            .text("Задать город")
            .callbackData("setCityInUserParamsButton")
            .build();

    public static InlineKeyboardButton setKitchenTypesInUserParamsButton = InlineKeyboardButton
            .builder()
            .text("Задать типы кухни")
            .callbackData("setKitchenTypesInUserParamsButton")
            .build();

    public static InlineKeyboardButton setPriceCategoriesInUserParamsButton = InlineKeyboardButton
            .builder()
            .text("Задать ценовые категории")
            .callbackData("setPriceCategoriesInUserParamsButton")
            .build();

    public static InlineKeyboardButton setKeyWordsInUserParamsButton = InlineKeyboardButton
            .builder()
            .text("Задать Ключевые слова")
            .callbackData("setKeyWordsInUserParamsButton")
            .build();

    public static InlineKeyboardButton cancelUserParamsButton = InlineKeyboardButton
            .builder()
            .text("Отмена")
            .callbackData("goToUserParamsButton")
            .build();

    public static InlineKeyboardButton goToMenuButton = InlineKeyboardButton
            .builder()
            .text("Перейти в меню")
            .callbackData("goToMenuButton")
            .build();

    public static InlineKeyboardButton goToUserParamsButton = InlineKeyboardButton
            .builder()
            .text("Перейти к пользовательским настройкам")
            .callbackData("goToUserParamsButton")
            .build();

    public static InlineKeyboardButton goToFavouriteListButton = InlineKeyboardButton
            .builder()
            .text("Избранные рестораны")
            .callbackData("goToFavouriteListButton")
            .build();

    public static InlineKeyboardButton removeFromFavouriteListButton = InlineKeyboardButton
            .builder()
            .text("Удалить из избранного")
            .callbackData("removeFromFavouriteListButton")
            .build();

    public static InlineKeyboardButton setAsVisitedButton = InlineKeyboardButton
            .builder()
            .text("Отметить как посещенный")
            .callbackData("setAsVisitedButton")
            .build();

    public static InlineKeyboardButton setAsNonVisitedButton = InlineKeyboardButton
            .builder()
            .text("Удалить отметку о посещении")
            .callbackData("setAsVisitedButton")
            .build();

    public static InlineKeyboardButton nextRestaurantFavouriteListButton = InlineKeyboardButton
            .builder()
            .text("Следующий ресторан")
            .callbackData("goToFavouriteListButton")
            .build();

    public static InlineKeyboardButton randomRestaurantButton = InlineKeyboardButton
            .builder()
            .text("Случайный Ресторан")
            .callbackData("randomRestaurantButton")
            .build();
    public static InlineKeyboardButton backToRandomRestaurantButton = InlineKeyboardButton
            .builder()
            .text("Назад")
            .callbackData("randomRestaurantButton")
            .build();

    public static InlineKeyboardButton nextRandomRestaurantButton = InlineKeyboardButton
            .builder()
            .text("Следующий ресторан")
            .callbackData("nextRandomRestaurantButton")
            .build();

    public static InlineKeyboardButton addRandomRestaurantToFavouriteListButton = InlineKeyboardButton
            .builder()
            .text("Добавить в избранное")
            .callbackData("addRandomRestaurantToFavouriteListButton")
            .build();

    public static InlineKeyboardButton removeRandomRestaurantFromFavouriteListButton = InlineKeyboardButton
            .builder()
            .text("Убрать из избранного")
            .callbackData("addRandomRestaurantToFavouriteListButton")
            .build();

    public static InlineKeyboardButton restaurantSearchButton = InlineKeyboardButton
            .builder()
            .text("Поиск ресторанов")
            .callbackData("restaurantSearchButton")
            .build();

    public static InlineKeyboardButton setCityRestaurantSearchButton = InlineKeyboardButton
            .builder()
            .text("Задать город")
            .callbackData("setCityRestaurantSearchButton")
            .build();


    public static InlineKeyboardButton setKitchenTypesRestaurantSearchButton = InlineKeyboardButton
            .builder()
            .text("Задать типы кухни")
            .callbackData("setKitchenTypesRestaurantSearchButton")
            .build();

    public static InlineKeyboardButton setPriceCategoriesRestaurantSearchButton = InlineKeyboardButton
            .builder()
            .text("Задать ценовые категории")
            .callbackData("setPriceCategoriesRestaurantSearchButton")
            .build();

    public static InlineKeyboardButton setKeyWordsRestaurantSearchButton = InlineKeyboardButton
            .builder()
            .text("Задать Ключевые слова")
            .callbackData("setKeyWordsRestaurantSearchButton")
            .build();

    public static InlineKeyboardButton setDefaultButton = InlineKeyboardButton
            .builder()
            .text("По умолчанию")
            .callbackData("setDefaultButton")
            .build();

    public static InlineKeyboardButton setDisabledButton = InlineKeyboardButton
            .builder()
            .text("Отключить")
            .callbackData("setDisabledButton")
            .build();

    public static InlineKeyboardButton backToRestaurantSearchButton = InlineKeyboardButton
            .builder()
            .text("Назад")
            .callbackData("restaurantSearchButton")
            .build();

    public static InlineKeyboardButton searchButton = InlineKeyboardButton
            .builder()
            .text("Поиск")
            .callbackData("searchButton")
            .build();

    public static InlineKeyboardButton nextRestaurantSearchButton = InlineKeyboardButton
            .builder()
            .text("Следующий ресторан")
            .callbackData("nextRestaurantSearchButton")
            .build();

    public static InlineKeyboardButton addRestaurantSearchToFavouriteListButton = InlineKeyboardButton
            .builder()
            .text("Добавить в избранное")
            .callbackData("addRestaurantSearchToFavouriteListButton")
            .build();

    public static InlineKeyboardButton removeRestaurantSearchFromFavouriteListButton = InlineKeyboardButton
            .builder()
            .text("Убрать из избранного")
            .callbackData("addRandomRestaurantToFavouriteListButton")
            .build();

}

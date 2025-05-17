package dev.tishenko.restobot.telegram.config;

import static java.lang.Math.toIntExact;

import dev.tishenko.restobot.telegram.services.*;
import java.net.MalformedURLException;
import java.util.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

public class RestoBotUserHandler {

    private String actualState;
    private String lastParams;
    private List<RestaurantCardDTO> restaurantSelection;
    private int actualIndex;
    private boolean isSettingUserParams;
    private boolean isSettingLocation;

    private FavoriteListDAO favoriteListDAO;
    private RestaurantCardFinder restaurantCardFinder;
    private UserDAO userDAO;
    private SearchParametersService searchParametersService;

    private final String correctCities;
    private final String correctKitchenTypes;
    private final String correctPriceCategories;

    private final String ZWSP = "\u200B";
    private boolean isZWSP = false;

    public RestoBotUserHandler(
            FavoriteListDAO favoriteListDAO,
            RestaurantCardFinder restaurantCardFinder,
            UserDAO userDAO,
            SearchParametersService searchParametersService)
            throws MalformedURLException {
        restaurantSelection = new ArrayList<>();
        isSettingUserParams = false;
        isSettingLocation = false;
        actualState = "/start";

        correctCities =
                searchParametersService
                        .getCitiesNames()
                        .toString()
                        .replace("[", "")
                        .replace("]", "");
        correctKitchenTypes =
                searchParametersService
                        .getKitchenTypesNames()
                        .toString()
                        .replace("[", "")
                        .replace("]", "");
        correctPriceCategories =
                searchParametersService
                        .getPriceCategoriesNames()
                        .toString()
                        .replace("[", "")
                        .replace("]", "");
    }

    public boolean isSettingUserParams() {
        return isSettingUserParams;
    }

    private RestaurantCardDTO nextRestaurantFromSelection() {
        actualIndex = actualIndex >= restaurantSelection.size() - 1 ? 0 : actualIndex + 1;
        return restaurantSelection.get(actualIndex);
    }

    public SendMessage greetingMessage(UserData userData) {
        actualState = "goToUserParamsButton";
        return SendMessage.builder()
                .chatId(userData.getChatID())
                .text(
                        "Здравствуйте!"
                                + "\n"
                                + "Я Ваш личный помощник в подборе ресторана. Вы можете задать параметры поиска ресторана или же пропустить этот шаг."
                                + "\n"
                                + "Ваши настройки: \n"
                                + userData.userParamsToString())
                .replyMarkup(
                        InlineKeyboardMarkup.builder()
                                .keyboardRow(new InlineKeyboardRow(setCityInUserParamsButton))
                                .keyboardRow(
                                        new InlineKeyboardRow(setKitchenTypesInUserParamsButton))
                                .keyboardRow(new InlineKeyboardRow(setCityInUserParamsButton))
                                .keyboardRow(new InlineKeyboardRow(setKeyWordsInUserParamsButton))
                                .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                .build())
                .build();
    }

    public String safeForceEdit(String text) {
        if (isZWSP) {
            isZWSP = false;
            return text;
        }
        isZWSP = true;
        return text + ZWSP;
    }

    public EditMessageText nextState(
            Update message, long messageId, boolean isText, UserData userData)
            throws MalformedURLException {
        long chatId = userData.getChatID();
        if (isText) {
            if (message.getMessage().hasLocation()) {
                restaurantSelection =
                        restaurantCardFinder.getRestaurantCardByGeolocation(
                                message.getMessage().getLocation().getLatitude(),
                                message.getMessage().getLocation().getLongitude());
                return actionChose("randomRestaurantSearch", messageId, chatId, userData);
            }
            if (isSettingUserParams) {
                if (setParams(userData, message.getMessage().getText())) {
                    isSettingUserParams = false;
                    restaurantSelection =
                            restaurantCardFinder.getRestaurantCardByParams(
                                    userData.getCity(),
                                    userData.getKitchenTypes(),
                                    userData.getPriceCategories(),
                                    userData.getKeyWords());
                    return actionChose(actualState, messageId, chatId, userData);
                } else {
                    return EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(toIntExact(messageId))
                            .text(
                                    safeForceEdit(
                                            "Ошибка ввода. Ввод \""
                                                    + message.getMessage().getText()
                                                    + "\" некорректный. "
                                                    + "Повторите попытку"))
                            .replyMarkup(
                                    InlineKeyboardMarkup.builder()
                                            .keyboardRow(
                                                    new InlineKeyboardRow(
                                                            cancelSettingParamsButton))
                                            .build())
                            .build();
                }
            }
        }
        return actionChose(message.getCallbackQuery().getData(), messageId, chatId, userData);
    }

    private EditMessageText actionChose(
            String message, long messageId, long chatId, UserData userData) {
        switch (message) {
            case "goToUserParamsButton" -> {
                actualState = "goToUserParamsButton";
                isSettingUserParams = false;
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                "Выберите настройки, которые хотите изменить. \n"
                                        + "Ваши настройки: \n"
                                        + userData.userParamsToString())
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(setCityInUserParamsButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setKitchenTypesInUserParamsButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setPriceCategoriesInUserParamsButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setKeyWordsInUserParamsButton))
                                        .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                        .build())
                        .build();
            }
            case "setCityInUserParamsButton" -> {
                lastParams = "city";
                isSettingUserParams = true;
                actualState = "goToUserParamsButton";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                safeForceEdit(
                                        "Ведите город."
                                                + "\n"
                                                + "Доступные города: "
                                                + "\n"
                                                + correctCities))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(new InlineKeyboardRow(cancelUserParamsButton))
                                        .build())
                        .build();
            }
            case "setKitchenTypesInUserParamsButton" -> {
                lastParams = "kitchenTypes";
                isSettingUserParams = true;
                actualState = "goToUserParamsButton";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                safeForceEdit(
                                        "Введите типы кухни."
                                                + "\n"
                                                + "Доступные типы кухни: "
                                                + "\n"
                                                + correctKitchenTypes))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(new InlineKeyboardRow(cancelUserParamsButton))
                                        .build())
                        .build();
            }
            case "setPriceCategoriesInUserParamsButton" -> {
                lastParams = "priceCategories";
                isSettingUserParams = true;
                actualState = "goToUserParamsButton";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                safeForceEdit(
                                        "Введите ценовые категории."
                                                + "\n"
                                                + "Доступные ценовые категории: "
                                                + "\n"
                                                + correctPriceCategories))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(new InlineKeyboardRow(cancelUserParamsButton))
                                        .build())
                        .build();
            }
            case "setKeyWordsInUserParamsButton" -> {
                lastParams = "keyWords";
                isSettingUserParams = true;
                actualState = "goToUserParamsButton";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(safeForceEdit("Введите ключевые слова."))
                        .build();
            }
            case "goToMenuButton" -> {
                actualState = "goToMenuButton";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text("Выберите действие:")
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(new InlineKeyboardRow(goToUserParamsButton))
                                        .keyboardRow(new InlineKeyboardRow(goToFavouriteListButton))
                                        .keyboardRow(new InlineKeyboardRow(randomRestaurantButton))
                                        .keyboardRow(new InlineKeyboardRow(restaurantSearchButton))
                                        .build())
                        .build();
            }
            case "goToFavouriteListButton" -> {
                actualState = "goToFavouriteListButton";
                if (userData.getFavoriteList() == null || userData.getFavoriteList().isEmpty()) {
                    return EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(toIntExact(messageId))
                            .text("Список пуст")
                            .replyMarkup(
                                    InlineKeyboardMarkup.builder()
                                            .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                            .build())
                            .build();
                }
                FavouriteRestaurantCardDTO restaurantCard =
                        userData.nextRestaurantFromFavoriteList();
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(safeForceEdit(restaurantCardToStringForFavouriteList(restaurantCard)))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        nextRestaurantFavouriteListButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        restaurantCard.isVisited()
                                                                ? setAsNonVisitedButton
                                                                : setAsVisitedButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        removeFromFavouriteListButton))
                                        .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                        .build())
                        .build();
            }
            case "removeFromFavouriteListButton" -> {
                actualState = "removeFromFavouriteListButton";
                userData.removeRestaurantFromFavouriteListByIndex();
                FavouriteRestaurantCardDTO restaurantCard =
                        userData.nextRestaurantFromFavoriteList();
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(safeForceEdit(restaurantCardToStringForFavouriteList(restaurantCard)))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        nextRestaurantFavouriteListButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        restaurantCard.isVisited()
                                                                ? setAsNonVisitedButton
                                                                : setAsVisitedButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        removeFromFavouriteListButton))
                                        .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                        .build())
                        .build();
            }
            case "setAsVisitedButton" -> {
                actualState = "setAsVisitedButton";
                userData.changeIsVisited();
                FavouriteRestaurantCardDTO restaurantCard =
                        userData.getRestaurantFromFavouriteListByIndex();
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(safeForceEdit(restaurantCardToStringForFavouriteList(restaurantCard)))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        nextRestaurantFavouriteListButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        restaurantCard.isVisited()
                                                                ? setAsNonVisitedButton
                                                                : setAsVisitedButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        removeFromFavouriteListButton))
                                        .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                        .build())
                        .build();
            }
            case "randomRestaurantButton" -> {
                actualState = "randomRestaurantButton";
                isSettingLocation = true;
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                "Отправьте геоточку, в радиусе километра от которой хотите найти рестораны")
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                        .build())
                        .build();
            }
            case "randomRestaurantSearch" -> {
                actualState = "randomRestaurantSearch";
                isSettingLocation = false;
                if (restaurantSelection.isEmpty()) {
                    return EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(toIntExact(messageId))
                            .text("Мне не удалось подобрать рестораны.")
                            .replyMarkup(
                                    InlineKeyboardMarkup.builder()
                                            .keyboardRow(
                                                    new InlineKeyboardRow(
                                                            backToRandomRestaurantButton))
                                            .build())
                            .build();
                }
                RestaurantCardDTO restaurantCard = nextRestaurantFromSelection();
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(safeForceEdit(restaurantCardToString(restaurantCard)))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(nextRandomRestaurantButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        (!userData.isRestaurantInFavouriteList(
                                                                        restaurantCard))
                                                                ? addRandomRestaurantToFavouriteListButton
                                                                : removeRandomRestaurantFromFavouriteListButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(backToRandomRestaurantButton))
                                        .build())
                        .build();
            }
            case "addRandomRestaurantToFavouriteListButton" -> {
                actualState = "addRandomRestaurantToFavouriteListButton";
                RestaurantCardDTO restaurantCard = restaurantSelection.get(actualIndex);
                if (!userData.isRestaurantInFavouriteList(restaurantCard))
                    userData.addRestaurantToFavouriteList(restaurantCard);
                else userData.removeRestaurantFromFavouriteList(restaurantCard);
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(safeForceEdit(restaurantCardToString(restaurantCard)))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(nextRandomRestaurantButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        (!userData.isRestaurantInFavouriteList(
                                                                        restaurantCard))
                                                                ? addRandomRestaurantToFavouriteListButton
                                                                : removeRandomRestaurantFromFavouriteListButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(backToRandomRestaurantButton))
                                        .build())
                        .build();
            }
            case "restaurantSearchButton" -> {
                actualState = "restaurantSearchButton";
                isSettingUserParams = false;
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                "Вы можете изменить настройки поиска. Сейчас настройки поиска выглядят так:"
                                        + "\n"
                                        + userData.userParamsToSearchRestaurantsToString())
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setCityRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setKitchenTypesRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setPriceCategoriesRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setKeyWordsRestaurantSearchButton))
                                        .keyboardRow(new InlineKeyboardRow(searchButton))
                                        .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                        .build())
                        .build();
            }
            case "setCityRestaurantSearchButton" -> {
                lastParams = "cityForSearch";
                isSettingUserParams = true;
                actualState = "restaurantSearchButton";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                safeForceEdit(
                                        "Укажите город или выберите опцию."
                                                + "\n"
                                                + "Доступные города: "
                                                + "\n"
                                                + correctCities))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(new InlineKeyboardRow(setDefaultButton))
                                        .keyboardRow(new InlineKeyboardRow(setDisabledButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(backToRestaurantSearchButton))
                                        .build())
                        .build();
            }
            case "setKitchenTypesRestaurantSearchButton" -> {
                lastParams = "kitchenTypesForSearch";
                isSettingUserParams = true;
                actualState = "restaurantSearchButton";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                safeForceEdit(
                                        "Укажите типы кухни или выберите опцию."
                                                + "\n"
                                                + "Доступные типы кухни: "
                                                + "\n"
                                                + correctKitchenTypes))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(new InlineKeyboardRow(setDefaultButton))
                                        .keyboardRow(new InlineKeyboardRow(setDisabledButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(backToRestaurantSearchButton))
                                        .build())
                        .build();
            }
            case "setPriceCategoriesRestaurantSearchButton" -> {
                lastParams = "priceCategoriesForSearch";
                isSettingUserParams = true;
                actualState = "restaurantSearchButton";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                safeForceEdit(
                                        "Укажите ценовые категории или выберите опцию."
                                                + "\n"
                                                + "Доступные ценовые категории: "
                                                + "\n"
                                                + correctPriceCategories))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(new InlineKeyboardRow(setDefaultButton))
                                        .keyboardRow(new InlineKeyboardRow(setDisabledButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(backToRestaurantSearchButton))
                                        .build())
                        .build();
            }
            case "setKeyWordsRestaurantSearchButton" -> {
                lastParams = "keyWordsForSearch";
                isSettingUserParams = true;
                actualState = "restaurantSearchButton";
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(safeForceEdit("Укажите ключевые слова или выберите опцию."))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(new InlineKeyboardRow(setDefaultButton))
                                        .keyboardRow(new InlineKeyboardRow(setDisabledButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(backToRestaurantSearchButton))
                                        .build())
                        .build();
            }
            case "setDefaultButton" -> {
                actualState = "setDefaultButton";
                setDefaultParams(userData);
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                "Вы можете изменить настройки поиска. Сейчас настройки поиска выглядят так:"
                                        + "\n"
                                        + userData.userParamsToSearchRestaurantsToString())
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setCityRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setKitchenTypesRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setPriceCategoriesRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setKeyWordsRestaurantSearchButton))
                                        .keyboardRow(new InlineKeyboardRow(searchButton))
                                        .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                        .build())
                        .build();
            }
            case "setDisabledButton" -> {
                actualState = "setDisabledButton";
                setDisableParams(userData);
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(
                                "Вы можете изменить настройки поиска. Сейчас настройки поиска выглядят так:"
                                        + "\n"
                                        + userData.userParamsToSearchRestaurantsToString())
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setCityRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setKitchenTypesRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setPriceCategoriesRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        setKeyWordsRestaurantSearchButton))
                                        .keyboardRow(new InlineKeyboardRow(searchButton))
                                        .keyboardRow(new InlineKeyboardRow(goToMenuButton))
                                        .build())
                        .build();
            }
            case "searchButton" -> {
                actualState = "searchButton";
                if (restaurantSelection.isEmpty()) {
                    return EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(toIntExact(messageId))
                            .text("Мне не удалось подобрать рестораны.")
                            .replyMarkup(
                                    InlineKeyboardMarkup.builder()
                                            .keyboardRow(
                                                    new InlineKeyboardRow(
                                                            backToRestaurantSearchButton))
                                            .build())
                            .build();
                }
                RestaurantCardDTO restaurantCard = nextRestaurantFromSelection();
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(safeForceEdit(restaurantCardToString(restaurantCard)))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(nextRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        (!userData.isRestaurantInFavouriteList(
                                                                        restaurantCard))
                                                                ? addRestaurantSearchToFavouriteListButton
                                                                : removeRestaurantSearchFromFavouriteListButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(backToRestaurantSearchButton))
                                        .build())
                        .build();
            }
            case "addRestaurantSearchToFavouriteListButton" -> {
                actualState = "addRestaurantSearchToFavouriteListButton";
                RestaurantCardDTO restaurantCard = restaurantSelection.get(actualIndex);
                if (!userData.isRestaurantInFavouriteList(restaurantCard))
                    userData.addRestaurantToFavouriteList(restaurantCard);
                else userData.removeRestaurantFromFavouriteList(restaurantCard);
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(toIntExact(messageId))
                        .text(safeForceEdit(restaurantCardToString(restaurantCard)))
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboardRow(
                                                new InlineKeyboardRow(nextRestaurantSearchButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(
                                                        (!userData.isRestaurantInFavouriteList(
                                                                        restaurantCard))
                                                                ? addRestaurantSearchToFavouriteListButton
                                                                : removeRestaurantSearchFromFavouriteListButton))
                                        .keyboardRow(
                                                new InlineKeyboardRow(backToRestaurantSearchButton))
                                        .build())
                        .build();
            }
            case "cancelSettingParamsButton" -> {
                return actionChose(actualState, messageId, chatId, userData);
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

    private boolean setParams(UserData userData, String params) {
        switch (lastParams) {
            case "city" -> {
                return userData.checkAndSetCity(params);
            }
            case "kitchenTypes" -> {
                return userData.checkAndSetKitchenTypes(params);
            }
            case "priceCategories" -> {
                return userData.checkAndSetPriceCategories(params);
            }
            case "keyWords" -> {
                return userData.setKeyWords(params);
            }
            case "cityForSearch" -> {
                return userData.checkAndSetCityForSearch(params);
            }
            case "kitchenTypesForSearch" -> {
                return userData.checkAndSetKitchenTypesForSearch(params);
            }
            case "priceCategoriesForSearch" -> {
                return userData.checkAndSetPriceCategoriesForSearch(params);
            }
            case "keyWordsForSearch" -> {
                return userData.setKeyWordsForSearch(params);
            }
        }
        return false;
    }

    private void setDisableParams(UserData userData) {
        switch (lastParams) {
            case "cityForSearch" -> {
                userData.setCityForSearch("Отключено");
            }
            case "kitchenTypesForSearch" -> {
                userData.setKitchenTypesForSearch(List.of("Отключено"));
            }
            case "priceCategoriesForSearch" -> {
                userData.setPriceCategoriesForSearch(List.of("Отключено"));
            }
            case "keyWordsForSearch" -> {
                userData.setKeyWordsForSearch(List.of("Отключено"));
            }
        }
    }

    private void setDefaultParams(UserData userData) {
        switch (lastParams) {
            case "cityForSearch" -> {
                userData.setCityForSearch(userData.getCity());
            }
            case "kitchenTypesForSearch" -> {
                userData.setKitchenTypesForSearch(userData.getKitchenTypes());
            }
            case "priceCategoriesForSearch" -> {
                userData.setPriceCategoriesForSearch(userData.getPriceCategories());
            }
            case "keyWordsForSearch" -> {
                userData.setKeyWordsForSearch(userData.getKeyWords());
            }
        }
    }

    public static InlineKeyboardButton setCityInUserParamsButton =
            InlineKeyboardButton.builder()
                    .text("Задать город")
                    .callbackData("setCityInUserParamsButton")
                    .build();

    public static InlineKeyboardButton setKitchenTypesInUserParamsButton =
            InlineKeyboardButton.builder()
                    .text("Задать типы кухни")
                    .callbackData("setKitchenTypesInUserParamsButton")
                    .build();

    public static InlineKeyboardButton setPriceCategoriesInUserParamsButton =
            InlineKeyboardButton.builder()
                    .text("Задать ценовые категории")
                    .callbackData("setPriceCategoriesInUserParamsButton")
                    .build();

    public static InlineKeyboardButton setKeyWordsInUserParamsButton =
            InlineKeyboardButton.builder()
                    .text("Задать ключевые слова")
                    .callbackData("setKeyWordsInUserParamsButton")
                    .build();

    public static InlineKeyboardButton cancelUserParamsButton =
            InlineKeyboardButton.builder()
                    .text("Отмена")
                    .callbackData("goToUserParamsButton")
                    .build();

    public static InlineKeyboardButton goToMenuButton =
            InlineKeyboardButton.builder()
                    .text("Перейти в меню")
                    .callbackData("goToMenuButton")
                    .build();

    public static InlineKeyboardButton goToUserParamsButton =
            InlineKeyboardButton.builder()
                    .text("Перейти к пользовательским настройкам")
                    .callbackData("goToUserParamsButton")
                    .build();

    public static InlineKeyboardButton goToFavouriteListButton =
            InlineKeyboardButton.builder()
                    .text("Избранные рестораны")
                    .callbackData("goToFavouriteListButton")
                    .build();

    public static InlineKeyboardButton removeFromFavouriteListButton =
            InlineKeyboardButton.builder()
                    .text("Удалить из избранного")
                    .callbackData("removeFromFavouriteListButton")
                    .build();

    public static InlineKeyboardButton setAsVisitedButton =
            InlineKeyboardButton.builder()
                    .text("Отметить как посещенный")
                    .callbackData("setAsVisitedButton")
                    .build();

    public static InlineKeyboardButton setAsNonVisitedButton =
            InlineKeyboardButton.builder()
                    .text("Удалить отметку о посещении")
                    .callbackData("setAsVisitedButton")
                    .build();

    public static InlineKeyboardButton nextRestaurantFavouriteListButton =
            InlineKeyboardButton.builder()
                    .text("Следующий ресторан")
                    .callbackData("goToFavouriteListButton")
                    .build();

    public static InlineKeyboardButton randomRestaurantButton =
            InlineKeyboardButton.builder()
                    .text("Случайный Ресторан")
                    .callbackData("randomRestaurantButton")
                    .build();
    public static InlineKeyboardButton backToRandomRestaurantButton =
            InlineKeyboardButton.builder()
                    .text("Назад")
                    .callbackData("randomRestaurantButton")
                    .build();

    public static InlineKeyboardButton nextRandomRestaurantButton =
            InlineKeyboardButton.builder()
                    .text("Следующий ресторан")
                    .callbackData("randomRestaurantSearch")
                    .build();

    public static InlineKeyboardButton addRandomRestaurantToFavouriteListButton =
            InlineKeyboardButton.builder()
                    .text("Добавить в избранное")
                    .callbackData("addRandomRestaurantToFavouriteListButton")
                    .build();

    public static InlineKeyboardButton removeRandomRestaurantFromFavouriteListButton =
            InlineKeyboardButton.builder()
                    .text("Убрать из избранного")
                    .callbackData("addRandomRestaurantToFavouriteListButton")
                    .build();

    public static InlineKeyboardButton restaurantSearchButton =
            InlineKeyboardButton.builder()
                    .text("Поиск ресторанов")
                    .callbackData("restaurantSearchButton")
                    .build();

    public static InlineKeyboardButton setCityRestaurantSearchButton =
            InlineKeyboardButton.builder()
                    .text("Задать город")
                    .callbackData("setCityRestaurantSearchButton")
                    .build();

    public static InlineKeyboardButton setKitchenTypesRestaurantSearchButton =
            InlineKeyboardButton.builder()
                    .text("Задать типы кухни")
                    .callbackData("setKitchenTypesRestaurantSearchButton")
                    .build();

    public static InlineKeyboardButton setPriceCategoriesRestaurantSearchButton =
            InlineKeyboardButton.builder()
                    .text("Задать ценовые категории")
                    .callbackData("setPriceCategoriesRestaurantSearchButton")
                    .build();

    public static InlineKeyboardButton setKeyWordsRestaurantSearchButton =
            InlineKeyboardButton.builder()
                    .text("Задать ключевые слова")
                    .callbackData("setKeyWordsRestaurantSearchButton")
                    .build();

    public static InlineKeyboardButton setDefaultButton =
            InlineKeyboardButton.builder()
                    .text("По умолчанию")
                    .callbackData("setDefaultButton")
                    .build();

    public static InlineKeyboardButton setDisabledButton =
            InlineKeyboardButton.builder()
                    .text("Отключить")
                    .callbackData("setDisabledButton")
                    .build();

    public static InlineKeyboardButton backToRestaurantSearchButton =
            InlineKeyboardButton.builder()
                    .text("Назад")
                    .callbackData("restaurantSearchButton")
                    .build();

    public static InlineKeyboardButton searchButton =
            InlineKeyboardButton.builder().text("Поиск").callbackData("searchButton").build();

    public static InlineKeyboardButton nextRestaurantSearchButton =
            InlineKeyboardButton.builder()
                    .text("Следующий ресторан")
                    .callbackData("searchButton")
                    .build();

    public static InlineKeyboardButton addRestaurantSearchToFavouriteListButton =
            InlineKeyboardButton.builder()
                    .text("Добавить в избранное")
                    .callbackData("addRestaurantSearchToFavouriteListButton")
                    .build();

    public static InlineKeyboardButton removeRestaurantSearchFromFavouriteListButton =
            InlineKeyboardButton.builder()
                    .text("Убрать из избранного")
                    .callbackData("addRandomRestaurantToFavouriteListButton")
                    .build();

    public static InlineKeyboardButton cancelSettingParamsButton =
            InlineKeyboardButton.builder()
                    .text("Отмена")
                    .callbackData("cancelSettingParamsButton")
                    .build();

    public boolean isSettingLocation() {
        return isSettingLocation;
    }

    public String restaurantCardToStringForFavouriteList(
            FavouriteRestaurantCardDTO favouriteRestaurantCardDTO) {
        return favouriteRestaurantCardDTO.restaurantCardDTO().name()
                + "\n"
                + "Находится по адресу: "
                + favouriteRestaurantCardDTO.restaurantCardDTO().addressString()
                + '\n'
                + "Имеет рейтинг: "
                + favouriteRestaurantCardDTO.restaurantCardDTO().rating()
                + "\n"
                + "Официальный сайт: "
                + favouriteRestaurantCardDTO.restaurantCardDTO().website()
                + '\n'
                + favouriteRestaurantCardDTO.restaurantCardDTO().description()
                + '\n'
                + "Посещен: "
                + (favouriteRestaurantCardDTO.isVisited() ? "Да" : "Нет");
    }

    public String restaurantCardToString(RestaurantCardDTO restaurantCardDTO) {
        return restaurantCardDTO.name()
                + "\n"
                + "Находится по адресу: "
                + restaurantCardDTO.addressString()
                + '\n'
                + "Имеет рейтинг: "
                + restaurantCardDTO.rating()
                + "\n"
                + "Официальный сайт: "
                + restaurantCardDTO.website()
                + '\n'
                + restaurantCardDTO.description();
    }
}

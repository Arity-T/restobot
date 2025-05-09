package dev.tishenko.restobot.telegram.RestoBot.config;

import org.springframework.context.annotation.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Map;


@Configuration
@Scope("singleton")
public class RestoBotConfig {

    private String actualState;
    private String lastParams;
    private Map<String, List<String>> states;

    private void initStates() {
        states.put("/start", List.of("goToUserParamsButton")); // Greeting
        states.put("goToUserParamsButton", List.of(
                "setCityInUserParamsButton",
                "setKitchenTypesInUserParamsButton",
                "setPriceCategoriesInUserParamsButton",
                "setKeyWordsInUserParamsButton")); // User params
        states.put("setCityInUserParamsButton", List.of(
                "goToUserParamsButton",
                "setCityButton")); // Set city (user params)
        states.put("setKitchenTypesInUserParamsButton", List.of(
                "goToUserParamsButton",
                "setKitchenTypesInUserParamsButton")); // Set kitchen type (user params)
        states.put("setPriceCategoriesInUserParamsButton", List.of(
                "goToUserParamsButton",
                "setPriceCategoriesInUserParamsButton")); // Set price category (user params)
        states.put("setKeyWordsInUserParamsButton", List.of(
                "goToUserParamsButton")); // Set keywords (user params)
        states.put("goToMenuButton", List.of(
                "goToUserParamsButton",
                "goToFavouriteListButton",
                "randomRestaurantButton",
                "restaurantSearchButton")); // Menu
        states.put("goToFavouriteListButton", List.of(
                "goToMenuButton",
                "removeFromFavouriteListButton")); // Fav list
        states.put("removeFromFavouriteListButton", List.of(
                "nextRestaurantButton")); // Remove from Fav list
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

    public void nextState(String state){

    }

    public InlineKeyboardRow setCityInUserParamsButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать город")
            .callbackData("setCityInUserParamsButton")
            .build()
    );

    public InlineKeyboardRow setKitchenTypesInUserParamsButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать типы кухни")
            .callbackData("setKitchenTypesInUserParamsButton")
            .build()
    );

    public InlineKeyboardRow setPriceCategoriesInUserParamsButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать ценовые категории")
            .callbackData("setPriceCategoriesInUserParamsButton")
            .build()
    );

    public InlineKeyboardRow setKeyWordsInUserParamsButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать Ключевые слова")
            .callbackData("setKeyWordsInUserParamsButton")
            .build()
    );

    public InlineKeyboardRow cancelUserParamsButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Отмена")
            .callbackData("goToUserParamsButton")
            .build()
    );

    public InlineKeyboardRow goToMenuButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Перейти в меню")
            .callbackData("goToMenuButton")
            .build()
    );

    public InlineKeyboardRow goToUserParamsButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Перейти к пользовательским настройкам")
            .callbackData("goToUserParamsButton")
            .build()
    );

    public InlineKeyboardRow goToFavouriteListButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Избранные рестораны")
            .callbackData("goToFavouriteListButton")
            .build()
    );

    public InlineKeyboardRow removeFromFavouriteListButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Удалить из избранного")
            .callbackData("removeFromFavouriteListButton")
            .build()
    );

    public InlineKeyboardRow setAsVisitedButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Отметить как посещенный")
            .callbackData("setAsVisitedButton")
            .build()
    );

    public InlineKeyboardRow setAsNonVisitedButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Удалить отметку о посещении")
            .callbackData("setAsVisitedButton")
            .build()
    );

    public InlineKeyboardRow nextRestaurantFavouriteListButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Следующий ресторан")
            .callbackData("nextRestaurantFavouriteListButton")
            .build()
    );

    public InlineKeyboardRow randomRestaurantButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Случайный Ресторан")
            .callbackData("randomRestaurantButton")
            .build()
    );

    public InlineKeyboardRow backToRandomRestaurantButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Назад")
            .callbackData("randomRestaurantButton")
            .build()
    );

    public InlineKeyboardRow nextRandomRestaurantButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Следующий ресторан")
            .callbackData("nextRandomRestaurantButton")
            .build()
    );

    public InlineKeyboardRow addRandomRestaurantToFavouriteListButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Добавить в избранное")
            .callbackData("addRandomRestaurantToFavouriteListButton")
            .build()
    );

    public InlineKeyboardRow removeRandomRestaurantFromFavouriteListButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Убрать из избранного")
            .callbackData("addRandomRestaurantToFavouriteListButton")
            .build()
    );

    public InlineKeyboardRow restaurantSearchButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Поиск ресторанов")
            .callbackData("restaurantSearchButton")
            .build()
    );

    public InlineKeyboardRow setCityRestaurantSearchButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать город")
            .callbackData("setCityRestaurantSearchButton")
            .build()
    );


    public InlineKeyboardRow setKitchenTypesRestaurantSearchButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать типы кухни")
            .callbackData("setKitchenTypesRestaurantSearchButton")
            .build()
    );

    public InlineKeyboardRow setPriceCategoriesRestaurantSearchButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать ценовые категории")
            .callbackData("setPriceCategoriesRestaurantSearchButton")
            .build()
    );

    public InlineKeyboardRow setKeyWordsRestaurantSearchButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать Ключевые слова")
            .callbackData("setKeyWordsRestaurantSearchButton")
            .build()
    );

    public InlineKeyboardRow setDefaultButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("По умолчанию")
            .callbackData("setDefaultButton")
            .build()
    );

    public InlineKeyboardRow setDisabledButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Отключить")
            .callbackData("setDisabledButton")
            .build()
    );

    public InlineKeyboardRow backToRestaurantSearchButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Назад")
            .callbackData("restaurantSearchButton")
            .build()
    );

    public InlineKeyboardRow searchButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Поиск")
            .callbackData("searchButton")
            .build()
    );

    public InlineKeyboardRow nextRestaurantSearchButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Следующий ресторан")
            .callbackData("nextRestaurantSearchButton")
            .build()
    );

    public InlineKeyboardRow addRestaurantSearchToFavouriteListButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Добавить в избранное")
            .callbackData("addRestaurantSearchToFavouriteListButton")
            .build()
    );

    public InlineKeyboardRow removeRestaurantSearchFromFavouriteListButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Убрать из избранного")
            .callbackData("addRandomRestaurantToFavouriteListButton")
            .build()
    );

}

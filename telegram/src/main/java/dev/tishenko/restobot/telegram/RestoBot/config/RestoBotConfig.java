package dev.tishenko.restobot.telegram.RestoBot.config;

import org.springframework.context.annotation.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Map;


@Configuration
@Scope("singleton")
public class RestoBotConfig {

    private int actualState;

    private Map<Integer, List<Integer>> states;

    private void initStates() {
        states.put(0, List.of(1)); // Greeting
        states.put(1, List.of(2, 3, 4, 5)); // User params
        states.put(2, List.of(1, 2)); // Set city (user params)
        states.put(3, List.of(1, 3)); // Set kitchen type (user params)
        states.put(4, List.of(1, 4)); // Set price category (user params)
        states.put(5, List.of(1)); // Set keywords (user params)
        states.put(6, List.of(1, 7)); // Menu
        states.put(7, List.of(6, 7)); // Fav list
        states.put(8, List.of(6, 8, 9)); // Random rest
        states.put(9, List.of(8, 9)); // Rest card (Random rest)
        states.put(10, List.of(6, 10, 11, 12, 13, 14, 15)); // Search crit 
        states.put(11, List.of(10, 11)); // Set city (Search crit)
        states.put(12, List.of(10, 12)); // Set kitchen type (Search crit)
        states.put(13, List.of(10, 13)); // Set price category (Search crit)
        states.put(14, List.of(10)); // Set keywords (Search crit)
        states.put(15, List.of(10, 15)); // Rest card (Search crit)
    }

    public RestoBotConfig() {
        initStates();
    }


    public InlineKeyboardRow setCityButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать город")
            .callbackData("setCityButton")
            .build()
    );

    public InlineKeyboardRow setKitchenTypesButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать типы кухни")
            .callbackData("setKitchenTypesButton")
            .build()
    );

    public InlineKeyboardRow setPriceCategoriesButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать ценовые категории")
            .callbackData("setPriceCategoriesButton")
            .build()
    );

    public InlineKeyboardRow setKeyWordsButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Задать Ключевые слова")
            .callbackData("setKeyWordsButton")
            .build()
    );

    public InlineKeyboardRow cancelButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Отмена")
            .callbackData("cancelButton")
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
            .callbackData("setAsNonVisitedButton")
            .build()
    );

    public InlineKeyboardRow nextRestaurantButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Следующий ресторан")
            .callbackData("nextRestaurantButton")
            .build()
    );

    public InlineKeyboardRow randomRestaurantButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Случайный Ресторан")
            .callbackData("randomRestaurantButton")
            .build()
    );

    public InlineKeyboardRow backButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Назад")
            .callbackData("backButton")
            .build()
    );

    public InlineKeyboardRow addToFavouriteListButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Добавить в избранное")
            .callbackData("addToFavouriteListButton")
            .build()
    );

    public InlineKeyboardRow restaurantSearchButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Поиск ресторанов")
            .callbackData("restaurantSearchButton")
            .build()
    );

    public InlineKeyboardRow searchButton = new InlineKeyboardRow(InlineKeyboardButton
            .builder()
            .text("Поиск")
            .callbackData("searchButton")
            .build()
    );

}

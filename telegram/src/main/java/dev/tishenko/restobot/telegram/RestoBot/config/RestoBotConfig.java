package dev.tishenko.restobot.telegram.RestoBot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;


@Configuration
@Scope("singleton")
public class RestoBotConfig {
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

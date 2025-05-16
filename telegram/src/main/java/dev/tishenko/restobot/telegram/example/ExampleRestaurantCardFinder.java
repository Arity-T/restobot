package dev.tishenko.restobot.telegram.example;

import dev.tishenko.restobot.telegram.config.RestaurantCard;
import dev.tishenko.restobot.telegram.services.RestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.RestaurantCardFinder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ExampleRestaurantCardFinder implements RestaurantCardFinder {
    @Override
    public List<RestaurantCardDTO> getRestaurantCardByGeolocation(double latitude, double longitude) throws MalformedURLException {
        return List.of(new RestaurantCardDTO(
                        0,
                        "Звездочка",
                        "Энгельса 10",
                        9.3,
                        new URL("https://github.com/Arity-T/restobot"),
                        "Самый лучший ресторан",
                        33.0,
                        12.0,
                        "Москва"),
                new RestaurantCardDTO(
                        1,
                        "Жердочка",
                        "Фенгельса 10",
                        4.3,
                        new URL("https://github.com/Arity-T/restobot"),
                        "Самый лучший ресторан",
                        33.0,
                        12.0,
                        "Москва"),
                new RestaurantCardDTO(
                        2,
                        "Черточка",
                        "Шменгельса 10",
                        5.3,
                        new URL("https://github.com/Arity-T/restobot"),
                        "Самый лучший ресторан",
                        33.0,
                        12.0,
                        "Москва"),
                new RestaurantCardDTO(
                        3,
                        "Форточка",
                        "Чемельса 10",
                        6.3,
                        new URL("https://github.com/Arity-T/restobot"),
                        "Самый лучший ресторан",
                        33.0,
                        12.0,
                        "Москва"));
    }


    @Override
    public List<RestaurantCardDTO> getRestaurantCardByParams(String city, List<String> kitchenTypes, List<String> priceCategories, List<String> keyWords) throws MalformedURLException {
        return List.of(new RestaurantCardDTO(
                        0,
                        "Звездочка",
                        "Энгельса 10",
                        9.3,
                        new URL("https://github.com/Arity-T/restobot"),
                        "Самый лучший ресторан",
                        33.0,
                        12.0,
                        "Москва"),
                new RestaurantCardDTO(
                        1,
                        "Жердочка",
                        "Фенгельса 10",
                        4.3,
                        new URL("https://github.com/Arity-T/restobot"),
                        "Самый лучший ресторан",
                        33.0,
                        12.0,
                        "Москва"),
                new RestaurantCardDTO(
                        2,
                        "Черточка",
                        "Шменгельса 10",
                        5.3,
                        new URL("https://github.com/Arity-T/restobot"),
                        "Самый лучший ресторан",
                        33.0,
                        12.0,
                        "Москва"),
                new RestaurantCardDTO(
                        3,
                        "Форточка",
                        "Чемельса 10",
                        6.3,
                        new URL("https://github.com/Arity-T/restobot"),
                        "Самый лучший ресторан",
                        33.0,
                        12.0,
                        "Москва"));
    }
}

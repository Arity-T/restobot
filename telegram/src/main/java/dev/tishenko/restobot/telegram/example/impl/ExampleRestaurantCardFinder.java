package dev.tishenko.restobot.telegram.example.impl;

import dev.tishenko.restobot.telegram.services.RestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.RestaurantCardFinder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExampleRestaurantCardFinder implements RestaurantCardFinder {
    private static final Logger logger = LoggerFactory.getLogger(ExampleRestaurantCardFinder.class);

    @Override
    public List<RestaurantCardDTO> getRestaurantCardByGeolocation(double latitude, double longitude)
            throws MalformedURLException {
        logger.info(
                "Getting restaurant card by geolocation: latitude={}, longitude={}",
                latitude,
                longitude);
        return List.of(
                new RestaurantCardDTO(
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
    public List<RestaurantCardDTO> getRestaurantCardByParams(
            String city,
            List<String> kitchenTypes,
            List<String> priceCategories,
            List<String> keyWords)
            throws MalformedURLException {
        logger.info(
                "Getting restaurant card by params: city={}, kitchenTypes={}, priceCategories={}, keyWords={}",
                city,
                kitchenTypes,
                priceCategories,
                keyWords);
        return List.of(
                new RestaurantCardDTO(
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

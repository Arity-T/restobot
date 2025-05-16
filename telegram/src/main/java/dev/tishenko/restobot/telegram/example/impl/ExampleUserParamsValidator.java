package dev.tishenko.restobot.telegram.example.impl;

import dev.tishenko.restobot.telegram.services.UserParamsValidator;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExampleUserParamsValidator implements UserParamsValidator {

    @Override
    public boolean cityIsValid(String city) {
        return List.of(
                        "Москва",
                        "Санкт-Петербург",
                        "Новосибирск",
                        "Екатеринбург",
                        "Казань",
                        "Красноярск",
                        "Нижний Новгород",
                        "Челябинск",
                        "Уфа",
                        "Самара",
                        "Ростов-на-Дону",
                        "Краснодар",
                        "Омск",
                        "Воронеж",
                        "Пермь",
                        "Волгоград")
                .contains(city);
    }

    @Override
    public boolean kitchenTypesAreValid(List<String> kitchenTypes) {
        return new HashSet<>(
                        List.of(
                                "африканская",
                                "азиатская",
                                "американская",
                                "барбекю",
                                "ближневосточная",
                                "британская",
                                "вьетнамская",
                                "восточно-европейская",
                                "европейская",
                                "ирландская",
                                "испанская",
                                "итальянская",
                                "индийская",
                                "каджунская",
                                "карибская",
                                "китайская",
                                "мексиканская",
                                "немецкая",
                                "средиземноморская",
                                "тайская",
                                "французская",
                                "фьюжн",
                                "греческая",
                                "японская",
                                "южноамериканская"))
                .containsAll(kitchenTypes);
    }

    @Override
    public boolean priceCategoriesAreValid(List<String> priceCategories) {
        return List.of("Дешевое питание", "Средний ценовой сегмент", "Высокая кухня")
                .containsAll(priceCategories);
    }
}

package dev.tishenko.restobot.telegram.example.impl;

import dev.tishenko.restobot.telegram.services.SearchParametersService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExampleSearchParametersService implements SearchParametersService {
    @Override
    public List<String> getCitiesNames() {
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
                "Волгоград");
    }

    @Override
    public List<String> getKitchenTypesNames() {
        return List.of(
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
                "южноамериканская");
    }

    @Override
    public List<String> getPriceCategoriesNames() {
        return List.of("Дешевое питание", "Средний ценовой сегмент", "Высокая кухня");
    }
}

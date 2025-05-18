package dev.tishenko.restobot.telegram.example.impl;

import dev.tishenko.restobot.telegram.services.SearchParametersService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExampleSearchParametersService implements SearchParametersService {
    private static final Logger logger =
            LoggerFactory.getLogger(ExampleSearchParametersService.class);

    @Override
    public List<String> getCitiesNames() {
        logger.info("Getting cities names");
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
        logger.info("Getting kitchen types names");
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
        logger.info("Getting price categories names");
        return List.of("Дешевое питание", "Средний ценовой сегмент", "Высокая кухня");
    }
}

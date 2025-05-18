package dev.tishenko.restobot.logic.service.telegram;

import dev.tishenko.restobot.logic.service.LogicCityService;
import dev.tishenko.restobot.logic.service.LogicKitchenTypeService;
import dev.tishenko.restobot.logic.service.LogicPriceCategoryService;
import dev.tishenko.restobot.telegram.services.SearchParametersService;
import java.util.List;
import java.util.stream.Collectors;
import org.example.jooq.generated.tables.records.CityRecord;
import org.example.jooq.generated.tables.records.KitchenTypeRecord;
import org.example.jooq.generated.tables.records.PriceCategoryRecord;
import org.springframework.stereotype.Service;

@Service
public class SearchParametersServiceImpl implements SearchParametersService {

    private final LogicCityService cityService;
    private final LogicKitchenTypeService kitchenTypeService;
    private final LogicPriceCategoryService priceCategoryService;

    public SearchParametersServiceImpl(
            LogicCityService cityService,
            LogicKitchenTypeService kitchenTypeService,
            LogicPriceCategoryService priceCategoryService) {
        this.cityService = cityService;
        this.kitchenTypeService = kitchenTypeService;
        this.priceCategoryService = priceCategoryService;
    }

    @Override
    public List<String> getCitiesNames() {
        return cityService.getAllCities().stream()
                .map(CityRecord::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getKitchenTypesNames() {
        return kitchenTypeService.getAll().stream()
                .map(KitchenTypeRecord::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getPriceCategoriesNames() {
        return priceCategoryService.getAll().stream()
                .map(PriceCategoryRecord::getName)
                .collect(Collectors.toList());
    }
}

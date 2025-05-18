package dev.tishenko.restobot.logic.service.telegram;

import dev.tishenko.restobot.logic.jooq.generated.tables.records.CityRecord;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.KitchenTypeRecord;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.PriceCategoryRecord;
import dev.tishenko.restobot.logic.service.CityService;
import dev.tishenko.restobot.logic.service.KitchenTypeService;
import dev.tishenko.restobot.logic.service.PriceCategoryService;
import dev.tishenko.restobot.telegram.services.SearchParametersService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SearchParametersServiceImpl implements SearchParametersService {

    private final CityService cityService;
    private final KitchenTypeService kitchenTypeService;
    private final PriceCategoryService priceCategoryService;

    public SearchParametersServiceImpl(
            CityService cityService,
            KitchenTypeService kitchenTypeService,
            PriceCategoryService priceCategoryService) {
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

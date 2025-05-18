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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SearchParametersServiceImpl implements SearchParametersService {

    private static final Logger logger = LoggerFactory.getLogger(SearchParametersServiceImpl.class);
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
        logger.info("SearchParametersServiceImpl initialized");
    }

    @Override
    public List<String> getCitiesNames() {
        logger.debug("Fetching all city names");
        List<String> cityNames =
                cityService.getAllCities().stream()
                        .map(CityRecord::getName)
                        .collect(Collectors.toList());
        logger.debug("Found {} cities", cityNames.size());
        return cityNames;
    }

    @Override
    public List<String> getKitchenTypesNames() {
        logger.debug("Fetching all kitchen type names");
        List<String> kitchenTypeNames =
                kitchenTypeService.getAll().stream()
                        .map(KitchenTypeRecord::getName)
                        .collect(Collectors.toList());
        logger.debug("Found {} kitchen types", kitchenTypeNames.size());
        return kitchenTypeNames;
    }

    @Override
    public List<String> getPriceCategoriesNames() {
        logger.debug("Fetching all price category names");
        List<String> priceCategoryNames =
                priceCategoryService.getAll().stream()
                        .map(PriceCategoryRecord::getName)
                        .collect(Collectors.toList());
        logger.debug("Found {} price categories", priceCategoryNames.size());
        return priceCategoryNames;
    }
}

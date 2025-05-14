package dev.tishenko.restobot.telegram.services;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service interface for validating user-provided search parameters.
 * <p>
 * Provides methods to check the validity of user inputs such as city name,
 * selected kitchen types, and price categories.
 * </p>
 */
@Service
public interface UserParamsValidator {

    /**
     * Validates whether the provided city name is recognized and supported.
     *
     * @param city the name of the city to validate
     * @return {@code true} if the city is valid and supported; {@code false} otherwise
     */
    boolean cityIsValid(String city);

    /**
     * Validates whether the provided list of kitchen types contains only allowed values.
     *
     * @param kitchenTypes a list of kitchen or cuisine types to validate
     * @return {@code true} if all kitchen types are valid; {@code false} otherwise
     */
    boolean kitchenTypesAreValid(List<String> kitchenTypes);

    /**
     * Validates whether the provided list of price category identifiers contains only allowed values.
     *
     * @param priceCategories a list of price category identifiers to validate
     * @return {@code true} if all price categories are valid; {@code false} otherwise
     */
    boolean priceCategoriesAreValid(List<String> priceCategories);
}


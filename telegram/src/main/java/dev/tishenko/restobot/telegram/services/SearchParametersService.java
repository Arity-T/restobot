package dev.tishenko.restobot.telegram.services;

import java.util.List;

/** Service interface for retrieving available search parameter values for filtering restaurants. */
public interface SearchParametersService {

    /**
     * Retrieves the list of all supported city names.
     *
     * @return a {@link List} of city names available for restaurant search
     */
    List<String> getCitiesNames();

    /**
     * Retrieves the list of all supported kitchen (cuisine) type names.
     *
     * @return a {@link List} of kitchen or cuisine type names available for restaurant search
     */
    List<String> getKitchenTypesNames();

    /**
     * Retrieves the list of all supported price category names.
     *
     * @return a {@link List} of price category identifiers available for restaurant search
     */
    List<String> getPriceCategoriesNames();
}

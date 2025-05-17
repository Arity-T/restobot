package dev.tishenko.restobot.telegram.services;

/**
 * Data Transfer Object representing a favorite restaurant card combined with its visit status.
 *
 * <p>Encapsulates the restaurant card details along with a flag indicating whether the user has
 * visited it.
 *
 * @param restaurantCardDTO the {@link RestaurantCardDTO} containing restaurant card information
 * @param isVisited {@code true} if the restaurant has been visited by the user; {@code false}
 *     otherwise
 */
public record FavoriteRestaurantCardDTO(RestaurantCardDTO restaurantCardDTO, Boolean isVisited) {}

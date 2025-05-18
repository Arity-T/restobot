package dev.tishenko.restobot.telegram.services;

import java.net.URL;
import java.util.Objects;

/**
 * Used to transfer a restaurant card
 *
 * @param tripadvisorId {@code Integer}: Restaurant tripadvisor ID
 * @param name {@code String}: Name of the restaurant
 * @param addressString {@code String}: Address of the restaurant
 * @param rating {@code double}: Rating of the restaurant
 * @param website {@code URL}: of the restaurant
 * @param description {@code String}: of the restaurant
 * @param latitude {@code double}: Latitude of the restaurant
 * @param longitude {@code double}: Longitude of the restaurant
 * @param city {@code String}: Restaurant city
 */
public record RestaurantCardDTO(
        Integer tripadvisorId,
        String name,
        String addressString,
        double rating,
        URL website,
        String description,
        double latitude,
        double longitude,
        String city) {

}

package dev.tishenko.restobot.telegram.services;

import java.net.URL;
import org.springframework.stereotype.Service;

/**
 * Used to transfer a restaurant card
 *
 * @param restaurantId {@code int}: Restaurant ID in DB
 * @param tripadvisorId {@code int}: Restaurant tripadvisor ID
 * @param name {@code String}: Name of the restaurant
 * @param addressString {@code String}: Address of the restaurant
 * @param rating {@code double}: Address of the restaurant
 * @param website {@code URL}: of the restaurant
 * @param description {@code String}: of the restaurant
 * @param latitude {@code double}: Latitude of the restaurant
 * @param longitude {@code double}: Longitude of the restaurant
 * @param city {@code String}: Restaurant city
 */

public record RestaurantCardDTO(
        int restaurantId,
        int tripadvisorId,
        String name,
        String addressString,
        double rating,
        URL website,
        String description,
        double latitude,
        double longitude,
        String city) {}

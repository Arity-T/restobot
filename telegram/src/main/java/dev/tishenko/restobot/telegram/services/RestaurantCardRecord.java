package dev.tishenko.restobot.telegram.services;


import org.springframework.stereotype.Service;

import java.net.URL;


/**
 * Used to transfer a restaurant card
 *
 * @param restaurantId  {@code int}: Restaurant ID in DB
 * @param tripadvisorId {@code int}: Restaurant tripadvisor ID
 * @param name          {@code String}: Name of the restaurant
 * @param addressString {@code String}: Address of the restaurant
 * @param rating        {@code double}: Address of the restaurant
 * @param website       {@code URL}:  of the restaurant
 * @param description   {@code String}:  of the restaurant
 * @param latitude      {@code double}: Latitude of the restaurant
 * @param longitude     {@code double}: Longitude of the restaurant
 * @param city          {@code String}: Restaurant city
 */
@Service
public record RestaurantCardRecord(
        int restaurantId,
        int tripadvisorId,
        String name,
        String addressString,
        double rating,
        URL website,
        String description,
        double latitude,
        double longitude,
        String city) {
}

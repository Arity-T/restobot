package dev.tishenko.restobot.telegram.config;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;


@Component
@Scope("prototype")
public class RestaurantCard {
    private int restaurantId;
    private int tripadvisorId;
    private String name;
    private String addressString;
    private double rating;
    private URL website;
    private String description;
    private double latitude;
    private double longitude;
    private String city;

    public RestaurantCard() {
    }

    public RestaurantCard(int restaurantId,
                          int tripadvisorId,
                          String name,
                          String addressString,
                          double rating,
                          URL website,
                          String description,
                          double latitude,
                          double longitude,
                          String city) {
        this.restaurantId = restaurantId;
        this.tripadvisorId = tripadvisorId;
        this.name = name;
        this.addressString = addressString;
        this.rating = rating;
        this.website = website;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public int getTripadvisorId() {
        return tripadvisorId;
    }

    public void setTripadvisorId(int tripadvisorId) {
        this.tripadvisorId = tripadvisorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    public URL getWebsite() {
        return website;
    }

    public void setWebsite(URL website) {
        this.website = website;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

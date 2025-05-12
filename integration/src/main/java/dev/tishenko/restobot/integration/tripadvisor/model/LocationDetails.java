package dev.tishenko.restobot.integration.tripadvisor.model;

import lombok.Getter;

// https://tripadvisor-content-api.readme.io/reference/getlocationdetails
// almost all fields are presented
@Getter
public class LocationDetails {
    private Integer location_id;
    private String name;
    private String description;
    private String web_url;
    private Address address_obj;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String email;
    private String phone;
    private String website;
    private String write_review;
    private Double rating;
    private String rating_image_url;
    private String num_reviews; // it's a string according to the API docs
    private Integer photo_count;
    private String see_all_photos;
    private String price_level;
    private String parent_brand;
    private String brand;

    @Getter
    public static class Address {
        private String street1;
        private String street2;
        private String city;
        private String state;
        private String country;
        private String postalcode;
        private String address_string;
    }
}

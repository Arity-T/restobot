package dev.tishenko.restobot.integration.tripadvisor.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;

// https://tripadvisor-content-api.readme.io/reference/getlocationdetails
// almost all fields are presented
@Getter
public class LocationDetails {
    @SerializedName("location_id")
    private Integer locationId;

    private String name;
    private String description;

    @SerializedName("web_url")
    private String webUrl;

    @SerializedName("address_obj")
    private Address addressObj;

    @Getter
    public class Address {
        private String street1;
        private String street2;
        private String city;
        private String state;
        private String country;
        private String postalcode;

        @SerializedName("address_string")
        private String addressString;
    }

    private Double latitude;
    private Double longitude;
    private String timezone;
    private String email;
    private String phone;
    private String website;

    @SerializedName("write_review")
    private String writeReview;

    private Double rating;

    @SerializedName("rating_image_url")
    private String ratingImageUrl;

    @SerializedName("num_reviews")
    private String numReviews; // it's a string according to the API docs

    @SerializedName("photo_count")
    private Integer photoCount;

    @SerializedName("see_all_photos")
    private String seeAllPhotos;

    @SerializedName("price_level")
    private String priceLevel;

    private Hours hours;

    @Getter
    public static class Hours {
        private List<Period> periods;
        private List<String> weekdayText;

        @Getter
        public static class Period {
            private DayTime close;
            private DayTime open;

            @Getter
            public static class DayTime {
                private Integer day;
                private String time;
            }
        }
    }

    private List<String> amenities;
    private List<String> features;
    private List<Cuisine> cuisine;

    @Getter
    public static class Cuisine {
        private String name;
        private String localizedName;
    }

    @SerializedName("parent_brand")
    private String parentBrand;

    private String brand;

    private Category category;

    @Getter
    public static class Category {
        private String name;

        @SerializedName("localized_name")
        private String localizedName;

        private List<Subcategory> subcategory;

        @Getter
        public static class Subcategory {
            private String name;

            @SerializedName("localized_name")
            private String localizedName;
        }
    }

    private List<String> styles;

    @SerializedName("neighborhood_info")
    private List<Neighborhood> neighborhoodInfo;

    @Getter
    public static class Neighborhood {
        @SerializedName("location_id")
        private String locationId;

        private String name;
    }

    @SerializedName("trip_types")
    private List<TripType> tripTypes;

    @Getter
    public static class TripType {
        private String name;

        @SerializedName("localized_name")
        private String localizedName;

        private String value;
    }
}

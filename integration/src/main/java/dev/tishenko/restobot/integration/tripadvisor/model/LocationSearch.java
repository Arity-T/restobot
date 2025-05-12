package dev.tishenko.restobot.integration.tripadvisor.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;

// https://tripadvisor-content-api.readme.io/reference/searchforlocations
// same as https://tripadvisor-content-api.readme.io/reference/searchfornearbylocations
@Getter
public class LocationSearch {
    private List<LocationSearchResult> data;

    @Getter
    public static class LocationSearchResult {
        @SerializedName("location_id")
        private Integer locationId;

        private String name;
        private String distance;
        private String bearing;

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
    }
}

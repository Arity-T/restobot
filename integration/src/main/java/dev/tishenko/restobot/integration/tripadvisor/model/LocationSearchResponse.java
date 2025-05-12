package dev.tishenko.restobot.integration.tripadvisor.model;

import java.util.List;
import lombok.Getter;

// https://tripadvisor-content-api.readme.io/reference/searchforlocations
// same as https://tripadvisor-content-api.readme.io/reference/searchfornearbylocations
@Getter
public class LocationSearchResponse {
    private List<LocationSearchResult> data;

    @Getter
    public static class LocationSearchResult {
        private Integer location_id;
        private String name;
        private String distance;
        private String bearing;
        private Address address_obj;

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

}

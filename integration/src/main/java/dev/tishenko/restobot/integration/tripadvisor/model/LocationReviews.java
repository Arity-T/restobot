package dev.tishenko.restobot.integration.tripadvisor.model;

import java.util.List;
import lombok.Getter;

// https://tripadvisor-content-api.readme.io/reference/getlocationreviews
// almost all fields are presented
@Getter
public class LocationReviews {
    private List<Review> data;

    @Getter
    public static class Review {
        private Integer id;
        private String lang;
        private Integer location_id;
        private String published_date;
        private Integer rating;
        private Integer helpful_votes;
        private String rating_image_url;
        private String url;
        private String trip_type;
        private String travel_date;
        private String text;
        private String title;
        private Boolean is_machine_translated;
        private User user;

        @Getter
        public static class User {
            private String username;
            private Integer review_count;
            private String reviewer_badge;
        }
    }

    private Paging paging;

    @Getter
    public static class Paging {
        private String next;
        private String previous;
        private Integer results;
        private Integer total_results;
        private Integer skipped;
    }
}

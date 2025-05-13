package dev.tishenko.restobot.integration.tripadvisor.model;

import com.google.gson.annotations.SerializedName;
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

        @SerializedName("location_id")
        private Integer locationId;

        @SerializedName("published_date")
        private String publishedDate;

        private Integer rating;

        @SerializedName("helpful_votes")
        private Integer helpfulVotes;

        @SerializedName("rating_image_url")
        private String ratingImageUrl;

        private String url;

        @SerializedName("trip_type")
        private String tripType;

        @SerializedName("travel_date")
        private String travelDate;

        private String text;
        private String title;

        @SerializedName("is_machine_translated")
        private Boolean isMachineTranslated;

        private User user;

        @Getter
        public static class User {
            private String username;

            @SerializedName("review_count")
            private Integer reviewCount;

            @SerializedName("reviewer_badge")
            private String reviewerBadge;
        }
    }

    private Paging paging;

    @Getter
    public static class Paging {
        private String next;
        private String previous;
        private Integer results;

        @SerializedName("total_results")
        private Integer totalResults;

        private Integer skipped;
    }
}

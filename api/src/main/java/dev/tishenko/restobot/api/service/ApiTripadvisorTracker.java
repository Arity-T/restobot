package dev.tishenko.restobot.api.service;

import java.time.LocalDateTime;

/** Service for tracking TripAdvisor API calls. */
public interface ApiTripadvisorTracker {

    /**
     * Gets the timestamp of the last successful call to TripAdvisor API.
     *
     * @return The timestamp of the last call
     */
    LocalDateTime getLastCallTime();

    /** Updates the timestamp of the last successful call to TripAdvisor API. */
    void updateLastCallTime();
}

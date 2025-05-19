package dev.tishenko.restobot.integration.tripadvisor;

import java.time.Instant;

/** Interface for tracking TripAdvisor API usage */
public interface TripAdvisorTracker {
    /**
     * Get the time of the last API call
     *
     * @return Instant representing the last API call time
     */
    Instant getLastCallTime();

    /** Update the time of the last API call to current time */
    void updateLastCallTime();
}

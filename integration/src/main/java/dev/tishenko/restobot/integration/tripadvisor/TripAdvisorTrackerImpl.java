package dev.tishenko.restobot.integration.tripadvisor;

import java.time.Instant;
import org.springframework.stereotype.Component;

/** Implementation of TripAdvisorTracker that tracks the last API call time. */
@Component
public class TripAdvisorTrackerImpl implements TripAdvisorTracker {
    private Instant lastCallTime = Instant.now();

    @Override
    public Instant getLastCallTime() {
        return lastCallTime;
    }

    @Override
    public void updateLastCallTime() {
        this.lastCallTime = Instant.now();
    }
}

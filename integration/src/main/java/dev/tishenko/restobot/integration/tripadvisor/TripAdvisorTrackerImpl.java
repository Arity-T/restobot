package dev.tishenko.restobot.integration.tripadvisor;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TripAdvisorTrackerImpl implements TripAdvisorTracker {
    private static final Logger logger = LoggerFactory.getLogger(TripAdvisorTrackerImpl.class);
    private volatile Instant lastCallTime = Instant.now();

    @Override
    public Instant getLastCallTime() {
        return lastCallTime;
    }

    public void updateLastCallTime() {
        lastCallTime = Instant.now();
        logger.debug("Updated last call time to: {}", lastCallTime);
    }
}

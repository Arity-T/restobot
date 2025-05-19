package dev.tishenko.restobot.logic.service.api;

import dev.tishenko.restobot.api.service.ApiTripadvisorTracker;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class ApiTripadvisorTrackerImpl implements ApiTripadvisorTracker {
    private LocalDateTime lastCallTime = LocalDateTime.now();

    @Override
    public LocalDateTime getLastCallTime() {
        return lastCallTime;
    }

    @Override
    public void updateLastCallTime() {
        this.lastCallTime = LocalDateTime.now();
    }
}

package dev.tishenko.restobot.api.example.impl;

import dev.tishenko.restobot.api.service.TripadvisorTracker;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/** Example implementation of TripadvisorTracker. */
@Service
public class ExampleTripadvisorTracker implements TripadvisorTracker {

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

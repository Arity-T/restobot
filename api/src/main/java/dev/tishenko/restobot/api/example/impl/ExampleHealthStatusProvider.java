package dev.tishenko.restobot.api.example.impl;

import dev.tishenko.restobot.api.service.ApiHealthStatusProvider;
import dev.tishenko.restobot.api.service.ApiTripadvisorTracker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Example implementation of HealthStatusProvider. */
@Service
public class ExampleHealthStatusProvider implements ApiHealthStatusProvider {

    private final ApiTripadvisorTracker tripadvisorTracker;

    public ExampleHealthStatusProvider(ApiTripadvisorTracker tripadvisorTracker) {
        this.tripadvisorTracker = tripadvisorTracker;
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "OK");
        status.put("lastTripAdvisorCallTime", tripadvisorTracker.getLastCallTime().toString());
        status.put("authors", List.of("Тищенко Артём", "Гаар Владислав", "Губковский Дмитрий"));
        return status;
    }
}

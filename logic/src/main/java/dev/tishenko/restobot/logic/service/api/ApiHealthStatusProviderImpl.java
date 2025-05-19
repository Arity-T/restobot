package dev.tishenko.restobot.logic.service.api;

import dev.tishenko.restobot.api.service.ApiHealthStatusProvider;
import dev.tishenko.restobot.integration.tripadvisor.TripAdvisorTracker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApiHealthStatusProviderImpl implements ApiHealthStatusProvider {

    private static final Logger logger = LoggerFactory.getLogger(ApiHealthStatusProviderImpl.class);
    private final TripAdvisorTracker tripAdvisorTracker;

    public ApiHealthStatusProviderImpl(TripAdvisorTracker tripAdvisorTracker) {
        this.tripAdvisorTracker = tripAdvisorTracker;
        logger.info("ApiHealthStatusProviderImpl initialized");
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        logger.debug("Providing health status information");
        Map<String, Object> status = new HashMap<>();
        status.put("status", "OK");
        status.put("lastTripAdvisorCallTime", tripAdvisorTracker.getLastCallTime().toString());
        status.put("authors", List.of("Тищенко Артём", "Гаар Владислав", "Губковский Дмитрий"));
        return status;
    }
}

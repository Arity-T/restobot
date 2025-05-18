package dev.tishenko.restobot.logic.service.api;

import dev.tishenko.restobot.api.service.ApiHealthStatusProvider;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class HealthStatusProvider implements ApiHealthStatusProvider {

    @Override
    public Map<String, Object> getHealthStatus() {
        return Map.of("status", "OK");
    }
}

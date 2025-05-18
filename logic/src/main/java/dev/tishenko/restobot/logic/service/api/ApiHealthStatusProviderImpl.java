package dev.tishenko.restobot.logic.service.api;

import dev.tishenko.restobot.api.service.ApiHealthStatusProvider;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ApiHealthStatusProviderImpl implements ApiHealthStatusProvider {

    @Override
    public Map<String, Object> getHealthStatus() {
        return Map.of("status", "OK");
    }
}

package dev.tishenko.restobot.logic.service.api;

import dev.tishenko.restobot.api.service.ApiHealthStatusProvider;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApiHealthStatusProviderImpl implements ApiHealthStatusProvider {

    private static final Logger logger = LoggerFactory.getLogger(ApiHealthStatusProviderImpl.class);

    public ApiHealthStatusProviderImpl() {
        logger.info("ApiHealthStatusProviderImpl initialized");
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        logger.debug("Providing health status information");
        return Map.of("status", "OK");
    }
}

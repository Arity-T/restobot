package dev.tishenko.restobot.api.service;

import java.util.Map;

/** Service for providing health status information. */
public interface HealthStatusProvider {

    /**
     * Gets the current health status of the application.
     *
     * @return A map containing health status information
     */
    Map<String, Object> getHealthStatus();
}

package dev.tishenko.restobot.api;

import dev.tishenko.restobot.api.service.ApiHealthStatusProvider;
import dev.tishenko.restobot.api.service.ApiKeyValidator;
import dev.tishenko.restobot.api.service.ApiUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

/**
 * Main configuration class for Restobot API library. To use this library, import this configuration
 * in your Spring application.
 */
@Configuration
public class RestobotApiConfig {

    @Bean
    public HealthCheckHandler healthCheckHandler(ApiHealthStatusProvider healthStatusProvider) {
        return new HealthCheckHandler(healthStatusProvider);
    }

    @Bean
    public UserHandler userHandler(ApiKeyValidator apiKeyValidator, ApiUserService userService) {
        return new UserHandler(apiKeyValidator, userService);
    }

    /**
     * Configures routes for Restobot API endpoints.
     *
     * @param healthCheckHandler Handler for health check endpoints
     * @param userHandler Handler for user endpoints
     * @return Router function with configured routes
     */
    @Bean
    public RouterFunction<ServerResponse> restobotRoutes(
            HealthCheckHandler healthCheckHandler, UserHandler userHandler) {
        return RouterFunctions.route(
                        RequestPredicates.GET("/healthcheck"), healthCheckHandler::healthcheck)
                .andRoute(RequestPredicates.GET("/users"), userHandler::getUsers);
    }
}

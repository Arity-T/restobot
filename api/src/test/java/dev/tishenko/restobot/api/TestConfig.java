package dev.tishenko.restobot.api;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

import dev.tishenko.restobot.api.service.ApiHealthStatusProvider;
import dev.tishenko.restobot.api.service.ApiKeyValidator;
import dev.tishenko.restobot.api.service.ApiUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class TestConfig {

    @Bean
    public HealthCheckHandler healthCheckHandler(ApiHealthStatusProvider healthStatusProvider) {
        return new HealthCheckHandler(healthStatusProvider);
    }

    @Bean
    public UserHandler userHandler(ApiKeyValidator apiKeyValidator, ApiUserService userService) {
        return new UserHandler(apiKeyValidator, userService);
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction(
            HealthCheckHandler healthCheckHandler, UserHandler userHandler) {
        return RouterFunctions.route(GET("/healthcheck"), healthCheckHandler::healthcheck)
                .andRoute(GET("/users"), userHandler::getUsers);
    }
}

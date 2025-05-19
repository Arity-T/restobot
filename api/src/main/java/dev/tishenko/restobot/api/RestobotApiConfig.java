package dev.tishenko.restobot.api;

import dev.tishenko.restobot.api.service.ApiHealthStatusProvider;
import dev.tishenko.restobot.api.service.ApiKeyValidator;
import dev.tishenko.restobot.api.service.ApiUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

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
                .andRoute(RequestPredicates.GET("/users"), userHandler::getUsers)
                .andRoute(RequestPredicates.all(), notFoundHandler());
    }

    private HandlerFunction<ServerResponse> notFoundHandler() {
        return request ->
                ServerResponse.status(404)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("{\"error\": \"Not Found\"}");
    }

    @Bean
    public WebFilter errorHandlingFilter() {
        return (exchange, chain) ->
                chain.filter(exchange)
                        .onErrorResume(
                                Exception.class,
                                e -> {
                                    exchange.getResponse()
                                            .setStatusCode(
                                                    org.springframework.http.HttpStatus
                                                            .INTERNAL_SERVER_ERROR);
                                    exchange.getResponse()
                                            .getHeaders()
                                            .setContentType(MediaType.APPLICATION_JSON);
                                    return exchange.getResponse()
                                            .writeWith(
                                                    Mono.just(
                                                            exchange.getResponse()
                                                                    .bufferFactory()
                                                                    .wrap(
                                                                            "{\"error\": \"Internal Server Error\"}"
                                                                                    .getBytes())));
                                });
    }
}

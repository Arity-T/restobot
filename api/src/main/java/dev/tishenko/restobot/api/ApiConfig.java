package dev.tishenko.restobot.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.WebHandler;

@Configuration
@ComponentScan("dev.tishenko.restobot.api")
public class ApiConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApiConfig.class);

    @Bean
    public RouterFunction<ServerResponse> routes(
            HealthCheckHandler healthCheckHandler, UserHandler userHandler) {
        logger.info("Configuring routes");
        return RouterFunctions.route(
                        RequestPredicates.GET("/healthcheck"), healthCheckHandler::healthcheck)
                .andRoute(RequestPredicates.GET("/users"), userHandler::getUsers);
    }

    @Bean
    public WebHandler webHandler(RouterFunction<ServerResponse> routes) {
        logger.info("Configuring web handler");
        return RouterFunctions.toWebHandler(routes);
    }
}

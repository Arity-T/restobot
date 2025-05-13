package dev.tishenko.restobot.api;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.WebHandler;

@Configuration
@ComponentScan("dev.tishenko.restobot.api")
public class ApiConfig {

    private final Gson gson = new Gson();

    @Bean
    public RouterFunction<ServerResponse> routes(
            HealthCheckHandler healthCheckHandler, UserHandler userHandler) {
        return RouterFunctions.route(
                        RequestPredicates.GET("/healthcheck"), healthCheckHandler::healthcheck)
                .andRoute(RequestPredicates.GET("/users"), userHandler::getUsers);
    }

    @Bean
    public WebHandler webHandler(RouterFunction<ServerResponse> routes) {
        return RouterFunctions.toWebHandler(routes);
    }

    @Bean
    public Gson gson() {
        return gson;
    }
}

package dev.tishenko.restobot.api.example;

import dev.tishenko.restobot.api.RestobotApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebHandler;

/**
 * Example configuration that shows how to use RestobotApiConfig. This class demonstrates how to
 * import and use the Restobot API library.
 */
@Configuration
@Import(RestobotApiConfig.class)
@ComponentScan("dev.tishenko.restobot.api.example.impl")
public class AppConfigExample {
    private static final Logger logger = LoggerFactory.getLogger(AppConfigExample.class);

    @Bean
    public WebHandler webHandler(RouterFunction<ServerResponse> restobotRoutes) {
        logger.info("Configuring web handler with Restobot API routes");
        return RouterFunctions.toWebHandler(restobotRoutes);
    }
}

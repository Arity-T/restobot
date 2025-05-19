package dev.tishenko.restobot.app;

import dev.tishenko.restobot.api.RestobotApiConfig;
import dev.tishenko.restobot.integration.tripadvisor.TripAdvisorConfiguration;
import dev.tishenko.restobot.logic.config.LogicConfig;
import dev.tishenko.restobot.telegram.config.BotFactoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebHandler;

@Configuration
@Import({
    RestobotApiConfig.class,
    LogicConfig.class,
    TripAdvisorConfiguration.class,
    BotFactoryConfig.class
})
@PropertySource("classpath:application.properties")
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public WebHandler webHandler(RouterFunction<ServerResponse> restobotRoutes) {
        logger.info("Configuring web handler with Restobot API routes");
        return RouterFunctions.toWebHandler(restobotRoutes);
    }
}

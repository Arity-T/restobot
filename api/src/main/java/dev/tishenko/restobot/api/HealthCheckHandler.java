package dev.tishenko.restobot.api;

import com.google.gson.Gson;
import dev.tishenko.restobot.api.service.ApiHealthStatusProvider;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

public class HealthCheckHandler {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckHandler.class);

    private final Gson gson = new Gson();
    private final ApiHealthStatusProvider healthStatusProvider;

    public HealthCheckHandler(ApiHealthStatusProvider healthStatusProvider) {
        this.healthStatusProvider = healthStatusProvider;
    }

    public Mono<ServerResponse> healthcheck(ServerRequest request) {
        logger.info("Received healthcheck request");
        Map<String, Object> response = healthStatusProvider.getHealthStatus();

        String jsonResponse = gson.toJson(response);
        logger.info("Sending healthcheck response of length {}", jsonResponse.length());
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(jsonResponse);
    }
}

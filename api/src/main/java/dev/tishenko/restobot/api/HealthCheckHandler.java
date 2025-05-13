package dev.tishenko.restobot.api;

import com.google.gson.Gson;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Component
public class HealthCheckHandler {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckHandler.class);

    private final Gson gson = new Gson();
    private LocalDateTime lastTripAdvisorCallTime =
            LocalDateTime.now(); // можно обновлять по факту успешного обращения

    public Mono<ServerResponse> healthcheck(ServerRequest request) {
        logger.info("Received healthcheck request");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("lastTripAdvisorCallTime", lastTripAdvisorCallTime.toString());
        response.put("authors", List.of("Тищенко Артём", "Гаар Владислав", "Губковский Дмитрий"));

        String jsonResponse = gson.toJson(response);
        logger.info("Sending healthcheck response of length {}", jsonResponse.length());
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(jsonResponse);
    }
}

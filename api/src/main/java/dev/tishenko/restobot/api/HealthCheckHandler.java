package dev.tishenko.restobot.api;

import com.google.gson.Gson;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Component
public class HealthCheckHandler {
    private final Gson gson;
    private LocalDateTime lastTripAdvisorCallTime =
            LocalDateTime.now(); // можно обновлять по факту успешного обращения

    public HealthCheckHandler(Gson gson) {
        this.gson = gson;
    }

    public Mono<ServerResponse> healthcheck(ServerRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("lastTripAdvisorCallTime", lastTripAdvisorCallTime.toString());
        response.put("authors", List.of("Тищенко Артём", "Гаар Владислав", "Губковский Дмитрий"));

        String jsonResponse = gson.toJson(response);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(jsonResponse);
    }
}

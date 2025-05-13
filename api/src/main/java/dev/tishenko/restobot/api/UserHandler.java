package dev.tishenko.restobot.api;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Component
public class UserHandler {
    private final Gson gson;
    private final String validApiKey = "my-secure-api-key";

    public UserHandler(Gson gson) {
        this.gson = gson;
    }

    public Mono<ServerResponse> getUsers(ServerRequest request) {
        String apiKey = request.headers().firstHeader("X-API-KEY");

        if (apiKey == null || !apiKey.equals(validApiKey)) {
            Map<String, String> error = Map.of("error", "Unauthorized");
            String errorResponse = gson.toJson(error);
            return ServerResponse.status(401)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(errorResponse);
        }

        List<Map<String, String>> users =
                List.of(
                        Map.of("id", "1", "username", "@user1"),
                        Map.of("id", "2", "username", "@user2"));

        String jsonResponse = gson.toJson(users);
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(jsonResponse);
    }
}

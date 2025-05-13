package dev.tishenko.restobot.api;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Component
public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);

    private final Gson gson = new Gson();
    private final String validApiKey = "my-secure-api-key";

    public Mono<ServerResponse> getUsers(ServerRequest request) {
        logger.info("Received request to get users");
        String apiKey = request.headers().firstHeader("X-API-KEY");

        if (apiKey == null || !apiKey.equals(validApiKey)) {
            Map<String, String> error = Map.of("error", "Unauthorized");
            String errorResponse = gson.toJson(error);
            logger.error("Unauthorized request with API key: {}", apiKey);
            return ServerResponse.status(401)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(errorResponse);
        }

        List<Map<String, String>> users =
                List.of(
                        Map.of("id", "1", "username", "@user1"),
                        Map.of("id", "2", "username", "@user2"));

        String jsonResponse = gson.toJson(users);
        logger.info("Sending users response of length {}", jsonResponse.length());
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(jsonResponse);
    }
}

package dev.tishenko.restobot.api;

import com.google.gson.Gson;
import dev.tishenko.restobot.api.service.ApiKeyValidator;
import dev.tishenko.restobot.api.service.UserService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);

    private final Gson gson = new Gson();
    private final ApiKeyValidator apiKeyValidator;
    private final UserService userService;

    public UserHandler(ApiKeyValidator apiKeyValidator, UserService userService) {
        this.apiKeyValidator = apiKeyValidator;
        this.userService = userService;
    }

    public Mono<ServerResponse> getUsers(ServerRequest request) {
        logger.info("Received request to get users");
        String apiKey = request.headers().firstHeader("X-API-KEY");

        if (!apiKeyValidator.isValidApiKey(apiKey)) {
            Map<String, String> error = Map.of("error", "Unauthorized");
            String errorResponse = gson.toJson(error);
            logger.error("Unauthorized request with API key: {}", apiKey);
            return ServerResponse.status(401)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(errorResponse);
        }

        String jsonResponse = gson.toJson(userService.getUsers());
        logger.info("Sending users response of length {}", jsonResponse.length());
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(jsonResponse);
    }
}

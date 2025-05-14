package dev.tishenko.restobot.api.example.impl;

import dev.tishenko.restobot.api.service.ApiKeyValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Example implementation of ApiKeyValidator. */
@Service
public class ExampleApiKeyValidator implements ApiKeyValidator {

    private final String validApiKey;

    public ExampleApiKeyValidator(
            @Value("${restobot.api.key:my-secure-api-key}") String validApiKey) {
        this.validApiKey = validApiKey;
    }

    @Override
    public boolean isValidApiKey(String apiKey) {
        return apiKey != null && apiKey.equals(validApiKey);
    }
}

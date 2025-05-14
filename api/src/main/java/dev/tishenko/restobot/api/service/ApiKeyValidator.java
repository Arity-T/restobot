package dev.tishenko.restobot.api.service;

/** Service for validating API keys. */
public interface ApiKeyValidator {

    /**
     * Validates if the provided API key is valid.
     *
     * @param apiKey The API key to validate
     * @return true if the API key is valid, false otherwise
     */
    boolean isValidApiKey(String apiKey);
}

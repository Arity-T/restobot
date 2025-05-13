package dev.tishenko.restobot.integration.tripadvisor.exception;

/** Exception thrown when an error occurs during TripAdvisor API communication */
public class TripAdvisorApiException extends RuntimeException {
    private final int statusCode;

    /**
     * Creates a new TripAdvisor API exception
     *
     * @param message Error message
     * @param statusCode HTTP status code
     */
    public TripAdvisorApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Creates a new TripAdvisor API exception with a cause
     *
     * @param message Error message
     * @param statusCode HTTP status code
     * @param cause The cause of the exception
     */
    public TripAdvisorApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Gets the HTTP status code associated with the error
     *
     * @return HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}

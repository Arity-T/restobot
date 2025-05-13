package dev.tishenko.restobot.integration.tripadvisor.model;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private Error error;

    @Getter
    public static class Error {
        private String message;
        private String type;
        private Integer code;
    }
}

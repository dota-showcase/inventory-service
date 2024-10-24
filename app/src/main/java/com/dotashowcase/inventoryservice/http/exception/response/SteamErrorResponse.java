package com.dotashowcase.inventoryservice.http.exception.response;

import lombok.Data;

import java.time.Instant;

public class SteamErrorResponse extends ErrorResponse {

    private final SteamErrorDetail steamBody;

    public SteamErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            SteamErrorDetail steamBody
    ) {
        super(timestamp, status, error, message, path);
        this.steamBody = steamBody;
    }

    public SteamErrorDetail getSteamBody() {
        return steamBody;
    }

    @Data
    public static class SteamErrorDetail {

        private String type;

        private int httpStatus;

        private Integer inventoryStatus;

        private String error;

        private String message;
    }
}

package com.dotashowcase.inventoryservice.http.exception.response;

import lombok.Data;

import java.time.Instant;

public class SteamErrorResponse extends ErrorResponse {

    private SteamErrorDetail steamBody;

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

        private int status;

        private String error;

        private String message;
    }
}

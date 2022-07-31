package com.dotashowcase.inventoryservice.steamclient.exception;

/**
 * {@code BadRequestException} is thrown on steam api errors.
 * For example, on 400 or 500 HTTP response codes.
 */
public class BadRequestException extends SteamException {

    private int steamHttpStatusCode;

    private BadRequestException(int steamHttpStatusCode, String message) {
        super("Steam Response: " + message);
        this.steamHttpStatusCode = steamHttpStatusCode;
    }

    public static BadRequestException steamApiError(int steamHttpStatusCode, String message) {
        return new BadRequestException(steamHttpStatusCode, String.format("Failed with message: %s", message));
    }

    public int getSteamHttpStatusCode() {
        return this.steamHttpStatusCode;
    }
}

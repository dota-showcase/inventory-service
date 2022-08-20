package com.dotashowcase.inventoryservice.steamclient.exception;

/**
 * {@code BadRequestException} is thrown on steam api errors.
 * For example, on 400 or 500 HTTP response codes.
 */
public class BadRequestException extends SteamException {

    private final int steamHttpStatusCode;

    private final String steamHttpMessage;

    private BadRequestException(int steamHttpStatusCode, String message) {
        super("Steam Response - " + message);
        this.steamHttpStatusCode = steamHttpStatusCode;
        this.steamHttpMessage = message;
    }

    public static BadRequestException steamApiError(int steamHttpStatusCode, String message) {
        return new BadRequestException(steamHttpStatusCode, message);
    }

    public int getSteamHttpStatusCode() {
        return this.steamHttpStatusCode;
    }

    public String getSteamHttpMessage() {
        return this.steamHttpMessage;
    }
}

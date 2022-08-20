package com.dotashowcase.inventoryservice.steamclient.exception;

public class SteamClientException extends RuntimeException {

    public SteamClientException(String message) {
        super("Steam integration error - " + message);
    }
}
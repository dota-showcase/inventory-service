package com.dotashowcase.inventoryservice.steamclient.response.exception;

public class BadResponseBodyException extends RuntimeException {

    private BadResponseBodyException(String message) {
        super("Failed to parse a response from steam: " + message);
    }

    public static BadResponseBodyException invalidBodyStructure(String message) {
        return new BadResponseBodyException(message);
    }

    public static BadResponseBodyException jsonNodeNotFound(String nodeName) {
        return new BadResponseBodyException(String.format("Body is mission required node - '%s'", nodeName));
    }
}

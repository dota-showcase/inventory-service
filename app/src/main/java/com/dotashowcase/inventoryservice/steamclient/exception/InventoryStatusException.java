package com.dotashowcase.inventoryservice.steamclient.exception;

/**
 * {@code InventoryStatusException} is thrown on accessing user inventory.
 * Response body contains steam status of response.
 */
public class InventoryStatusException extends SteamException {

    private final int steamHttpStatusCode;

    private final int steamInventoryStatusCode;

    private final String steamInnerMessage;

    private InventoryStatusException(int steamHttpStatusCode, int steamInventoryStatusCode, String message) {
        super("Inventory status - " + message);
        this.steamHttpStatusCode = steamHttpStatusCode;
        this.steamInventoryStatusCode = steamInventoryStatusCode;
        this.steamInnerMessage = message;
    }

    public static InventoryStatusException itemsNotPresent(
            int    steamHttpStatusCode,
            int    steamInnerStatusCode,
            String message
    ) {
        return new InventoryStatusException(steamHttpStatusCode, steamInnerStatusCode, message);
    }

    public int getSteamHttpStatusCode() {
        return this.steamHttpStatusCode;
    }

    public int getSteamInventoryStatusCode() {
        return this.steamInventoryStatusCode;
    }

    public String getSteamInnerMessage() {
        return this.steamInnerMessage;
    }
}

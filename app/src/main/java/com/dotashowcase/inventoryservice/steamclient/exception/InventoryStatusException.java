package com.dotashowcase.inventoryservice.steamclient.exception;

/**
 * {@code InventoryStatusException} is thrown on accessing user inventory.
 * Response body contains steam status of response.
 */
public class InventoryStatusException extends SteamException {

    private int steamInnerStatusCode;

    private InventoryStatusException(int steamInnerStatusCode, String message) {
        super("Inventory status: " + message);
        this.steamInnerStatusCode = steamInnerStatusCode;
    }

    public static InventoryStatusException itemsNotPresent(int steamInnerStatusCode, String message) {
        return new InventoryStatusException(steamInnerStatusCode, message);
    }

    public int getSteamInnerStatusCode() {
        return this.steamInnerStatusCode;
    }
}

package com.dotashowcase.inventoryservice.steamclient;

import org.springframework.stereotype.Component;

/**
 * Docs:
 * https://wiki.teamfortress.com/wiki/WebAPI/GetPlayerItems
 */
@Component
public class InventoryStatusHandler {

    private static final int STATUS_OK = 1;
    private static final int STATUS_STEAM_ID_MISSING = 8;
    private static final int STATUS_IS_PRIVATE = 15;
    private static final int STATUS_STEAM_ID_NOT_EXISTS = 18;

    public boolean isOk(int status) {
        return status == STATUS_OK;
    }

    public String getStatusMessage(int status) {
        return switch (status) {
            case STATUS_OK -> "Success";
            case STATUS_STEAM_ID_MISSING -> "The steamid parameter was invalid or missing";
            case STATUS_IS_PRIVATE -> "User inventory is private";
            case STATUS_STEAM_ID_NOT_EXISTS -> "The Steam ID given does not exist";
            default -> "Unknown inventory status";
        };
    }
}

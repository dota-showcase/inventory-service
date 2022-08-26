package com.dotashowcase.inventoryservice.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class HistoryActionNotFoundException extends InventoryException {

    public HistoryActionNotFoundException(Long steamId, Integer version) {
        super(String.format("HistoryAction with version %d for inventory %d not exists", version, steamId));
    }

    public HistoryActionNotFoundException(Long steamId) {
        super(String.format("There is only one HistoryAction for inventory %d", steamId));
    }
}

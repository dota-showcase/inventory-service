package com.dotashowcase.inventoryservice.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class OperationNotFoundException extends InventoryException {

    public OperationNotFoundException(Long steamId, Integer version) {
        super(String.format("Operation with version %d for Inventory %d not exists", version, steamId));
    }

    public OperationNotFoundException(Long steamId) {
        super(String.format("There are not Operations for Inventory %d", steamId));
    }
}

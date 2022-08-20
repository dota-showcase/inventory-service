package com.dotashowcase.inventoryservice.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Inventory already exists")
public class InventoryAlreadyExistsException extends InventoryException {

}

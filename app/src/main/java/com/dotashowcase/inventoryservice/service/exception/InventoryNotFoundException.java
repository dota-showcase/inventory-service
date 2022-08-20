package com.dotashowcase.inventoryservice.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Inventory not exists")
public class InventoryNotFoundException extends InventoryException {

}

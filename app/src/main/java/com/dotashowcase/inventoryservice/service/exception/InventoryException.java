package com.dotashowcase.inventoryservice.service.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@NoArgsConstructor
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InventoryException extends RuntimeException {

    public InventoryException(String message) {
        super(message);
    }
}
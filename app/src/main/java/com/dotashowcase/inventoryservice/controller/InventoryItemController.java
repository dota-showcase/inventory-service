package com.dotashowcase.inventoryservice.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/")
public class InventoryItemController {

    @GetMapping("inventory-items/{steamId}")
    public long index(@PathVariable long steamId) {
        return steamId;
    }
}
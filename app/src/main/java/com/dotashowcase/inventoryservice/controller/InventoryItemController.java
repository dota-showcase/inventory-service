package com.dotashowcase.inventoryservice.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/")
public class InventoryItemController {

    @GetMapping("inventories/{steamId}/items")
    public Long index(@PathVariable Long steamId) {
        return steamId;
    }
}

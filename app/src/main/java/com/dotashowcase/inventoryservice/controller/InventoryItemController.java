package com.dotashowcase.inventoryservice.controller;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.service.InventoryItemService;
import com.dotashowcase.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/")
public class InventoryItemController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("inventories/{steamId}/items")
    public List<InventoryItem> index(@PathVariable Long steamId) {
        Inventory inventory = inventoryService.get(steamId);

        return inventoryItemService.get(inventory);
    }
}

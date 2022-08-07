package com.dotashowcase.inventoryservice.controller;

import com.dotashowcase.inventoryservice.config.AppConstant;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.service.InventoryItemService;
import com.dotashowcase.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/")
public class InventoryItemController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @GetMapping("inventories/{steamId}/items")
    public Page<InventoryItem> index(
            @PathVariable Long steamId,
            @PageableDefault(page = 0, size = AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE) Pageable pageable
    ) {
        Inventory inventory = inventoryService.get(steamId);

        return inventoryItemService.get(inventory, pageable);
    }
}

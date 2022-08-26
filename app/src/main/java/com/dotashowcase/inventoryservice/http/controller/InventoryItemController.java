package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.config.AppConstant;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.InventoryItemService;
import com.dotashowcase.inventoryservice.service.InventoryService;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryChangesDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
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
    public PageResult<InventoryItemDTO> index(
            @PathVariable Long steamId,
            @PageableDefault(size = AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE) Pageable pageable
    ) {
        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemService.get(inventory, pageable);
    }

    @GetMapping("inventories/{steamId}/changes/{version}")
    public InventoryChangesDTO getChanges(@PathVariable Long steamId, @PathVariable Integer version) {
        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemService.getChanges(inventory, version);
    }

    @GetMapping("inventories/{steamId}/changes")
    public InventoryChangesDTO getLatestChanges(@PathVariable Long steamId) {
        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemService.getChanges(inventory, null);
    }
}

package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.config.AppConstant;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.InventoryItemService;
import com.dotashowcase.inventoryservice.service.InventoryService;
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
            @PageableDefault(page = 0, size = AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE) Pageable pageable
    ) {
        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemService.get(inventory, pageable);
    }
}

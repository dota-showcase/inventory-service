package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.config.AppConstant;
import com.dotashowcase.inventoryservice.http.filter.InventoryItemFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.InventoryItemChangesService;
import com.dotashowcase.inventoryservice.service.InventoryItemService;
import com.dotashowcase.inventoryservice.service.InventoryService;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryChangesDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import com.dotashowcase.inventoryservice.support.validator.SteamIdConstraint;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "inventory-item", description = "the inventory item API")
@Validated
@RestController
@RequestMapping("api/v1/")
public class InventoryItemController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private InventoryItemChangesService inventoryItemChangesService;

    @GetMapping("inventories/{steamId}/items")
    public List<InventoryItemDTO> index(
            @PathVariable @SteamIdConstraint Long steamId,
            @RequestParam Optional<String> sort,
            @RequestParam Optional<List<Integer>> defIndexes,
            @RequestParam Optional<List<Byte>> qualities,
            @RequestParam Optional<Boolean> isTradable,
            @RequestParam Optional<Boolean> isCraftable,
            @RequestParam Optional<Boolean> isEquipped,
            @RequestParam Optional<Boolean> hasAttribute
    ) {
        InventoryItemFilter filter = new InventoryItemFilter(
                defIndexes.orElse(null),
                qualities.orElse(null),
                isTradable.orElse(null),
                isCraftable.orElse(null),
                isEquipped.orElse(null),
                hasAttribute.orElse(null)
        );

        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemService.get(inventory, filter, sort.orElse(null));
    }

    @GetMapping("inventories/{steamId}/items/page")
    public PageResult<InventoryItemDTO> index(
            @PathVariable @SteamIdConstraint Long steamId,
            @PageableDefault(size = AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE) Pageable pageable,
            @RequestParam Optional<String> sort,
            @RequestParam Optional<List<Integer>> defIndexes,
            @RequestParam Optional<List<Byte>> qualities,
            @RequestParam Optional<Boolean> isTradable,
            @RequestParam Optional<Boolean> isCraftable,
            @RequestParam Optional<Boolean> isEquipped,
            @RequestParam Optional<Boolean> hasAttribute
    ) {
        InventoryItemFilter filter = new InventoryItemFilter(
                defIndexes.orElse(null),
                qualities.orElse(null),
                isTradable.orElse(null),
                isCraftable.orElse(null),
                isEquipped.orElse(null),
                hasAttribute.orElse(null)
        );

        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemService.get(inventory, pageable, filter, sort.orElse(null));
    }

    @GetMapping("inventories/{steamId}/changes")
    public Map<Integer, InventoryChangesDTO> getChanges(@PathVariable @SteamIdConstraint Long steamId) {
        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemChangesService.get(inventory);
    }

    @GetMapping("inventories/{steamId}/changes/{version}")
    public InventoryChangesDTO getChanges(
            @PathVariable @SteamIdConstraint Long steamId,
            @PathVariable Integer version
    ) {
        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemChangesService.get(inventory, version == -1 ? null : version);
    }
}

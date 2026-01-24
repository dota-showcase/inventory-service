package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.http.filter.InventoryItemChangeFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.type.ChangeType;

import java.util.List;

public interface InventoryItemChangesService {

    List<InventoryItemDTO> get(Inventory inventory, Integer version, ChangeType type, InventoryItemChangeFilter filter);
}

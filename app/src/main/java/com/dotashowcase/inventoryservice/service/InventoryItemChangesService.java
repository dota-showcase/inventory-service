package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryChangesDTO;

public interface InventoryItemChangesService {

    InventoryChangesDTO get(Inventory inventory, Integer version);
}

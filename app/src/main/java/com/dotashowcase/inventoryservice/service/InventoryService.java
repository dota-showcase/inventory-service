package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestOperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithOperationsDTO;

import java.util.List;

public interface InventoryService {

    Inventory findInventory(Long steamId);

    List<InventoryWithOperationsDTO> getAll(String sortBy);

    InventoryWithOperationsDTO get(Long steamId);

    InventoryWithLatestOperationDTO create(Long steamId);

    InventoryWithLatestOperationDTO update(Long steamId);

    void delete(Long steamId);
}

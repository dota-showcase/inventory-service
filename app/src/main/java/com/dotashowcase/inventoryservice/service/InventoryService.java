package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithHistoriesDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestHistoryDTO;

import java.util.List;

public interface InventoryService {

    Inventory findInventory(Long steamId);

    List<InventoryWithHistoriesDTO> getAll(String sortBy);

    InventoryWithHistoriesDTO get(Long steamId);

    InventoryWithLatestHistoryDTO create(Long steamId);

    InventoryWithLatestHistoryDTO update(Long steamId);

    void delete(Long steamId);
}

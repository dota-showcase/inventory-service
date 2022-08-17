package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryItemService {

    Page<InventoryItem> get(Inventory inventory, Pageable pageable);

    List<InventoryItem> create(Inventory inventory, HistoryAction currentHistoryAction, List<ItemDTO> responseItems);

    int sync(Inventory inventory, HistoryAction currentHistoryAction, List<ItemDTO> responseItems);

    long delete(Inventory inventory);
}

package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryChangesDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryItemService {

    PageResult<InventoryItemDTO> get(Inventory inventory, Pageable pageable);

    InventoryChangesDTO getChanges(Inventory inventory, Integer version);

    List<InventoryItem> create(Inventory inventory, Operation currentOperation, List<ItemDTO> responseItems);

    int sync(Inventory inventory, Operation currentOperation, List<ItemDTO> responseItems);

    long delete(Inventory inventory);
}

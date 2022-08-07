package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;

import java.util.List;

public interface InventoryItemService {

    public List<InventoryItem> get(Inventory inventory);

    public List<InventoryItem> create(Inventory inventory, List<ItemDTO> responseItems);
}

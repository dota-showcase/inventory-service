package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;

import java.util.List;

public interface InventoryItemDAL {

    List<InventoryItem> findAll(Inventory inventory);

    List<InventoryItem> insertAll(List<InventoryItem> inventoryItems);
}

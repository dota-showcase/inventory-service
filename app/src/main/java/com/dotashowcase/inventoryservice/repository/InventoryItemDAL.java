package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryItemDAL {

    Page<InventoryItem> findAll(Inventory inventory, Pageable pageable);

    List<InventoryItem> insertAll(List<InventoryItem> inventoryItems);
}

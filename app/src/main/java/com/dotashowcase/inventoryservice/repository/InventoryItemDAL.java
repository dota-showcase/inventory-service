package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InventoryItemDAL {

    Page<InventoryItem> findAll(Inventory inventory, Pageable pageable);

    Map<Long, InventoryItem> findAllActive(Inventory inventory);

    List<InventoryItem> insertAll(List<InventoryItem> inventoryItems);

    long removeAll(Inventory inventory, Set<Long> ids);
}

package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InventoryItemDAL {

    Page<InventoryItem> findAll(Inventory inventory, Pageable pageable);

    Map<Long, InventoryItem> findAll(Inventory inventory);

    Map<Long, InventoryItem> findAll(Inventory inventory, HistoryAction action);

    List<InventoryItem> insertAll(List<InventoryItem> inventoryItems);

    long updateAll(Set<ObjectId> ids, AbstractMap.SimpleImmutableEntry<String, Object> updateEntry);

    long removeAll(Inventory inventory);

//    long removeAll(Inventory inventory, Set<Long> ids);
}
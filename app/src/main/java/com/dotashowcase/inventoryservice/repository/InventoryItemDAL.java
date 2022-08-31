package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

public interface InventoryItemDAL {

    Page<InventoryItem> findAll(Inventory inventory, Pageable pageable);

    List<InventoryItem> findAll(Inventory inventory);

    List<InventoryItem> findAll(Inventory inventory, Operation operation);

    List<InventoryItem> insertAll(List<InventoryItem> inventoryItems);

    long updateAll(Set<ObjectId> ids, List<AbstractMap.SimpleImmutableEntry<String, Object>> updateEntry);

    long removeAll(Inventory inventory);

//    long removeAll(Inventory inventory, Set<Long> ids);
}

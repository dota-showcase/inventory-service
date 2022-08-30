package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;

import java.util.List;

public interface OperationDAL {

    List<Operation> findByInventories(List<Long> inventoryIds);

    Operation findLatest(Inventory inventory);

    List<Operation> findNLatest(Inventory inventory, int limit);

    Operation findByVersion(Inventory inventory, int version);

    Operation insertOne(Operation operation);

    long updateMeta(Operation operation, OperationMeta meta);

    long removeAll(Inventory inventory);
}

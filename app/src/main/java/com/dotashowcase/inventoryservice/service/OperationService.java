package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.support.HistoryRangeCriteria;

import java.util.List;
import java.util.Map;

public interface OperationService {

    Map<Long, List<Operation>> getAll(List<Long> inventoryIds);

    Operation getLatest(Inventory inventory);

    Operation getByVersion(Inventory inventory, Integer version);

    Operation create(
            Inventory inventory,
            Operation.Type type,
            Operation prevOperation
    );

    void createAndSaveMeta(Operation operation, Integer count, Integer operations, Integer numSlots);

    long delete(Inventory inventory);
}

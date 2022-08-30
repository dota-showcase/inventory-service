package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.service.result.dto.OperationCountDTO;

import java.util.List;
import java.util.Map;

public interface OperationService {

    Map<Long, List<Operation>> getAll(List<Long> inventoryIds);

    List<Operation> getAll(Inventory inventory);

    Operation getLatest(Inventory inventory);

    Operation getByVersion(Inventory inventory, Integer version);

    Operation create(Inventory inventory, Operation.Type type, Operation prevOperation);

    void createAndSaveMeta(Operation operation, OperationCountDTO operations, Integer count, Integer numSlots);

    long delete(Inventory inventory);
}

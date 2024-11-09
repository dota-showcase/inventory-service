package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface OperationDAL {

    // TODO: tmp not used, because of bug in vendor
    List<Operation> aggregateLatestByInventories(List<Long> inventoryIds);

    // TODO: tmp method to replace aggregateLatestByInventories
    List<Operation> findLatestByInventoriesNPlusOne(List<Long> inventorySteamIds);

    Page<Operation> findPage(Inventory inventory, Pageable pageable, Sort sort);

    Operation findLatest(Inventory inventory);

    Operation findByVersion(Inventory inventory, int version);

    Operation insertOne(Operation operation);

    long updateMeta(Operation operation, OperationMeta meta);

    long removeAll(Inventory inventory);
}

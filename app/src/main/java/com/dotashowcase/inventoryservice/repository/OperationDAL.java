package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.http.filter.InventoryOperationFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface OperationDAL {

    List<Operation> aggregateLatestByInventories(List<Long> inventoryIds);

    Page<Operation> findPage(Inventory inventory, Pageable pageable, InventoryOperationFilter filter, Sort sort);

    Operation findLatest(Inventory inventory);

    Operation findByVersion(Inventory inventory, int version);

    Operation insertOne(Operation operation);

    long updateMeta(Operation operation, OperationMeta meta);

    long removeAll(Inventory inventory);
}

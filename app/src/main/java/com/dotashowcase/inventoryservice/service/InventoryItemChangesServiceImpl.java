package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.http.filter.InventoryItemChangeFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.mapper.InventoryItemServiceResultMapper;
import com.dotashowcase.inventoryservice.service.type.ChangeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

@Service
public class InventoryItemChangesServiceImpl implements InventoryItemChangesService {

    private final InventoryItemDALRepository inventoryItemRepository;

    private final InventoryItemServiceResultMapper inventoryItemServiceResultMapper;

    private final OperationService operationService;

    @Autowired
    public InventoryItemChangesServiceImpl(
            InventoryItemDALRepository inventoryItemRepository,
            OperationService operationService
    ) {
        Assert.notNull(inventoryItemRepository, "InventoryItemDALRepository must not be null!");
        this.inventoryItemRepository = inventoryItemRepository;

        Assert.notNull(operationService, "OperationService must not be null!");
        this.operationService = operationService;

        this.inventoryItemServiceResultMapper = new InventoryItemServiceResultMapper();
    }

    @Override
    public List<InventoryItemDTO> get(
            Inventory inventory,
            Integer version,
            ChangeType type,
            InventoryItemChangeFilter filter
    ) {
        Operation operation = operationService.getByVersion(inventory, version);

        return inventoryItemRepository.findAll(inventory, operation, type, filter).stream()
                .map(inventoryItemServiceResultMapper::getInventoryItemDTO)
                .toList();
    }
}

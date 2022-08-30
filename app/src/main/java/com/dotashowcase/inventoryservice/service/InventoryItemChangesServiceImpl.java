package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryChangesDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.mapper.InventoryItemServiceResultMapper;
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
    public InventoryChangesDTO get(Inventory inventory, Integer version) {
        Operation operation = operationService.getByVersion(inventory, version);
        List<InventoryItem> items = inventoryItemRepository.findAll(inventory, operation);

        return mapChanges(items);
    }

    @Override
    public Map<Integer, InventoryChangesDTO> get(Inventory inventory) {
        Map<Integer, InventoryChangesDTO> result = new LinkedHashMap<>();
        List<Operation> operations = operationService.getAll(inventory);

        for (Operation operation : operations) {
            List<InventoryItem> items = inventoryItemRepository.findAll(inventory, operation);

            result.put(operation.getVersion(), mapChanges(items));
        }

        return result;
    }

    // TODO: separated service?
    private InventoryChangesDTO mapChanges(List<InventoryItem> items) {
        InventoryChangesDTO result = new InventoryChangesDTO();

        List<InventoryItemDTO> createList = result.getCreate();
        List<InventoryItemDTO> updateList = result.getUpdate();
        List<InventoryItemDTO> deleteList = result.getDelete();

        for (InventoryItem item : items) {
            if (item.getDeleteOperationId() != null) {
                deleteList.add(inventoryItemServiceResultMapper.getInventoryItemDTO(item));

                continue;
            }

            Operation.Type operationType = item.getOperationType();
            if (operationType == Operation.Type.C) {
                createList.add(inventoryItemServiceResultMapper.getInventoryItemDTO(item));
            } else if (operationType == Operation.Type.U) {
                updateList.add(inventoryItemServiceResultMapper.getInventoryItemDTO(item));
            }
        }

        return result;
    }
}

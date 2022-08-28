package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.mapper.InventoryItemMapper;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryChangesDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import com.dotashowcase.inventoryservice.service.result.mapper.InventoryItemServiceResultMapper;
import com.dotashowcase.inventoryservice.service.result.mapper.PageMapper;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemDALRepository inventoryItemRepository;

    private final InventoryItemMapper inventoryItemMapper;

    private final InventoryItemServiceResultMapper inventoryItemServiceResultMapper;

    private final OperationService operationService;

    private final PageMapper<InventoryItem, InventoryItemDTO> pageMapper;

    @Autowired
    public InventoryItemServiceImpl(
            InventoryItemDALRepository inventoryItemRepository,
            OperationService operationService,
            PageMapper<InventoryItem, InventoryItemDTO> pageMapper
    ) {
        Assert.notNull(inventoryItemRepository, "InventoryItemDALRepository must not be null!");
        this.inventoryItemRepository = inventoryItemRepository;

        Assert.notNull(operationService, "OperationService must not be null!");
        this.operationService = operationService;

        Assert.notNull(pageMapper, "PageMapper<InventoryItem, InventoryItemDTO> must not be null!");
        this.pageMapper = pageMapper;

        this.inventoryItemMapper = new InventoryItemMapper();

        this.inventoryItemServiceResultMapper = new InventoryItemServiceResultMapper();
    }

    @Override
    public PageResult<InventoryItemDTO> get(Inventory inventory, Pageable pageable) {
        Page<InventoryItem> inventoryItems = inventoryItemRepository.findAll(inventory, pageable);

        return pageMapper.getPageResult(inventoryItems, inventoryItemServiceResultMapper::getInventoryItemDTO);
    }

    @Override
    public InventoryChangesDTO getChanges(Inventory inventory, Integer version) {
        Operation operation = operationService.getByVersion(inventory, version);
        List<InventoryItem> items = inventoryItemRepository.findAll(inventory, operation);

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

    @Override
    public List<InventoryItem> create(
            Inventory inventory, Operation currentOperation, List<ItemDTO> responseItems
    ) {
        List<InventoryItem> inventoryItems = inventoryItemMapper.itemDtoToInventoryItem(responseItems);

        for (InventoryItem inventoryItem : inventoryItems) {
            inventoryItem.setOperationId(currentOperation.getId());
            inventoryItem.setSteamId(inventory.getSteamId());
        }

        return inventoryItemRepository.insertAll(inventoryItems);
    }

    @Override
    public int sync(Inventory inventory, Operation currentOperation, List<ItemDTO> responseItems) {
        int operations = 0;
        List<InventoryItem> steamInventoryItems = inventoryItemMapper.itemDtoToInventoryItem(responseItems);
        Map<Long, InventoryItem> inventoryItemsById = inventoryItemRepository.findAll(inventory);

        Set<Long> itemIdsToRemove = new HashSet<>(inventoryItemsById.keySet());
        List<InventoryItem> itemsToCreate = new ArrayList<>();
        Set<ObjectId> itemIdsToUpdate = new HashSet<>();

        for (InventoryItem steamInventoryItem : steamInventoryItems) {
            Long id = steamInventoryItem.getItemId();
            itemIdsToRemove.remove(id);

            if (inventoryItemsById.containsKey(id)) {
                InventoryItem storedInventoryItem = inventoryItemsById.get(id);

                if (!Objects.equals(steamInventoryItem, storedInventoryItem)) {
                    itemIdsToUpdate.add(storedInventoryItem.getId());

                    // insert new
                    prepareItemToCreate(steamInventoryItem, currentOperation, Operation.Type.U);
                    itemsToCreate.add(steamInventoryItem);
                }

                continue;
            }

            prepareItemToCreate(steamInventoryItem, currentOperation, Operation.Type.C);
            itemsToCreate.add(steamInventoryItem);
        }

        if (itemIdsToRemove.size() > 0) {
            Set<ObjectId> itemIdsToHide = new HashSet<>();

            for (InventoryItem steamInventoryItem : steamInventoryItems) {
                Long itemId = steamInventoryItem.getItemId();
                if (itemIdsToRemove.contains(itemId)) {
                    itemIdsToHide.add(inventoryItemsById.get(itemId).getId());
                }
            }

            operations += inventoryItemRepository.updateAll(
                    itemIdsToHide,
                    new ArrayList<>(List.of(
                        new AbstractMap.SimpleImmutableEntry<>("_isA", false),
                        new AbstractMap.SimpleImmutableEntry<>("_odId", currentOperation.getId()) // deleteOperationId
                    ))
            );

//            operations += inventoryItemRepository.removeAll(inventory, itemIdsToRemove);
        }

        operations += inventoryItemRepository.updateAll(
                itemIdsToUpdate,
                new ArrayList<>(List.of(
                    new AbstractMap.SimpleImmutableEntry<>("_isA", false)
                ))
        );

        operations += inventoryItemRepository.insertAll(itemsToCreate).size();

        // TODO: store in crd meta

        return operations;
    }

    private void prepareItemToCreate(InventoryItem inventoryItem,
                                     Operation currentOperation,
                                     Operation.Type operationType) {
        inventoryItem.setOperationId(currentOperation.getId());
        inventoryItem.setOperationType(operationType);
    }

    public long delete(Inventory inventory) {
        return inventoryItemRepository.removeAll(inventory);
    }
}

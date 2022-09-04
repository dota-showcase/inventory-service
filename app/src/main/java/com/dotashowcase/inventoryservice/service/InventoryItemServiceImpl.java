package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.http.filter.InventoryItemFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.mapper.InventoryItemMapper;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.dto.OperationCountDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import com.dotashowcase.inventoryservice.service.result.mapper.InventoryItemServiceResultMapper;
import com.dotashowcase.inventoryservice.service.result.mapper.PageMapper;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.support.SortBuilder;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemDALRepository inventoryItemRepository;

    private final InventoryItemMapper inventoryItemMapper;

    private final InventoryItemServiceResultMapper inventoryItemServiceResultMapper;

    private final SortBuilder sortBuilder;

    private final PageMapper<InventoryItem, InventoryItemDTO> pageMapper;

    @Autowired
    public InventoryItemServiceImpl(
            InventoryItemDALRepository inventoryItemRepository,
            SortBuilder sortBuilder,
            PageMapper<InventoryItem, InventoryItemDTO> pageMapper
    ) {
        Assert.notNull(inventoryItemRepository, "InventoryItemDALRepository must not be null!");
        this.inventoryItemRepository = inventoryItemRepository;

        Assert.notNull(sortBuilder, "SortBuilder must not be null!");
        this.sortBuilder = sortBuilder;

        Assert.notNull(pageMapper, "PageMapper<InventoryItem, InventoryItemDTO> must not be null!");
        this.pageMapper = pageMapper;

        this.inventoryItemMapper = new InventoryItemMapper();

        this.inventoryItemServiceResultMapper = new InventoryItemServiceResultMapper();
    }

    @Override
    public List<InventoryItemDTO> get(Inventory inventory, InventoryItemFilter filter, String sortBy) {
        Sort sort = sortBuilder.fromRequestParam(sortBy);

        return inventoryItemRepository.searchAll(inventory, filter, sort)
                .stream()
                .map(inventoryItemServiceResultMapper::getInventoryItemDTO)
                .toList();
    }

    @Override
    public PageResult<InventoryItemDTO> get(
            Inventory inventory,
            Pageable pageable,
            InventoryItemFilter filter,
            String sortBy
    ) {
        Sort sort = sortBuilder.fromRequestParam(sortBy);
        Page<InventoryItem> inventoryItems = inventoryItemRepository.searchAll(inventory, pageable, filter, sort);

        return pageMapper.getPageResult(inventoryItems, inventoryItemServiceResultMapper::getInventoryItemDTO);
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
    public OperationCountDTO sync(Inventory inventory, Operation currentOperation, List<ItemDTO> responseItems) {
        int createCount = 0, updateCount = 0, deleteCount = 0;
        List<InventoryItem> steamInventoryItems = inventoryItemMapper.itemDtoToInventoryItem(responseItems);
        Map<Long, InventoryItem> inventoryItemsById = inventoryItemRepository
                .findAll(inventory)
                .stream()
                .collect(Collectors.toMap(InventoryItem::getItemId, Function.identity()));

        Set<Long> itemIdsToRemove = new HashSet<>(inventoryItemsById.keySet());
        List<InventoryItem> itemsToCreate = new ArrayList<>();
        Set<ObjectId> itemIdsToUpdate = new HashSet<>();

        Long steamId = inventory.getSteamId();

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
            steamInventoryItem.setSteamId(steamId);
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

            deleteCount += inventoryItemRepository.updateAll(
                    itemIdsToHide,
                    new ArrayList<>(List.of(
                        new AbstractMap.SimpleImmutableEntry<>("_isA", false),
                        new AbstractMap.SimpleImmutableEntry<>("_odId", currentOperation.getId()) // deleteOperationId
                    ))
            );

//            operations += inventoryItemRepository.removeAll(inventory, itemIdsToRemove);
        }

        updateCount += inventoryItemRepository.updateAll(
                itemIdsToUpdate,
                new ArrayList<>(List.of(
                    new AbstractMap.SimpleImmutableEntry<>("_isA", false)
                ))
        );

        createCount += inventoryItemRepository.insertAll(itemsToCreate).size();

        return new OperationCountDTO(createCount, updateCount, deleteCount, inventoryItemsById.size());
    }

    public long delete(Inventory inventory) {
        return inventoryItemRepository.removeAll(inventory);
    }

    private void prepareItemToCreate(InventoryItem inventoryItem,
                                     Operation currentOperation,
                                     Operation.Type operationType) {
        inventoryItem.setOperationId(currentOperation.getId());
        inventoryItem.setOperationType(operationType);
    }
}

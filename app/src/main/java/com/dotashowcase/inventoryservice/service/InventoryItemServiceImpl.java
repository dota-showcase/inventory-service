package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.mapper.InventoryItemMapper;
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

    @Autowired
    public InventoryItemServiceImpl(InventoryItemDALRepository inventoryItemRepository) {
        Assert.notNull(inventoryItemRepository, "InventoryItemDALRepository must not be null!");
        this.inventoryItemRepository = inventoryItemRepository;

        this.inventoryItemMapper = new InventoryItemMapper();
    }

    @Override
    public Page<InventoryItem> get(Inventory inventory, Pageable pageable) {
        return this.inventoryItemRepository.findAll(inventory, pageable);
    }

    @Override
    public List<InventoryItem> create(Inventory inventory, List<ItemDTO> responseItems) {
        List<InventoryItem> inventoryItems = inventoryItemMapper.itemDtoToInventoryItem(responseItems);

        for (InventoryItem inventoryItem : inventoryItems) {
            inventoryItem.setSteamId(inventory.getSteamId());
        }

        return inventoryItemRepository.insertAll(inventoryItems);
    }

    @Override
    public int sync(Inventory inventory, List<ItemDTO> responseItems, HistoryAction currentHistoryAction) {
        int operations = 0;
        List<InventoryItem> steamInventoryItems = inventoryItemMapper.itemDtoToInventoryItem(responseItems);
        Map<Long, InventoryItem> inventoryItemsById = inventoryItemRepository.findAllActive(inventory);

        Set<Long> itemIdsToRemove = new HashSet<>(inventoryItemsById.keySet());
        List<InventoryItem> itemsToCreate = new ArrayList<>();
        Set<ObjectId> itemIdsToUpdate = new HashSet<>();

        for (InventoryItem steamInventoryItem: steamInventoryItems) {
            Long id = steamInventoryItem.getItemId();
            itemIdsToRemove.remove(id);

            if (inventoryItemsById.containsKey(id)) {
                InventoryItem storedInventoryItem = inventoryItemsById.get(id);

                if (!Objects.equals(steamInventoryItem, storedInventoryItem)) {
                    itemIdsToUpdate.add(steamInventoryItem.getId());

                    // insert new
                    prepareItemToCreate(steamInventoryItem, currentHistoryAction);
                    itemsToCreate.add(steamInventoryItem);
                }

                continue;
            }

            prepareItemToCreate(steamInventoryItem, currentHistoryAction);
            itemsToCreate.add(steamInventoryItem);
        }

        if (itemIdsToRemove.size() > 0) {
            Set<ObjectId> itemIdsToHide = new HashSet<>();

            for (InventoryItem steamInventoryItem: steamInventoryItems) {
                if (itemIdsToRemove.contains(steamInventoryItem.getItemId())) {
                    itemIdsToHide.add(steamInventoryItem.getId());
                }
            }

            operations += inventoryItemRepository.updateAll(
                    itemIdsToHide,
                    new AbstractMap.SimpleImmutableEntry<>("status", false)
            );

//            operations += inventoryItemRepository.removeAll(inventory, itemIdsToRemove);
        }

        operations += inventoryItemRepository.updateAll(
                itemIdsToUpdate,
                new AbstractMap.SimpleImmutableEntry<>("status", false)
        );
        operations += inventoryItemRepository.insertAll(itemsToCreate).size();

        return operations;
    }

    private void prepareItemToCreate(InventoryItem inventoryItem, HistoryAction historyAction) {
        inventoryItem.setHistoryActionId(historyAction.getId());
    }

    public long delete(Inventory inventory) {
        return inventoryItemRepository.removeAll(inventory);
    }
}

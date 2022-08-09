package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.mapper.InventoryItemMapper;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
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
    public int sync(Inventory inventory, List<ItemDTO> responseItems) {
        int operations = 0;
        List<InventoryItem> steamInventoryItems = inventoryItemMapper.itemDtoToInventoryItem(responseItems);
        Map<Long, InventoryItem> inventoryItemsById = inventoryItemRepository.findAllActive(inventory);

        Set<Long> itemIdsToRemove = new HashSet<>(inventoryItemsById.keySet());
        List<InventoryItem> itemsToCreate = new ArrayList<>();
        // TODO: item to create and update
        List<InventoryItem> itemsToUpdate = new ArrayList<>();

        for (InventoryItem steamInventoryItem: steamInventoryItems) {
            Long id = steamInventoryItem.getId();
            itemIdsToRemove.remove(id);

            if (inventoryItemsById.containsKey(id)) {
                InventoryItem storedInventoryItem = inventoryItemsById.get(id);

                if (!Objects.equals(steamInventoryItem, storedInventoryItem)) {
                    itemsToUpdate.add(storedInventoryItem);
                }

                continue;
            }

            itemsToCreate.add(steamInventoryItem);
        }

        operations += inventoryItemRepository.insertAll(itemsToCreate).size();
        operations += inventoryItemRepository.removeAll(inventory, itemIdsToRemove);

        return operations;
    }
}

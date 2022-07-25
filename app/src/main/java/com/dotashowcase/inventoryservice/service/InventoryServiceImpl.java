package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.repository.InventoryItemRepository;
import com.dotashowcase.inventoryservice.repository.InventoryRepository;
import com.dotashowcase.inventoryservice.support.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    private final InventoryRepository inventoryRepository;

    private final SortBuilder sortBuilder;

    @Autowired
    public InventoryServiceImpl(
            InventoryItemRepository inventoryItemRepository,
            InventoryRepository inventoryRepository,
            SortBuilder sortBuilder
    ) {
        Assert.notNull(inventoryItemRepository, "InventoryItemRepository must not be null!");
        this.inventoryItemRepository = inventoryItemRepository;

        Assert.notNull(inventoryRepository, "InventoryRepository must not be null!");
        this.inventoryRepository = inventoryRepository;

        Assert.notNull(sortBuilder, "SortBuilder must not be null!");
        this.sortBuilder = sortBuilder;
    }

    @Override
    public List<Inventory> getAll(String sortBy) {
        Sort sort = this.sortBuilder.fromRequestParam(sortBy);

        return sort != null
                ? this.inventoryRepository.findAll(sort)
                : this.inventoryRepository.findAll();
    }

    @Override
    public Inventory get(Long steamId) {
        return this.findInventory(steamId);
    }

    @Override
    public Inventory create(Long steamId) {
        Inventory existingInventory = this.findInventory(steamId);

        if (existingInventory != null) {
            // TODO: throw exception

            return existingInventory;
        }

        // first create items then meta

        Inventory inventory = inventoryRepository.save(new Inventory(steamId));

        return inventory;
    }

    @Override
    public Inventory update(Long steamId) {
        Inventory existingInventory = this.findInventory(steamId);

//        if (existingInventory == null) {
//            // TODO: throw exception
//        }

        // TODO: for testing
        existingInventory.setCount(123);

        Inventory inventory = inventoryRepository.save(existingInventory);

        return inventory;
    }

    @Override
    public void delete(Long steamId) {
        Inventory existingInventory = this.findInventory(steamId);

        if (existingInventory == null) {
            // TODO: exception
        }

        inventoryRepository.delete(existingInventory);
    }

    private Inventory findInventory(Long steamId) {
        return inventoryRepository.findItemBySteamId(steamId);
    }
}

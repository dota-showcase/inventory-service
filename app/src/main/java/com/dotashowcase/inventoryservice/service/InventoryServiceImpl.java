package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.InventoryMeta;
import com.dotashowcase.inventoryservice.repository.InventoryItemRepository;
import com.dotashowcase.inventoryservice.repository.InventoryMetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    private final InventoryMetaRepository inventoryMetaRepository;

    @Autowired
    public InventoryServiceImpl(
            InventoryItemRepository inventoryItemRepository,
            InventoryMetaRepository inventoryMetaRepository
    ) {
        Assert.notNull(inventoryItemRepository, "InventoryItemRepository must not be null!");
        this.inventoryItemRepository = inventoryItemRepository;

        Assert.notNull(inventoryMetaRepository, "InventoryMetaRepository must not be null!");
        this.inventoryMetaRepository = inventoryMetaRepository;
    }

    @Override
    public InventoryMeta create(Long steamId) {
        InventoryMeta existingInventoryMeta = this.findInventoryMeta(steamId);

        if (existingInventoryMeta != null) {
            // TODO: throw exception

            return existingInventoryMeta;
        }

        InventoryMeta inventoryMeta = inventoryMetaRepository.save(new InventoryMeta(steamId));

        return inventoryMeta;
    }

    private InventoryMeta findInventoryMeta(Long steamId) {
        return inventoryMetaRepository.findItemBySteamId(steamId);
    }
}

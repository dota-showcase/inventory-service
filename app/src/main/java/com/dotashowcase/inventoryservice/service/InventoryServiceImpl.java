package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.InventoryMeta;
import com.dotashowcase.inventoryservice.repository.InventoryItemRepository;
import com.dotashowcase.inventoryservice.repository.InventoryMetaRepository;
import com.dotashowcase.inventoryservice.support.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    private final InventoryMetaRepository inventoryMetaRepository;

    private final SortBuilder sortBuilder;

    @Autowired
    public InventoryServiceImpl(
            InventoryItemRepository inventoryItemRepository,
            InventoryMetaRepository inventoryMetaRepository,
            SortBuilder sortBuilder
    ) {
        Assert.notNull(inventoryItemRepository, "InventoryItemRepository must not be null!");
        this.inventoryItemRepository = inventoryItemRepository;

        Assert.notNull(inventoryMetaRepository, "InventoryMetaRepository must not be null!");
        this.inventoryMetaRepository = inventoryMetaRepository;

        Assert.notNull(sortBuilder, "SortBuilder must not be null!");
        this.sortBuilder = sortBuilder;
    }

    @Override
    public List<InventoryMeta> getAll(String sortBy) {
        Sort sort = this.sortBuilder.fromRequestParam(sortBy);

        return sort != null
                ? this.inventoryMetaRepository.findAll(sort)
                : this.inventoryMetaRepository.findAll();
    }

    @Override
    public InventoryMeta create(Long steamId) {
        InventoryMeta existingInventoryMeta = this.findInventoryMeta(steamId);

        if (existingInventoryMeta != null) {
            // TODO: throw exception

            return existingInventoryMeta;
        }

        // first create items then meta

        InventoryMeta inventoryMeta = inventoryMetaRepository.save(new InventoryMeta(steamId));

        return inventoryMeta;
    }

    private InventoryMeta findInventoryMeta(Long steamId) {
        return inventoryMetaRepository.findItemBySteamId(steamId);
    }
}

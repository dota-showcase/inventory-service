package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.constant.HistoryActionType;
import com.dotashowcase.inventoryservice.repository.InventoryRepository;
import com.dotashowcase.inventoryservice.steamclient.SteamClient;
import com.dotashowcase.inventoryservice.steamclient.exception.SteamException;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.UserInventoryResponseDTO;
import com.dotashowcase.inventoryservice.support.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemService inventoryItemService;

    private final HistoryActionService historyActionService;

    private final InventoryRepository inventoryRepository;

    private final SortBuilder sortBuilder;

    private final SteamClient steamClient;

    @Autowired
    public InventoryServiceImpl(
            InventoryItemService inventoryItemService,
            HistoryActionService historyActionService,
            InventoryRepository inventoryRepository,
            SortBuilder sortBuilder,
            SteamClient steamClient
    ) {
        Assert.notNull(inventoryItemService, "InventoryItemService must not be null!");
        this.inventoryItemService = inventoryItemService;

        Assert.notNull(historyActionService, "HistoryActionService must not be null!");
        this.historyActionService = historyActionService;

        Assert.notNull(inventoryRepository, "InventoryRepository must not be null!");
        this.inventoryRepository = inventoryRepository;

        Assert.notNull(sortBuilder, "SortBuilder must not be null!");
        this.sortBuilder = sortBuilder;

        Assert.notNull(steamClient, "SteamClient must not be null!");
        this.steamClient = steamClient;
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

        UserInventoryResponseDTO inventoryResponseDTO;

        try {
            inventoryResponseDTO = steamClient.fetchUserInventory(steamId);
        } catch (SteamException steamException) {
            // TODO: throw exception
            return new Inventory();
        }

        List<ItemDTO> responseItems = inventoryResponseDTO.getItems();

        // store Inventory to get _id
        Inventory savedInventory = inventoryRepository.save(new Inventory(steamId));

        // store items
        List<InventoryItem> savedInventoryItems = inventoryItemService.create(savedInventory, responseItems);

        historyActionService.create(
                savedInventory,
                null,
                savedInventoryItems.size(),
                responseItems.size(),
                inventoryResponseDTO.getNumberBackpackSlots()
        );

        return savedInventory;
    }

    @Override
    public Inventory update(Long steamId) {
        Inventory existingInventory = this.findInventory(steamId);

        if (existingInventory == null) {
            // TODO
           throw new RuntimeException("Inventory not exists");
        }

        UserInventoryResponseDTO inventoryResponseDTO;

        try {
            inventoryResponseDTO = steamClient.fetchUserInventory(steamId);
        } catch (SteamException steamException) {
            // TODO: throw exception
            return new Inventory();
        }

        List<ItemDTO> responseItems = inventoryResponseDTO.getItems();
        int result = inventoryItemService.sync(existingInventory, responseItems);

        historyActionService.create(
                existingInventory,
                HistoryActionType.UPDATE,
                0, // TODO
                responseItems.size(),
                inventoryResponseDTO.getNumberBackpackSlots()
        );

        return existingInventory;
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

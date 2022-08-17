package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.repository.InventoryRepository;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithHistoriesDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestHistoryDTO;
import com.dotashowcase.inventoryservice.service.result.mapper.InventoryServiceResultMapper;
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
import java.util.Map;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemService inventoryItemService;

    private final HistoryActionService historyActionService;

    private final InventoryRepository inventoryRepository;

    private final SortBuilder sortBuilder;

    private final SteamClient steamClient;

    private final InventoryServiceResultMapper inventoryServiceResultMapper;

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

        this.inventoryServiceResultMapper = new InventoryServiceResultMapper();
    }

    @Override
    public List<InventoryWithHistoriesDTO> getAll(String sortBy) {
        Sort sort = this.sortBuilder.fromRequestParam(sortBy);

        List<Inventory> inventories = sort != null
                ? this.inventoryRepository.findAll(sort)
                : this.inventoryRepository.findAll();

        Map<Long, List<HistoryAction>> historyActions = historyActionService.getAll(
                inventories.stream().map(Inventory::getSteamId).toList()
        );

        return inventoryServiceResultMapper.getInventoriesWithHistoriesDTO(inventories, historyActions);
    }

    @Override
    public InventoryWithHistoriesDTO get(Long steamId) {
        Inventory inventory = this.findInventory(steamId);

        // process inventory as list
        Map<Long, List<HistoryAction>> historyActions = historyActionService.getAll(List.of(steamId));

        return inventoryServiceResultMapper.getInventoryWithHistoriesDTO(inventory, historyActions.get(steamId));
    }

    @Override
    public InventoryWithLatestHistoryDTO create(Long steamId) {
        Inventory existingInventory = inventoryRepository.findItemBySteamId(steamId);

        if (existingInventory != null) {
            // TODO: throw exception

            return new InventoryWithLatestHistoryDTO();
        }

        UserInventoryResponseDTO inventoryResponseDTO;

        try {
            inventoryResponseDTO = steamClient.fetchUserInventory(steamId);
        } catch (SteamException steamException) {
            System.out.println(steamException.getMessage());
            return new InventoryWithLatestHistoryDTO();
        }

        List<ItemDTO> responseItems = inventoryResponseDTO.getItems();

        // store Inventory to get _id
        Inventory savedInventory = inventoryRepository.save(new Inventory(steamId));

        // store items
        HistoryAction historyAction = historyActionService.create(savedInventory, null, null);
        List<InventoryItem> savedInventoryItems = inventoryItemService.create(
                savedInventory, historyAction, responseItems
        );

        historyActionService.createAndSaveMeta(
                historyAction,
                savedInventoryItems.size(),
                responseItems.size(),
                inventoryResponseDTO.getNumberBackpackSlots()
        );

        return inventoryServiceResultMapper.getInventoryWithLatestHistoryDTO(savedInventory, historyAction);
    }

    @Override
    public InventoryWithLatestHistoryDTO update(Long steamId) {
        Inventory inventory = this.findInventory(steamId);

        UserInventoryResponseDTO inventoryResponseDTO;

        try {
            inventoryResponseDTO = steamClient.fetchUserInventory(steamId);
        } catch (SteamException steamException) {
            // TODO: throw exception
            return new InventoryWithLatestHistoryDTO();
        }

        HistoryAction prevHistoryAction = historyActionService.getLatest(inventory);

        if (prevHistoryAction == null) {
            // TODO
            throw new RuntimeException("HistoryAction not exists");
        }

        List<ItemDTO> responseItems = inventoryResponseDTO.getItems();

        HistoryAction currentHistoryAction = historyActionService.create(
                inventory,
                HistoryAction.Type.UPDATE,
                prevHistoryAction
        );

        int result = inventoryItemService.sync(inventory, currentHistoryAction, responseItems);

        historyActionService.createAndSaveMeta(
                currentHistoryAction,
                responseItems.size(),
                result,
                inventoryResponseDTO.getNumberBackpackSlots()
        );

        return inventoryServiceResultMapper.getInventoryWithLatestHistoryDTO(inventory, currentHistoryAction);
    }

    @Override
    public void delete(Long steamId) {
        Inventory existingInventory = this.findInventory(steamId);

        inventoryRepository.delete(existingInventory);
        historyActionService.delete(existingInventory);
        inventoryItemService.delete(existingInventory);
    }

    @Override
    public Inventory findInventory(Long steamId) {
        Inventory inventory = inventoryRepository.findItemBySteamId(steamId);

        if (inventory == null) {
            throw new RuntimeException("Inventory not exists");
        }

        return inventory;
    }
}

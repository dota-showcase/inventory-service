package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.repository.InventoryRepository;
import com.dotashowcase.inventoryservice.service.exception.InventoryAlreadyExistsException;
import com.dotashowcase.inventoryservice.service.exception.InventoryException;
import com.dotashowcase.inventoryservice.service.exception.InventoryNotFoundException;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestOperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithOperationsDTO;
import com.dotashowcase.inventoryservice.service.result.dto.OperationCountDTO;
import com.dotashowcase.inventoryservice.service.result.mapper.InventoryServiceResultMapper;
import com.dotashowcase.inventoryservice.steamclient.SteamClient;
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

    private final OperationService operationService;

    private final InventoryRepository inventoryRepository;

    private final SortBuilder sortBuilder;

    private final SteamClient steamClient;

    private final InventoryServiceResultMapper inventoryServiceResultMapper;

    @Autowired
    public InventoryServiceImpl(
            InventoryItemService inventoryItemService,
            OperationService operationService,
            InventoryRepository inventoryRepository,
            SortBuilder sortBuilder,
            SteamClient steamClient
    ) {
        Assert.notNull(inventoryItemService, "InventoryItemService must not be null!");
        this.inventoryItemService = inventoryItemService;

        Assert.notNull(operationService, "OperationService must not be null!");
        this.operationService = operationService;

        Assert.notNull(inventoryRepository, "InventoryRepository must not be null!");
        this.inventoryRepository = inventoryRepository;

        Assert.notNull(sortBuilder, "SortBuilder must not be null!");
        this.sortBuilder = sortBuilder;

        Assert.notNull(steamClient, "SteamClient must not be null!");
        this.steamClient = steamClient;

        this.inventoryServiceResultMapper = new InventoryServiceResultMapper();
    }

    @Override
    public List<InventoryWithOperationsDTO> getAll(String sortBy) {
        Sort sort = sortBuilder.fromRequestParam(sortBy);

        List<Inventory> inventories = sort != null
                ? inventoryRepository.findAll(sort)
                : inventoryRepository.findAll();

        Map<Long, List<Operation>> operations = operationService.getAll(
                inventories.stream().map(Inventory::getSteamId).toList()
        );

        return inventoryServiceResultMapper.getInventoriesWithOperationsDTO(inventories, operations);
    }

    @Override
    public InventoryWithOperationsDTO get(Long steamId) {
        Inventory inventory = findInventory(steamId);

        // process inventory as list
        Map<Long, List<Operation>> operations = operationService.getAll(List.of(steamId));

        return inventoryServiceResultMapper.getInventoryWithOperationsDTO(inventory, operations.get(steamId));
    }

    @Override
    public InventoryWithLatestOperationDTO create(Long steamId) {
        Inventory existingInventory = inventoryRepository.findItemBySteamId(steamId);

        if (existingInventory != null) {
            throw new InventoryAlreadyExistsException();
        }

        UserInventoryResponseDTO inventoryResponseDTO = steamClient.fetchUserInventory(steamId);

        List<ItemDTO> responseItems = inventoryResponseDTO.getItems();

        // store Inventory to get _id
        Inventory savedInventory = inventoryRepository.save(new Inventory(steamId));

        // store items
        Operation operation = operationService.create(savedInventory, null, null);
        List<InventoryItem> savedInventoryItems = inventoryItemService.create(
                savedInventory, operation, responseItems
        );

        operationService.createAndSaveMeta(
                operation,
                new OperationCountDTO(savedInventoryItems.size(), 0, 0, 0),
                responseItems.size(),
                inventoryResponseDTO.getNumberBackpackSlots()
        );

        return inventoryServiceResultMapper.getInventoryWithLatestOperationDTO(savedInventory, operation);
    }

    @Override
    public InventoryWithLatestOperationDTO update(Long steamId) {
        Inventory inventory = findInventory(steamId);
        Operation prevOperation = operationService.getLatest(inventory);

        if (prevOperation == null) {
            throw new InventoryException("Cannot find Inventory Operation resource");
        }

        UserInventoryResponseDTO inventoryResponseDTO = steamClient.fetchUserInventory(steamId);
        List<ItemDTO> responseItems = inventoryResponseDTO.getItems();

        Operation currentOperation = operationService.create(inventory, Operation.Type.U, prevOperation);

        OperationCountDTO operationCountDTO = inventoryItemService.sync(inventory, currentOperation, responseItems);

        operationService.createAndSaveMeta(
                currentOperation,
                operationCountDTO,
                responseItems.size(),
                inventoryResponseDTO.getNumberBackpackSlots()
        );

        return inventoryServiceResultMapper.getInventoryWithLatestOperationDTO(inventory, currentOperation);
    }

    @Override
    public void delete(Long steamId) {
        Inventory existingInventory = findInventory(steamId);

        inventoryRepository.delete(existingInventory);
        operationService.delete(existingInventory);
        inventoryItemService.delete(existingInventory);
    }

    @Override
    public Inventory findInventory(Long steamId) {
        Inventory inventory = inventoryRepository.findItemBySteamId(steamId);

        if (inventory == null) {
            throw new InventoryNotFoundException();
        }

        return inventory;
    }

    @Override
    public Inventory findInventoryWithLatestOperation(Long steamId) {
        Inventory inventory = findInventory(steamId);
        Operation operation = operationService.getLatest(inventory);

        if (operation == null) {
            throw new InventoryException("Cannot find Inventory Operation resource");
        }

        inventory.setLatestOperation(operation);

        return inventory;
    }
}

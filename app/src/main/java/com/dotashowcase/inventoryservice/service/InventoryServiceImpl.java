package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.repository.InventoryRepository;
import com.dotashowcase.inventoryservice.service.exception.InventoryAlreadyExistsException;
import com.dotashowcase.inventoryservice.service.exception.InventoryException;
import com.dotashowcase.inventoryservice.service.exception.InventoryNotFoundException;
import com.dotashowcase.inventoryservice.service.exception.InventoryUpdateConflictException;
import com.dotashowcase.inventoryservice.service.lock.InventorySyncLock;
import com.dotashowcase.inventoryservice.service.result.dto.*;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import com.dotashowcase.inventoryservice.service.result.mapper.InventoryServiceResultMapper;
import com.dotashowcase.inventoryservice.service.result.mapper.PageMapper;
import com.dotashowcase.inventoryservice.steamclient.SteamClient;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.UserInventoryResponseDTO;
import com.dotashowcase.inventoryservice.support.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemService inventoryItemService;

    private final OperationService operationService;

    private final InventoryRepository inventoryRepository;

    private final SortBuilder sortBuilder;

    private static final Set<String> SORT_FIELDS = Set.of("steamId");

    private final SteamClient steamClient;

    private final PageMapper<Inventory, InventoryWithLatestOperationDTO> pageMapper;

    private final InventorySyncLock inventorySyncLock;

    private final InventoryServiceResultMapper inventoryServiceResultMapper;

    @Autowired
    public InventoryServiceImpl(
            InventoryItemService inventoryItemService,
            OperationService operationService,
            InventoryRepository inventoryRepository,
            SortBuilder sortBuilder,
            SteamClient steamClient,
            PageMapper<Inventory, InventoryWithLatestOperationDTO> pageMapper,
            InventorySyncLock inventorySyncLock
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

        Assert.notNull(pageMapper, "PageMapper<Inventory, InventoryWithLatestOperationDTO> must not be null!");
        this.pageMapper = pageMapper;

        Assert.notNull(inventorySyncLock, "InventorySyncLock must not be null!");
        this.inventorySyncLock = inventorySyncLock;

        this.inventoryServiceResultMapper = new InventoryServiceResultMapper();
    }

    @Override
    public List<InventoryDTO> getAll(String sortBy) {
        Sort sort = sortBuilder.fromRequestParam(sortBy, SORT_FIELDS);

        List<Inventory> inventories = sort != null
                ? inventoryRepository.findAll(sort)
                : inventoryRepository.findAll();

        return inventories
                .stream()
                .map(inventoryServiceResultMapper::getInventoryDTO)
                .toList();
    }

    @Override
    public PageResult<InventoryWithLatestOperationDTO> getPage(Pageable pageable, String sortBy) {
        Sort sort = sortBuilder.fromRequestParam(sortBy, SORT_FIELDS);

        Pageable innerPageable = sort != null
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<Inventory> inventoryPage = inventoryRepository.findAll(innerPageable);

        Map<Long, Operation> operations = operationService.getAllLatest(
                inventoryPage.getContent().stream().map(Inventory::getSteamId).toList()
        );

        List<InventoryWithLatestOperationDTO> result = new ArrayList<>();

        for (Inventory inventory : inventoryPage.getContent()) {
            result.add(
                    inventoryServiceResultMapper.getInventoryWithLatestOperationDTO(
                            inventory,
                            operations.get(inventory.getSteamId())
                    )
            );
        }

        return pageMapper.getPageResultWithoutMapping(inventoryPage, result);
    }

    @Override
    public InventoryWithLatestOperationDTO get(Long steamId) {
        Inventory inventory = findInventoryWithLatestOperation(steamId);

        return inventoryServiceResultMapper.getInventoryWithLatestOperationDTO(
                inventory,
                inventory.getLatestOperation()
        );
    }

    @Override
    public InventoryWithLatestOperationDTO create(Long steamId) {
        // block parallel sync of the same inventory
        if (!inventorySyncLock.tryLock(steamId)) {
            throw new InventoryAlreadyExistsException();
        }

        try {
            return doCreate(steamId);
        } finally {
            inventorySyncLock.unlock(steamId);
        }
    }

    private InventoryWithLatestOperationDTO doCreate(Long steamId) {
        Inventory existingInventory = inventoryRepository.findItemBySteamId(steamId);

        if (existingInventory != null) {
            throw new InventoryAlreadyExistsException();
        }

        UserInventoryResponseDTO inventoryResponseDTO = steamClient.fetchUserInventory(steamId);

        List<ItemDTO> responseItems = inventoryResponseDTO.getItems();

        // insert (not upsert) - fails on concurrent create
        Inventory savedInventory;
        try {
            savedInventory = inventoryRepository.insert(new Inventory(steamId));
        } catch (DuplicateKeyException duplicateKeyException) {
            throw new InventoryAlreadyExistsException();
        }

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
        // block parallel sync of the same inventory
        if (!inventorySyncLock.tryLock(steamId)) {
            throw new InventoryUpdateConflictException();
        }

        try {
            return doUpdate(steamId);
        } finally {
            inventorySyncLock.unlock(steamId);
        }
    }

    private InventoryWithLatestOperationDTO doUpdate(Long steamId) {
        Inventory inventory = findInventory(steamId);
        Operation prevOperation = operationService.getLatest(inventory);

        if (prevOperation == null) {
            throw new InventoryException("Cannot find Inventory Operation resource");
        }

        UserInventoryResponseDTO inventoryResponseDTO = steamClient.fetchUserInventory(steamId);
        List<ItemDTO> responseItems = inventoryResponseDTO.getItems();

        // unique version - fails on concurrent update
        Operation currentOperation;
        try {
            currentOperation = operationService.create(inventory, Operation.Type.U, prevOperation);
        } catch (DuplicateKeyException duplicateKeyException) {
            throw new InventoryUpdateConflictException();
        }

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
        // block delete during sync of the same inventory
        if (!inventorySyncLock.tryLock(steamId)) {
            throw new InventoryUpdateConflictException();
        }

        try {
            doDelete(steamId);
        } finally {
            inventorySyncLock.unlock(steamId);
        }
    }

    private void doDelete(Long steamId) {
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

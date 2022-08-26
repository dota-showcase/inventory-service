package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.mapper.InventoryItemMapper;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryChangesDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import com.dotashowcase.inventoryservice.service.result.mapper.InventoryItemServiceResultMapper;
import com.dotashowcase.inventoryservice.service.result.mapper.PageMapper;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.support.HistoryRangeCriteria;
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

    private final InventoryItemServiceResultMapper inventoryItemServiceResultMapper;

    private final HistoryActionService historyActionService;

    private final PageMapper<InventoryItem, InventoryItemDTO> pageMapper;

    @Autowired
    public InventoryItemServiceImpl(
            InventoryItemDALRepository inventoryItemRepository,
            HistoryActionService historyActionService,
            PageMapper<InventoryItem, InventoryItemDTO> pageMapper
    ) {
        Assert.notNull(inventoryItemRepository, "InventoryItemDALRepository must not be null!");
        this.inventoryItemRepository = inventoryItemRepository;

        Assert.notNull(historyActionService, "HistoryActionService must not be null!");
        this.historyActionService = historyActionService;

        Assert.notNull(pageMapper, "PageMapper<InventoryItem, InventoryItemDTO> must not be null!");
        this.pageMapper = pageMapper;

        this.inventoryItemMapper = new InventoryItemMapper();

        this.inventoryItemServiceResultMapper = new InventoryItemServiceResultMapper();
    }

    @Override
    public PageResult<InventoryItemDTO> get(Inventory inventory, Pageable pageable) {
        Page<InventoryItem> inventoryItems = inventoryItemRepository.findAll(inventory, pageable);

        return pageMapper.getPageResult(inventoryItems, inventoryItemServiceResultMapper::getInventoryItemDTO);
    }

    @Override
    public InventoryChangesDTO getChanges(Inventory inventory, Integer version) {
        List<HistoryAction> actions = historyActionService.getByVersions(inventory, new HistoryRangeCriteria(version));

        Map<Long, InventoryItem> items1 = inventoryItemRepository.findAll(inventory, actions.get(0));
        Map<Long, InventoryItem> items2 = inventoryItemRepository.findAll(inventory, actions.get(1));

        // TODO: find changes logic

        return new InventoryChangesDTO();
    }

    @Override
    public List<InventoryItem> create(
            Inventory inventory, HistoryAction currentHistoryAction, List<ItemDTO> responseItems
    ) {
        List<InventoryItem> inventoryItems = inventoryItemMapper.itemDtoToInventoryItem(responseItems);

        for (InventoryItem inventoryItem : inventoryItems) {
            inventoryItem.setHistoryActionId(currentHistoryAction.getId());
            inventoryItem.setSteamId(inventory.getSteamId());
        }

        return inventoryItemRepository.insertAll(inventoryItems);
    }

    // TODO: return inner class
    @Override
    public int sync(Inventory inventory, HistoryAction currentHistoryAction, List<ItemDTO> responseItems) {
        int operations = 0;
        List<InventoryItem> steamInventoryItems = inventoryItemMapper.itemDtoToInventoryItem(responseItems);
        Map<Long, InventoryItem> inventoryItemsById = inventoryItemRepository.findAll(inventory);

        Set<Long> itemIdsToRemove = new HashSet<>(inventoryItemsById.keySet());
        List<InventoryItem> itemsToCreate = new ArrayList<>();
        Set<ObjectId> itemIdsToUpdate = new HashSet<>();

        for (InventoryItem steamInventoryItem : steamInventoryItems) {
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

            for (InventoryItem steamInventoryItem : steamInventoryItems) {
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

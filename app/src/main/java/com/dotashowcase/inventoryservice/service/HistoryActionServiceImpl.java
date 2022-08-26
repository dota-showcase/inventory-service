package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.embedded.HistoryActionMeta;
import com.dotashowcase.inventoryservice.repository.HistoryActionRepository;
import com.dotashowcase.inventoryservice.service.exception.HistoryActionNotFoundException;
import com.dotashowcase.inventoryservice.support.HistoryRangeCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HistoryActionServiceImpl implements HistoryActionService  {

    private final HistoryActionRepository historyActionRepository;

    private static final Logger log = LoggerFactory.getLogger(HistoryActionServiceImpl.class);

    @Autowired
    public HistoryActionServiceImpl(HistoryActionRepository historyActionRepository) {
        Assert.notNull(historyActionRepository, "HistoryActionRepository must not be null!");
        this.historyActionRepository = historyActionRepository;
    }

    @Override
    public Map<Long, List<HistoryAction>> getAll(List<Long> inventoryIds) {
        List<HistoryAction> historyActions = historyActionRepository.findByInventories(inventoryIds);

        Map<Long, List<HistoryAction>> result = new HashMap<>();

        for (HistoryAction historyAction : historyActions) {
            Long steamId = historyAction.getSteamId();

            if (result.containsKey(steamId)) {
                result.get(steamId).add(historyAction);
            } else {
                result.put(steamId, new ArrayList<>(List.of(historyAction)));
            }
        }

        return result;
    }

    @Override
    public HistoryAction getLatest(Inventory inventory) {
       return historyActionRepository.findLatest(inventory);
    }

    @Override
    public List<HistoryAction> getByVersions(Inventory inventory, HistoryRangeCriteria historyRangeCriteria) {
        Integer prevVersion = historyRangeCriteria.getPrevVersion();
        Integer version = historyRangeCriteria.getVersion();

        // by default try to find two latest
        if (prevVersion == null || version == null) {
            List<HistoryAction> result = historyActionRepository.findLatest(inventory,2);

            if (result.size() < 2) {
                throw new HistoryActionNotFoundException(inventory.getSteamId()); // TODO: create another exception
            }

            return result;
        }

        List<HistoryAction> result = new ArrayList<>(2);

        HistoryAction action = historyActionRepository.findByVersion(inventory, version);

        if (action == null) {
            throw new HistoryActionNotFoundException(inventory.getSteamId(), version);
        }

        HistoryAction prevAction = historyActionRepository.findByVersion(inventory, prevVersion);

        if (prevAction == null) {
            throw new HistoryActionNotFoundException(inventory.getSteamId(), prevVersion);
        }

        result.add(0, prevAction);
        result.add(1, action);

        return result;
    }

    @Override
    public HistoryAction create(
            Inventory inventory,
            HistoryAction.Type type,
            HistoryAction prevHistoryAction
    ) {
        HistoryAction historyAction = new HistoryAction();

        historyAction.setSteamId(inventory.getSteamId());

        if (type != null) {
            historyAction.setType(type);
        }

        if (prevHistoryAction != null) {
            historyAction.setVersion(prevHistoryAction.getVersion() + 1);
        }

        return historyActionRepository.insertOne(historyAction);
    }

    @Override
    public void createAndSaveMeta(HistoryAction historyAction, Integer count, Integer operations, Integer numSlots) {
        HistoryActionMeta meta = new HistoryActionMeta();

        meta.setResponseCount(count);
        meta.setOperations(operations);
        meta.setNumSlots(numSlots);

        if (historyActionRepository.updateMeta(historyAction, meta) == 0) {
            log.warn("Failed to store HistoryActionMeta {} for Action _id - {}", meta, historyAction.getId());
        }

        historyAction.setMeta(meta);
    }

    @Override
    public long delete(Inventory inventory) {
        return historyActionRepository.removeAll(inventory);
    }
}

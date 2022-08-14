package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.embedded.HistoryActionMeta;
import com.dotashowcase.inventoryservice.repository.HistoryActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class HistoryActionServiceImpl implements HistoryActionService  {

    private final HistoryActionRepository historyActionRepository;

    @Autowired
    public HistoryActionServiceImpl(HistoryActionRepository historyActionRepository) {
        Assert.notNull(historyActionRepository, "HistoryActionRepository must not be null!");
        this.historyActionRepository = historyActionRepository;
    }

    @Override
    public HistoryAction getLatest(Inventory inventory) {
       return historyActionRepository.findLatest(inventory);
    }

    @Override
    public HistoryAction create(
            Inventory inventory,
            HistoryAction.Type type,
            HistoryAction prevHistoryAction
//            Integer count,
//            Integer expectedCount,
//            Integer numSlots
    ) {
        HistoryAction historyAction = new HistoryAction();

        historyAction.setInventory(inventory);

        if (type != null) {
            historyAction.setType(type);
        }

        if (prevHistoryAction != null) {
            historyAction.setVersion(prevHistoryAction.getVersion() + 1);
        }

//        historyAction.setCount(count);
//        historyAction.setExpectedCount(expectedCount);
//        historyAction.setNumSlots(numSlots);

        return historyActionRepository.insertOne(historyAction);
    }

    @Override
    public long createAndSaveMeta(HistoryAction historyAction, Integer count, Integer operations, Integer numSlots) {
        HistoryActionMeta meta = new HistoryActionMeta();

        meta.setResponseCount(count);
        meta.setOperations(operations);
        meta.setNumSlots(numSlots);

        return historyActionRepository.updateMeta(historyAction, meta);
    }

    @Override
    public long delete(Inventory inventory) {
        return historyActionRepository.removeAll(inventory);
    }
}

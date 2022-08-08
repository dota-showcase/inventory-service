package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.constant.HistoryActionType;
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

//    @Override
//    public HistoryAction get(Inventory inventory) {
//       return historyActionRepository.findOne(inventory.);
//    }

    // todo create
    @Override
    public HistoryAction create(
            Inventory inventory,
            HistoryActionType type,
            Integer count,
            Integer expectedCount,
            Integer numSlots
    ) {
        HistoryAction historyAction = new HistoryAction();

        if (type != null) {
            historyAction.setType(type);
        }

        historyAction.setInventory(inventory);
        historyAction.setCount(count);
        historyAction.setExpectedCount(expectedCount);
        historyAction.setNumSlots(numSlots);

        return historyActionRepository.save(historyAction);
    }
}

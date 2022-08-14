package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;

public interface HistoryActionService {

    HistoryAction getLatest(Inventory inventory);

    HistoryAction create(
            Inventory inventory,
            HistoryAction.Type type,
            HistoryAction prevHistoryAction
//            Integer count,
//            Integer expectedCount,
//            Integer numSlots
    );

    long createAndSaveMeta(HistoryAction historyAction, Integer count, Integer operations, Integer numSlots);

    long delete(Inventory inventory);
}

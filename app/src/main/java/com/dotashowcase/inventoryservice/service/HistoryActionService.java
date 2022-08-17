package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.embedded.HistoryActionMeta;

import java.util.List;
import java.util.Map;

public interface HistoryActionService {

    Map<Long, List<HistoryAction>> getAll(List<Long> inventoryIds);

    HistoryAction getLatest(Inventory inventory);

    HistoryAction create(
            Inventory inventory,
            HistoryAction.Type type,
            HistoryAction prevHistoryAction
    );

    void createAndSaveMeta(HistoryAction historyAction, Integer count, Integer operations, Integer numSlots);

    long delete(Inventory inventory);
}

package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.embedded.HistoryActionMeta;

import java.util.List;

public interface HistoryActionDAL {

    List<HistoryAction> findByInventories(List<Long> inventoryIds);

    HistoryAction findLatest(Inventory inventory);

    List<HistoryAction> findLatest(Inventory inventory, int limit);

    HistoryAction findByVersion(Inventory inventory, int version);

    HistoryAction insertOne(HistoryAction historyAction);

    long updateMeta(HistoryAction historyAction, HistoryActionMeta meta);

    long removeAll(Inventory inventory);
}

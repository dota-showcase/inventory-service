package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.embedded.HistoryActionMeta;

public interface HistoryActionDAL {

   HistoryAction findLatest(Inventory inventory);

   HistoryAction insertOne(HistoryAction historyAction);

    long updateMeta(HistoryAction historyAction, HistoryActionMeta meta);

    long removeAll(Inventory inventory);
}

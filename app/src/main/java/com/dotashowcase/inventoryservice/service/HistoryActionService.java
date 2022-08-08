package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.constant.HistoryActionType;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HistoryActionService {

//    public HistoryAction get(Inventory inventory);

    public HistoryAction create(
            Inventory inventory,
            HistoryActionType historyActionType,
            Integer count,
            Integer expectedCount,
            Integer numSlots
    );
}

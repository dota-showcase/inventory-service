package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.InventoryMeta;

import java.util.List;

public interface InventoryService {

//    public List<InventoryItem> getAllByDefindex(Integer defindex);

    public InventoryMeta create(Long steamId);
}

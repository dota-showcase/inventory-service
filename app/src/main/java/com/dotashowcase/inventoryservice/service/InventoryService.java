package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;

import java.util.List;

public interface InventoryService {

    List<Inventory> getAll(String sortBy);

    Inventory get(Long steamId);

    Inventory create(Long steamId);

    Inventory update(Long steamId);

    void delete(Long steamId);
}

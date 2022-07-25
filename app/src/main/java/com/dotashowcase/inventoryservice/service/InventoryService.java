package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;

import java.util.List;

public interface InventoryService {

    public List<Inventory> getAll(String sortBy);

    public Inventory get(Long steamId);

    public Inventory create(Long steamId);

    public Inventory update(Long steamId);

    public void delete(Long steamId);
}

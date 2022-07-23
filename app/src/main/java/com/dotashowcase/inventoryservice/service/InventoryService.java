package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.InventoryMeta;

import java.util.List;

public interface InventoryService {

    public List<InventoryMeta> getAll(String sortBy);

    public InventoryMeta create(Long steamId);
}

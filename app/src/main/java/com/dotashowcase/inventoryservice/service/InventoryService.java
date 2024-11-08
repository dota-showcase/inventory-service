package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestOperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithOperationsDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {

    Inventory findInventory(Long steamId);

    Inventory findInventoryWithLatestOperation(Long steamId);

    List<InventoryWithOperationsDTO> getAll(String sortBy);

    PageResult<InventoryWithLatestOperationDTO> getPage(Pageable pageable, String sortBy);

    InventoryWithOperationsDTO get(Long steamId);

    InventoryWithLatestOperationDTO create(Long steamId);

    InventoryWithLatestOperationDTO update(Long steamId);

    void delete(Long steamId);
}

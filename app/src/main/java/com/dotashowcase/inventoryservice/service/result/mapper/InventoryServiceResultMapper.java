package com.dotashowcase.inventoryservice.service.result.mapper;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.service.result.dto.*;

public class InventoryServiceResultMapper {

    private final OperationServiceResultMapper operationServiceResultMapper;

    public InventoryServiceResultMapper() {
        this.operationServiceResultMapper = new OperationServiceResultMapper();
    }

    public InventoryDTO getInventoryDTO(Inventory inventory) {
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setSteamId(inventory.getSteamId());

        return inventoryDTO;
    }

    public InventoryWithLatestOperationDTO getInventoryWithLatestOperationDTO(
            Inventory inventory,
            Operation operation
    ) {
        InventoryWithLatestOperationDTO inventoryDTO = new InventoryWithLatestOperationDTO();
        Long steamId = inventory.getSteamId();
        inventoryDTO.setSteamId(steamId);
        inventoryDTO.setOperation(this.operationServiceResultMapper.getOperationDTO(operation));

        return inventoryDTO;
    }
}

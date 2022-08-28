package com.dotashowcase.inventoryservice.service.result.mapper;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithOperationsDTO;
import com.dotashowcase.inventoryservice.service.result.dto.OperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestOperationDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryServiceResultMapper {

    public List<InventoryWithOperationsDTO> getInventoriesWithOperationsDTO(
            List<Inventory> inventories,
            Map<Long, List<Operation>> operations
    ) {
        List<InventoryWithOperationsDTO> result = new ArrayList<>(inventories.size());

        for (Inventory inventory : inventories) {
            result.add(getInventoryWithOperationsDTO(
                    inventory,
                    operations.get(inventory.getSteamId())
            ));
        }

        return result;
    }

    public InventoryWithOperationsDTO getInventoryWithOperationsDTO(Inventory inventory, List<Operation> operations) {
        InventoryWithOperationsDTO inventoryDTO = new InventoryWithOperationsDTO();
        Long steamId = inventory.getSteamId();
        inventoryDTO.setSteamId(steamId);

        if (operations != null) {
            List<OperationDTO> operationDTOS = new ArrayList<>(operations.size());

            for (Operation operation : operations) {
                operationDTOS.add(getOperationDTO(operation));
            }

            inventoryDTO.setOperations(operationDTOS);
        }

        return inventoryDTO;
    }

    public InventoryWithLatestOperationDTO getInventoryWithLatestOperationDTO(
            Inventory inventory,
            Operation operation
    ) {
        InventoryWithLatestOperationDTO inventoryDTO = new InventoryWithLatestOperationDTO();
        Long steamId = inventory.getSteamId();
        inventoryDTO.setSteamId(steamId);
        inventoryDTO.setOperation(getOperationDTO(operation));

        return inventoryDTO;
    }

    public OperationDTO getOperationDTO(Operation operation) {
        OperationDTO operationDTO = new OperationDTO();
        operationDTO.setVersion(operation.getVersion());
        operationDTO.setType(operation.getType());

        OperationMeta meta = operation.getMeta();
        operationDTO.setOperations(meta.getOperations());
        operationDTO.setResponseCount(meta.getResponseCount());
        operationDTO.setNumSlots(meta.getNumSlots());

        operationDTO.setCreatedAt(operation.getCreatedAt());

        return operationDTO;
    }
}

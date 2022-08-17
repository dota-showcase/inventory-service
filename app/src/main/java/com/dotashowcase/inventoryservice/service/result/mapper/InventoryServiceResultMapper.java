package com.dotashowcase.inventoryservice.service.result.mapper;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.embedded.HistoryActionMeta;
import com.dotashowcase.inventoryservice.service.result.dto.HistoryActionDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithHistoriesDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestHistoryDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryServiceResultMapper {

    public List<InventoryWithHistoriesDTO> getInventoriesWithHistoriesDTO(
            List<Inventory> inventories,
            Map<Long, List<HistoryAction>> historyActions
    ) {
        List<InventoryWithHistoriesDTO> result = new ArrayList<>(inventories.size());

        for (Inventory inventory : inventories) {
            result.add(getInventoryWithHistoriesDTO(
                    inventory,
                    historyActions.get(inventory.getSteamId())
            ));
        }

        return result;
    }

    public InventoryWithHistoriesDTO getInventoryWithHistoriesDTO(
            Inventory inventory,
            List<HistoryAction> historyActions
    ) {
        InventoryWithHistoriesDTO inventoryDTO = new InventoryWithHistoriesDTO();
        Long steamId = inventory.getSteamId();
        inventoryDTO.setSteamId(steamId);

        if (historyActions != null) {
            List<HistoryActionDTO> historyActionDTOs = new ArrayList<>(historyActions.size());

            for (HistoryAction historyAction : historyActions) {
                historyActionDTOs.add(getHistoryActionDTO(historyAction));
            }

            inventoryDTO.setActions(historyActionDTOs);
        }

        return inventoryDTO;
    }

    public InventoryWithLatestHistoryDTO getInventoryWithLatestHistoryDTO(
            Inventory inventory,
            HistoryAction historyAction
    ) {
        InventoryWithLatestHistoryDTO inventoryDTO = new InventoryWithLatestHistoryDTO();
        Long steamId = inventory.getSteamId();
        inventoryDTO.setSteamId(steamId);
        inventoryDTO.setAction(getHistoryActionDTO(historyAction));

        return inventoryDTO;
    }

    public HistoryActionDTO getHistoryActionDTO(HistoryAction historyAction) {
        HistoryActionDTO historyActionDTO = new HistoryActionDTO();
        historyActionDTO.setVersion(historyAction.getVersion());
        historyActionDTO.setType(historyAction.getType());

        HistoryActionMeta historyActionMeta = historyAction.getMeta();
        historyActionDTO.setOperations(historyActionMeta.getOperations());
        historyActionDTO.setResponseCount(historyActionMeta.getResponseCount());
        historyActionDTO.setNumSlots(historyActionMeta.getNumSlots());

        historyActionDTO.setCreatedAt(historyAction.getCreatedAt());

        return historyActionDTO;
    }
}

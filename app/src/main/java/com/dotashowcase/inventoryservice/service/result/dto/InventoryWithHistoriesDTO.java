package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryWithHistoriesDTO {

    private Long steamId;

    private List<HistoryActionDTO> actions;
}

package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.Data;

@Data
public class InventoryWithLatestHistoryDTO {

    private Long steamId;

    private HistoryActionDTO action;
}

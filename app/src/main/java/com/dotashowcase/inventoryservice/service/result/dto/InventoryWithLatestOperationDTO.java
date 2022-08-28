package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.Data;

@Data
public class InventoryWithLatestOperationDTO {

    private Long steamId;

    private OperationDTO operation;
}

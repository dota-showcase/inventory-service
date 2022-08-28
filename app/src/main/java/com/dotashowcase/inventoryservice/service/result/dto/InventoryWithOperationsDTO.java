package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryWithOperationsDTO {

    private Long steamId;

    private List<OperationDTO> operations;
}

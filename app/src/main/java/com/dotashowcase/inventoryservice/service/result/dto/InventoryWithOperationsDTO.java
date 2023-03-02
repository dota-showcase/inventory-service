package com.dotashowcase.inventoryservice.service.result.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(title="InventoryWithOperations")
@Data
public class InventoryWithOperationsDTO {

    private Long steamId;

    private List<OperationDTO> operations;
}

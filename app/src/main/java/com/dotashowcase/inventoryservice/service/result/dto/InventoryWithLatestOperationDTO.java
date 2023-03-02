package com.dotashowcase.inventoryservice.service.result.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title="InventoryWithLatestOperation")
@Data
public class InventoryWithLatestOperationDTO {

    private Long steamId;

    private OperationDTO operation;
}

package com.dotashowcase.inventoryservice.service.result.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title="Inventory")
@Data
public class InventoryDTO {

    private Long steamId;
}

package com.dotashowcase.inventoryservice.service.result.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(title="InventoryChanges")
@Data
public class InventoryChangesDTO {

    private List<InventoryItemDTO> create;

    private List<InventoryItemDTO> update;

    private List<InventoryItemDTO> delete;

    public InventoryChangesDTO() {
        this.create = new ArrayList<>();
        this.update = new ArrayList<>();
        this.delete = new ArrayList<>();
    }
}

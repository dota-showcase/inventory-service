package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryChangesDTO {

    List<InventoryItemDTO> create;

    List<InventoryItemDTO> update;

    List<InventoryItemDTO> delete;
}

package com.dotashowcase.inventoryservice.service.result.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(title="InventoryItem")
@Data
public class InventoryItemDTO {

    private Long itemId;

    private Long originalId;

    private Integer defIndex;

    private Byte level;

    private Byte quality;

    private Long inventoryToken;

    private Integer position;

    private Integer uses;

    private Boolean isTradable;

    private Boolean isCraftable;

    private Byte style;

    private String customName;

    private String customDesc;

    private List<ItemEquipmentDTO> equipments;

    private List<ItemAttributeDTO> attributes;
}

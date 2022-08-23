package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryItemDTO {

    private Long itemId;

    private Long originalId;

    private Integer defIndex;

    private Byte level;

    private Byte quality;

    private Long inventoryToken;

    private Integer quantity;

    private Boolean isTradable;

    private Boolean isCraftable;

    private Byte style;

    private String customName;

    private String customDesc;

    private List<ItemEquipmentDTO> equipments;

    private List<ItemAttributeDTO> attributes;
}

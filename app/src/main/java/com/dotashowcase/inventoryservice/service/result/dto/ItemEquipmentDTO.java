package com.dotashowcase.inventoryservice.service.result.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ItemEquipmentDTO {

    @JsonProperty("class")
    private Integer equipClass;

    private Integer slot;
}

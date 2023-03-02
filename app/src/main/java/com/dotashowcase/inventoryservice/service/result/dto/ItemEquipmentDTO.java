package com.dotashowcase.inventoryservice.service.result.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title="ItemEquipment")
@Data
public class ItemEquipmentDTO {

    @JsonProperty("class")
    private Integer equipClass;

    private Integer slot;
}

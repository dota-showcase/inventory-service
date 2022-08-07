package com.dotashowcase.inventoryservice.steamclient.response.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ItemEquipDTO {

    @JsonProperty("class")
    private Integer equip_class;

    private Integer slot;
}

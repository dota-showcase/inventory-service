package com.dotashowcase.inventoryservice.steamclient.response.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ItemEquipDTO {

    @JsonProperty("class")
    public int equip_class;

    public int slot;
}

package com.dotashowcase.inventoryservice.steamclient.response.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UserInventoryResponseDTO {

    @JsonProperty("num_backpack_slots")
    private Integer numberBackpackSlots;

    private Integer status;

    private List<ItemDTO> items;

    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }
}

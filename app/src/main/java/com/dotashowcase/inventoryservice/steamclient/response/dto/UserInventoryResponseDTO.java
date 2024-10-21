package com.dotashowcase.inventoryservice.steamclient.response.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInventoryResponseDTO {

    private Integer numberBackpackSlots;        // num_backpack_slots

    private Integer status;

    private List<ItemDTO> items;

    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }
}

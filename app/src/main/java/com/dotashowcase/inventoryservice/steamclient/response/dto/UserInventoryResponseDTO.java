package com.dotashowcase.inventoryservice.steamclient.response.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInventoryResponseDTO {

    private int numberBackpackSlots;        // num_backpack_slots

    private int status;

    List<ItemDTO> items;

    public boolean hasItems() {
        return items.size() > 0;
    }
}

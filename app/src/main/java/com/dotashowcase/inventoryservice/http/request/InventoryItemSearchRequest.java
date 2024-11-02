package com.dotashowcase.inventoryservice.http.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemSearchRequest {

    private List<Integer> defIndexes;

    private List<Byte> qualities;

    private Boolean isTradable;

    private Boolean isCraftable;

    private Boolean isEquipped;

    private Boolean hasAttribute;
}

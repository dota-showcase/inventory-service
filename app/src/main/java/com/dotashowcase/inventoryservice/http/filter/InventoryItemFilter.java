package com.dotashowcase.inventoryservice.http.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InventoryItemFilter {

    private List<Integer> defIndexes;

    private List<Byte> qualities;

    private Boolean isTradable;

    private Boolean isCraftable;

    private Boolean isEquipped;

    private Boolean hasAttribute;

    public boolean hasDefIndexes() {
        return defIndexes != null && !defIndexes.isEmpty();
    }

    public boolean hasQualities() {
        return qualities != null && !qualities.isEmpty();
    }
}

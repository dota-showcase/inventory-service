package com.dotashowcase.inventoryservice.http.request;

import com.dotashowcase.inventoryservice.http.filter.InventoryItemFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemSearchRequest {

    private List<Long> itemIds;

    private List<Integer> defIndexes;

    private List<Byte> qualities;

    private Boolean isTradable;

    private Boolean isCraftable;

    private Boolean isEquipped;

    private Boolean hasAttribute;

    public InventoryItemFilter toFilter() {
        return InventoryItemFilter.builder()
                .itemIds(itemIds)
                .defIndexes(defIndexes)
                .qualities(qualities)
                .isTradable(isTradable)
                .isCraftable(isCraftable)
                .isEquipped(isEquipped)
                .hasAttribute(hasAttribute)
                .build();
    }
}

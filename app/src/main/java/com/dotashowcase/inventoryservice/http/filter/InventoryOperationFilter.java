package com.dotashowcase.inventoryservice.http.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InventoryOperationFilter {

    private Boolean hasChanges;
}

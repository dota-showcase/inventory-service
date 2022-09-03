package com.dotashowcase.inventoryservice.http.request;

import com.dotashowcase.inventoryservice.support.validator.SteamIdConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCreateRequest {

    @SteamIdConstraint
    private Long steamId;
}

package com.dotashowcase.inventoryservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@AllArgsConstructor
@Data
@Document("inventory_metas")
public class InventoryMeta {

    @Id
    private Long steamId;

    @Field
    private Integer count = 0;

    @Field
    private Integer expCount = 0;

    public InventoryMeta(Long steamId) {
        this.steamId = steamId;
    }
}

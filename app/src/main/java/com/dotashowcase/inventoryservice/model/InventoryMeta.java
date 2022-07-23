package com.dotashowcase.inventoryservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document("inventory_metas")
public class InventoryMeta {

    @Id
    private Long steamId;

    @Field
    private Integer count;          // stored item count

    @Field
    private Integer expCount;       // expected item count / Steam API

    @Field
    private Integer slots;          // num_backpack_slots / Steam API

    public InventoryMeta(Long steamId) {
        this.steamId = steamId;
    }
}

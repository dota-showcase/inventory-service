package com.dotashowcase.inventoryservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document("inventory_metas")
public class InventoryMeta {

    @Id
    private Long steamId;

    @Field
    private Integer count = 0;          // stored item count

    @Field
    private Integer expCount = 0;       // expected item count / Steam API

    @Field
    private Integer slots = 0;          // num_backpack_slots / Steam API

    @CreatedDate
    @Field
    private Date createdAt;

    @Version
    private Long v;                     // version, tracks document updates / Spring

    public InventoryMeta(Long steamId) {
        this.steamId = steamId;
    }
}

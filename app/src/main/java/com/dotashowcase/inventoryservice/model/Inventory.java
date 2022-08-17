package com.dotashowcase.inventoryservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Data
@Document("inventories")
public class Inventory {

    @Id
    private Long steamId;

    public Inventory(Long steamId) {
        this.steamId = steamId;
    }
}

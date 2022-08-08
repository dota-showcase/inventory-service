package com.dotashowcase.inventoryservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

/**
 * Inventory meta data.
 */
@NoArgsConstructor
@Data
@Document("inventories")
public class Inventory {

    @Id
    private Long steamId;

//    @ReadOnlyProperty
//    @DocumentReference(lazy = true, lookup = "{'history_actions': ?#{#self.steamId}}")
//    List<HistoryAction> history;

    public Inventory(Long steamId) {
        this.steamId = steamId;
    }
}

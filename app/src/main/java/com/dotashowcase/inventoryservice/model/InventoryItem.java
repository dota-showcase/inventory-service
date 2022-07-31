package com.dotashowcase.inventoryservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document("inventory_items")
public class InventoryItem {

    @Id
    private Long id;

    private Long originalId;
    private Integer defindex;
    private Byte level;
    private Byte quality;
    private Integer quantity;
}

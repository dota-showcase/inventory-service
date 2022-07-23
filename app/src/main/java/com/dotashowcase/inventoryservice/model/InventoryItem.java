package com.dotashowcase.inventoryservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ```
 * //  {
 * //      "id": 698030079,
 * //      "original_id": 698030079,
 * //      "defindex": 5039,
 * //      "level": 1,
 * //      "quality": 4,
 * //      "inventory": 2147483982,
 * //      "quantity": 1,
 * //      "flag_cannot_trade": true,
 * //      "flag_cannot_craft": true,
 * //      "equipped": [                                       // optional
 * //          {
 * //              "class": 12,
 * //              "slot": 3
 * //          }
 * //      ],
 * //      "attributes": [                                     // optional
 * //          {
 * //              "defindex": 153,
 * //              "value": 1065353216,
 * //              "float_value": 1
 * //          },
 * //          {
 * //              "defindex": 415,
 * //              "value": 1,
 * //              "float_value": 1.40129846432481707e-45
 * //          }
 * //      ]
 * //  },
 * ```
 */

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

package com.dotashowcase.inventoryservice.steamclient.response.dto;

import lombok.Data;

import java.util.List;

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

@Data
public class ItemDTO {

    private Long id;

    private Long original_id;

    private Integer defindex;

    private Byte level;

    private Byte quality;

    private Long inventory;

    private Integer quantity;

    private Boolean flag_cannot_trade;

    private Boolean flag_cannot_craft;

    private Byte style;                         // optional

    private String custom_name;                 // optional

    private String custom_desc;                 // optional

    private List<ItemAttributeDTO> attributes;  // optional

    private List<ItemEquipDTO> equipped;        // optional
}

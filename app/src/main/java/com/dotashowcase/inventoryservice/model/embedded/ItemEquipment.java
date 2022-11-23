package com.dotashowcase.inventoryservice.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ItemEquipment {

    @Field("type")
    private Integer equipClass;

    private Integer slot;
}

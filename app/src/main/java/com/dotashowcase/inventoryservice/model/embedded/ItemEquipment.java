package com.dotashowcase.inventoryservice.model.embedded;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class ItemEquipment {

    @Field("type")
    private Integer equipClass;

    private Integer slot;
}

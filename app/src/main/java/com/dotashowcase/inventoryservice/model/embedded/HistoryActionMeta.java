package com.dotashowcase.inventoryservice.model.embedded;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class HistoryActionMeta {

    @Field
    private Integer operations = 0;          // stored item count / change operations

    @Field
    private Integer responseCount = 0;       // expected item count / Steam API

    @Field
    private Integer numSlots = 0;            // num_backpack_slots / Steam API
}

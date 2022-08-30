package com.dotashowcase.inventoryservice.model.embedded;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class OperationMeta {

    @Field("cCount")
    private Integer createOperationCount = 0;

    @Field("uCount")
    private Integer updateOperationCount = 0;

    @Field("dCount")
    private Integer deleteOperationCount = 0;

    @Field("respCount")
    private Integer responseCount = 0;          // expected item count / Steam API

    @Field
    private Integer numSlots = 0;               // num_backpack_slots / Steam API
}

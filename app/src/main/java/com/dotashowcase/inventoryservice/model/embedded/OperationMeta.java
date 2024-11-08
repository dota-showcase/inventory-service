package com.dotashowcase.inventoryservice.model.embedded;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@ToString
public class OperationMeta {

    @Field("iCount")
    private Integer itemCount = 0;

    @Field("respCount")
    private Integer responseCount = 0;          // expected item count / Steam API

    @Field("cCount")
    private Integer createOperationCount = 0;

    @Field("uCount")
    private Integer updateOperationCount = 0;

    @Field("dCount")
    private Integer deleteOperationCount = 0;

    @Field
    private Integer numSlots = 0;               // num_backpack_slots / Steam API
}

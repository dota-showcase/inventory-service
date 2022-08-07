package com.dotashowcase.inventoryservice.model.embedded;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class ItemAttribute {

    @Field("dIdx")
    private Integer defIndex;

    private String value;

    @Field("fVal")
    private Double floatValue;

    @Field("aInfo")
    private AccountInfo accountInfo;
}

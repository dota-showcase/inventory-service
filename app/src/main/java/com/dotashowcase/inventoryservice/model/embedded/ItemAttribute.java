package com.dotashowcase.inventoryservice.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@NoArgsConstructor
@AllArgsConstructor
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

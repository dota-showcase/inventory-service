package com.dotashowcase.inventoryservice.model.embedded;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class AccountInfo {

    @Field("steamId")
    private Long steamId;

    @Field("pName")
    private String personalName;
}

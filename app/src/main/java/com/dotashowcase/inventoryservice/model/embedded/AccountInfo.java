package com.dotashowcase.inventoryservice.model.embedded;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class AccountInfo {

    @Field("steamId")
    private Long steamId;

    @Field("pName")
    @EqualsAndHashCode.Exclude          // it changes with username in steam
    private String personalName;
}

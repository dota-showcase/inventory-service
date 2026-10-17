package com.dotashowcase.inventoryservice.model.embedded;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

@Getter
@Setter
@ToString
public class AccountInfo {

    @Field("steamId")
    private Long steamId;

    @Field("pName")
    private String personalName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountInfo that = (AccountInfo) o;
        return Objects.equals(steamId, that.steamId);
        // skip - it changes with username in steam
//         && Objects.equals(personalName, that.personalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                steamId
                // skip - it changes with username in steam
//                 personalName
        );
    }
}

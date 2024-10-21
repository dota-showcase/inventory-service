package com.dotashowcase.inventoryservice.model.embedded;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

@Data
public class AccountInfo {

    @Field("steamId")
    private Long steamId;

    @Field("pName")
    @EqualsAndHashCode.Exclude          // it changes with username in steam
    private String personalName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountInfo that = (AccountInfo) o;
        return Objects.equals(steamId, that.steamId) &&
                Objects.equals(personalName, that.personalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(steamId, personalName);
    }
}

package com.dotashowcase.inventoryservice.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ItemAttribute {

    @Field("dIdx")
    private Integer defIndex;

    private String value;

    @Field("fVal")
    private Double floatValue;

    @Field("aInfo")
    private AccountInfo accountInfo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemAttribute that = (ItemAttribute) o;
        return Objects.equals(defIndex, that.defIndex) &&
                Objects.equals(value, that.value) &&
                Objects.equals(floatValue, that.floatValue) &&
                Objects.equals(accountInfo, that.accountInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defIndex, value, floatValue, accountInfo);
    }
}

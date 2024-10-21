package com.dotashowcase.inventoryservice.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ItemEquipment {

    @Field("type")
    private Integer equipClass;

    private Integer slot;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemEquipment that = (ItemEquipment) o;
        return Objects.equals(equipClass, that.equipClass) &&
                Objects.equals(slot, that.slot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipClass, slot);
    }
}

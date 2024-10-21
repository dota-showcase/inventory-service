package com.dotashowcase.inventoryservice.model;

import com.dotashowcase.inventoryservice.model.embedded.ItemAttribute;
import com.dotashowcase.inventoryservice.model.embedded.ItemEquipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Data
@CompoundIndex(def = "{'steamId':1, 'isActive':1, 'defIndex':1}", name = "items__search_index")
@Document("items")
public class InventoryItem {

    @Id
    private ObjectId id;

    private Long itemId;

    @Field("_oId")
    private ObjectId operationId;

    @Field("_odId")
    private ObjectId deleteOperationId = null;

    @Field("_oT")
    private Operation.Type operationType = Operation.Type.C;

    @Field("_isA")
    private Boolean isActive = true;

    @Field("steamId")
    private Long steamId;

    @Field("orgId")
    private Long originalId;

    @Field("dIdx")
    private Integer defIndex;

    @Field("lvl")
    private Byte level;

    @Field("qlt")
    private Byte quality;

    @Field("inv")
    private Long inventoryToken;

    @Field("pos")
    private Integer inventoryPosition;

    @Field("qnt")
    private Integer quantity;

    @Field("isTr")
    private Boolean isTradable;

    @Field("isCr")
    private Boolean isCraftable;

    private Byte style;

    @Field("cName")
    private String customName;

    @Field("cDesc")
    private String customDesc;

    @Field("equips")
    private List<ItemEquipment> itemEquipment;

    @Field("attrs")
    private List<ItemAttribute> attributes;

    public static List<String> fillable = new ArrayList<>();

    static {
        fillable.add("_isA");
        fillable.add("_odId");
        fillable.add("isTr");
        fillable.add("isCr");
        fillable.add("qnt");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return Objects.equals(defIndex, that.defIndex) &&
                Objects.equals(level, that.level) &&
                Objects.equals(quality, that.quality) &&
                Objects.equals(inventoryToken, that.inventoryToken) &&
                Objects.equals(inventoryPosition, that.inventoryPosition) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(isTradable, that.isTradable) &&
                Objects.equals(isCraftable, that.isCraftable) &&
                Objects.equals(style, that.style) &&
                Objects.equals(customName, that.customName) &&
                Objects.equals(customDesc, that.customDesc) &&
                Objects.equals(itemEquipment, that.itemEquipment) &&
                Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defIndex, level, quality, inventoryToken, inventoryPosition, quantity,
                isTradable, isCraftable, style, customName, customDesc, itemEquipment, attributes);
    }
}

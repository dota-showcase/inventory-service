package com.dotashowcase.inventoryservice.model;

import com.dotashowcase.inventoryservice.model.embedded.ItemAttribute;
import com.dotashowcase.inventoryservice.model.embedded.ItemEquipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@CompoundIndex(def = "{'steamId':1, 'isActive':1, 'defIndex':1}", name = "items__search_index")
@Document("items")
public class InventoryItem {

    @Id
    @EqualsAndHashCode.Exclude
    private ObjectId id;

    @EqualsAndHashCode.Exclude
    private Long itemId;

    @Field("_oId")
    @EqualsAndHashCode.Exclude
    private ObjectId operationId;

    @Field("_odId")
    @EqualsAndHashCode.Exclude
    private ObjectId deleteOperationId = null;

    @Field("_oT")
    @EqualsAndHashCode.Exclude
    private Operation.Type operationType = Operation.Type.C;

    @Field("_isA")
    @EqualsAndHashCode.Exclude
    private Boolean isActive = true;

    @Field("steamId")
    @EqualsAndHashCode.Exclude
    private Long steamId;

    @Field("orgId")
    @EqualsAndHashCode.Exclude
    private Long originalId;

    @Field("dIdx")
    private Integer defIndex;

    @Field("lvl")
    private Byte level;

    @Field("qlt")
    private Byte quality;

    @Field("inv")
    private Long inventoryToken;

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
    }
}

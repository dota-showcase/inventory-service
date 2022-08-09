package com.dotashowcase.inventoryservice.model;

import com.dotashowcase.inventoryservice.model.embedded.ItemAttribute;
import com.dotashowcase.inventoryservice.model.embedded.ItemEquipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document("inventory_items")
public class InventoryItem {

    @Id
    @EqualsAndHashCode.Exclude
    private Long id;

    @Field("steamId")
    @EqualsAndHashCode.Exclude
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
}

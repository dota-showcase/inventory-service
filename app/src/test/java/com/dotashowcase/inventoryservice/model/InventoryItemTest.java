package com.dotashowcase.inventoryservice.model;

import com.dotashowcase.inventoryservice.model.embedded.ItemAttribute;
import com.dotashowcase.inventoryservice.model.embedded.ItemEquipment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryItemTest {

    @Test
    void itShouldBeEqualWhenEquipmentAndAttributesAreReordered() {
        // given
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemEquipment(List.of(new ItemEquipment(22, 0), new ItemEquipment(22, 8)));
        inventoryItem1.setAttributes(List.of(
                new ItemAttribute(185, "1441147439", 31633606443008.0, null),
                new ItemAttribute(2010, "2", 2.802596928649634e-45, null)
        ));

        InventoryItem inventoryItem2 = new InventoryItem();
        inventoryItem2.setItemEquipment(List.of(new ItemEquipment(22, 8), new ItemEquipment(22, 0)));
        inventoryItem2.setAttributes(List.of(
                new ItemAttribute(2010, "2", 2.802596928649634e-45, null),
                new ItemAttribute(185, "1441147439", 31633606443008.0, null)
        ));

        // then
        assertThat(inventoryItem1).isEqualTo(inventoryItem2);
        assertThat(inventoryItem1.hashCode()).isEqualTo(inventoryItem2.hashCode());
    }

    @Test
    void itShouldBeEqualWhenReorderedAttributesShareDefIndex() {
        // given - item 18878079022 from prod: equips rotated, two attributes with defIndex 2010
        List<ItemAttribute> attributes = List.of(
                new ItemAttribute(213, "0", 0.0, null),
                new ItemAttribute(153, "1065353216", 1.0, null),
                new ItemAttribute(2010, "2", 2.802596928649634e-45, null),
                new ItemAttribute(2010, "3", 4.203895392974451e-45, null)
        );

        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemEquipment(List.of(
                new ItemEquipment(42, 9), new ItemEquipment(42, 10), new ItemEquipment(42, 0)
        ));
        inventoryItem1.setAttributes(attributes);

        InventoryItem inventoryItem2 = new InventoryItem();
        inventoryItem2.setItemEquipment(List.of(
                new ItemEquipment(42, 0), new ItemEquipment(42, 9), new ItemEquipment(42, 10)
        ));
        inventoryItem2.setAttributes(attributes.reversed());

        // then
        assertThat(inventoryItem1).isEqualTo(inventoryItem2);
        assertThat(inventoryItem1.hashCode()).isEqualTo(inventoryItem2.hashCode());
    }

    @Test
    void itShouldNotBeEqualWhenEquipmentSlotChanges() {
        // given
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemEquipment(List.of(new ItemEquipment(22, 0), new ItemEquipment(22, 8)));

        InventoryItem inventoryItem2 = new InventoryItem();
        inventoryItem2.setItemEquipment(List.of(new ItemEquipment(22, 8), new ItemEquipment(22, 1)));

        // then
        assertThat(inventoryItem1).isNotEqualTo(inventoryItem2);
    }

    @Test
    void itShouldNotBeEqualWhenEquipmentDuplicatesDiffer() {
        // given
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemEquipment(List.of(
                new ItemEquipment(22, 0), new ItemEquipment(22, 0), new ItemEquipment(22, 8)
        ));

        InventoryItem inventoryItem2 = new InventoryItem();
        inventoryItem2.setItemEquipment(List.of(
                new ItemEquipment(22, 0), new ItemEquipment(22, 8), new ItemEquipment(22, 8)
        ));

        // then
        assertThat(inventoryItem1).isNotEqualTo(inventoryItem2);
    }
}

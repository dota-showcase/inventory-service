package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.ItemAttribute;
import com.dotashowcase.inventoryservice.model.embedded.ItemEquipment;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.mapper.PageMapper;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemAttributeDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemEquipDTO;
import com.dotashowcase.inventoryservice.support.SortBuilder;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Import(MongoTestConfig.class)
class InventoryItemSyncTest {

    @Mock
    private InventoryItemDALRepository inventoryItemRepository;

    @Mock
    private SortBuilder sortBuilder;

    @Mock
    private PageMapper<InventoryItem, InventoryItemDTO> pageMapper;

    private InventoryItemService underTest;

    @BeforeEach
    void setUp() {
        underTest = new InventoryItemServiceImpl(inventoryItemRepository, sortBuilder, pageMapper);
    }

    @Test
    void itShoudSyncWhenNoInventoryItems() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.U);
        operation1.setVersion(2);

        ItemDTO item1 = new ItemDTO();
        item1.setId(100L);
        item1.setOriginal_id(100L);
        item1.setDefindex(200);
        item1.setLevel((byte) 1);
        item1.setQuality((byte) 4);
        item1.setInventory(1000L);
        item1.setQuantity(1);
        item1.setFlag_cannot_craft(true);
        item1.setFlag_cannot_trade(true);
        item1.setStyle((byte) 1);
        item1.setCustom_name("Sample #1");
        ItemEquipDTO itemEquip11 = new ItemEquipDTO();
        itemEquip11.setEquip_class(12);
        itemEquip11.setSlot(3);
        item1.setEquipped(List.of(itemEquip11));
        ItemAttributeDTO itemAttribute11 = new ItemAttributeDTO();
        itemAttribute11.setDefindex(999);
        itemAttribute11.setValue("123");
        itemAttribute11.setFloat_value(1.0);
        item1.setAttributes(List.of(itemAttribute11));

        ItemDTO item2 = new ItemDTO();
        item2.setId(101L);
        item2.setOriginal_id(101L);
        item2.setDefindex(201);
        item2.setLevel((byte) 1);
        item2.setQuality((byte) 4);
        item2.setInventory(1000L);
        item2.setQuantity(1);
        item2.setFlag_cannot_craft(true);
        item2.setFlag_cannot_trade(true);
        item2.setStyle((byte) 1);
        item2.setCustom_name("Sample #2");
        ItemEquipDTO itemEquip21 = new ItemEquipDTO();
        itemEquip21.setEquip_class(12);
        itemEquip21.setSlot(3);
        item2.setEquipped(List.of(itemEquip21));
        ItemAttributeDTO itemAttribute21 = new ItemAttributeDTO();
        itemAttribute21.setDefindex(999);
        itemAttribute21.setValue("123");
        itemAttribute21.setFloat_value(1.0);
        item2.setAttributes(List.of(itemAttribute21));

        List<ItemDTO> items = List.of(item1, item2);

        when(inventoryItemRepository.findAll(inventory)).thenReturn(new ArrayList<InventoryItem>());

        // when
        underTest.sync(inventory, operation1, items);

        // then
        ArgumentCaptor<Set<ObjectId>> inventoryItemUpdateArgumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(inventoryItemRepository, times(1)).updateAll(
                inventoryItemUpdateArgumentCaptor.capture(),
                ArgumentMatchers.anyList()
        );

        assertThat(inventoryItemUpdateArgumentCaptor.getValue().size()).isEqualTo(0);

        ArgumentCaptor<List<InventoryItem>> inventoryItemsArgumentCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(inventoryItemRepository).insertAll(inventoryItemsArgumentCaptor.capture());

        assertThat(inventoryItemsArgumentCaptor.getValue().getFirst().getItemId()).isEqualTo(item1.getId());
        assertThat(inventoryItemsArgumentCaptor.getValue().getFirst().getSteamId()).isEqualTo(steamId);

        assertThat(inventoryItemsArgumentCaptor.getValue().get(1).getItemId()).isEqualTo(item2.getId());
        assertThat(inventoryItemsArgumentCaptor.getValue().get(1).getSteamId()).isEqualTo(steamId);
    }

    @Test
    void itShouldSyncSkipTheSameInventoryItems() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);

        Long itemId1 = 100L;
        Integer defIndex1 = 200;
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemId(itemId1);
        inventoryItem1.setOperationId(operation1.getId());
        inventoryItem1.setSteamId(steamId);
        inventoryItem1.setOriginalId(itemId1);
        inventoryItem1.setDefIndex(defIndex1);
        inventoryItem1.setLevel((byte) 1);
        inventoryItem1.setInventoryToken(1000L);
        inventoryItem1.setInventoryPosition(1000);
        inventoryItem1.setQuantity(1);
        inventoryItem1.setQuality((byte) 1);
        inventoryItem1.setStyle((byte) 1);
        inventoryItem1.setIsTradable(true);
        inventoryItem1.setIsCraftable(true);
        inventoryItem1.setCustomName("Sample #1");
        inventoryItem1.setItemEquipment(List.of(new ItemEquipment(1, 1)));
        inventoryItem1.setAttributes(List.of(new ItemAttribute(7, "1", 1.0, null)));

        Long itemId2 = 101L;
        Integer defIndex2 = 201;
        InventoryItem inventoryItem2 = new InventoryItem();
        inventoryItem2.setItemId(itemId2);
        inventoryItem2.setOperationId(operation1.getId());
        inventoryItem2.setSteamId(steamId);
        inventoryItem2.setOriginalId(itemId2);
        inventoryItem2.setDefIndex(defIndex2);
        inventoryItem2.setLevel((byte) 1);
        inventoryItem2.setInventoryToken(1000L);
        inventoryItem2.setInventoryPosition(1000);
        inventoryItem2.setQuantity(1);
        inventoryItem2.setQuality((byte) 1);
        inventoryItem2.setStyle((byte) 1);
        inventoryItem2.setIsTradable(true);
        inventoryItem2.setIsCraftable(true);
        inventoryItem2.setCustomName("Sample #2");
        inventoryItem2.setItemEquipment(List.of(new ItemEquipment(1, 2)));
        inventoryItem2.setAttributes(List.of(new ItemAttribute(8, "2", 1.0, null)));

        when(inventoryItemRepository.findAll(inventory)).thenReturn(List.of(inventoryItem1, inventoryItem2));

        Operation operation2 = new Operation();
        operation2.setSteamId(steamId);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(2);

        ItemDTO item1 = new ItemDTO();
        item1.setId(itemId1);
        item1.setOriginal_id(itemId1);
        item1.setDefindex(defIndex1);
        item1.setLevel((byte) 1);
        item1.setQuality((byte) 1);
        item1.setInventory(1000L);
        item1.setQuantity(1);
        item1.setFlag_cannot_craft(false);
        item1.setFlag_cannot_trade(false);
        item1.setStyle((byte) 1);
        item1.setCustom_name("Sample #1");
        ItemEquipDTO itemEquip11 = new ItemEquipDTO();
        itemEquip11.setEquip_class(1);
        itemEquip11.setSlot(1);
        item1.setEquipped(List.of(itemEquip11));
        ItemAttributeDTO itemAttribute11 = new ItemAttributeDTO();
        itemAttribute11.setDefindex(7);
        itemAttribute11.setValue("1");
        itemAttribute11.setFloat_value(1.0);
        item1.setAttributes(List.of(itemAttribute11));

        ItemDTO item2 = new ItemDTO();
        item2.setId(itemId2);
        item2.setOriginal_id(itemId2);
        item2.setDefindex(defIndex2);
        item2.setLevel((byte) 1);
        item2.setQuality((byte) 1);
        item2.setInventory(1000L);
        item2.setQuantity(1);
        item2.setFlag_cannot_craft(false);
        item2.setFlag_cannot_trade(false);
        item2.setStyle((byte) 1);
        item2.setCustom_name("Sample #2");
        ItemEquipDTO itemEquip21 = new ItemEquipDTO();
        itemEquip21.setEquip_class(1);
        itemEquip21.setSlot(2);
        item2.setEquipped(List.of(itemEquip21));
        ItemAttributeDTO itemAttribute21 = new ItemAttributeDTO();
        itemAttribute21.setDefindex(8);
        itemAttribute21.setValue("2");
        itemAttribute21.setFloat_value(1.0);
        item2.setAttributes(List.of(itemAttribute21));

        List<ItemDTO> items = List.of(item1, item2);

        // when
        underTest.sync(inventory, operation2, items);

        // then
        ArgumentCaptor<Set<ObjectId>> updateArgumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(inventoryItemRepository, times(1)).updateAll(
                updateArgumentCaptor.capture(),
                ArgumentMatchers.anyList()
        );
        assertThat(updateArgumentCaptor.getValue().size()).isEqualTo(0);

        ArgumentCaptor<List<InventoryItem>> insertArgumentCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(inventoryItemRepository).insertAll(insertArgumentCaptor.capture());
        assertThat(insertArgumentCaptor.getValue().size()).isEqualTo(0);
    }

    @Test
    void itShouldMarkInventoryItemsAsDeleted() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);

        Long itemId1 = 100L;
        Integer defIndex1 = 200;
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemId(itemId1);
        inventoryItem1.setOperationId(operation1.getId());
        inventoryItem1.setSteamId(steamId);
        inventoryItem1.setOriginalId(itemId1);
        inventoryItem1.setDefIndex(defIndex1);
        inventoryItem1.setLevel((byte) 1);
        inventoryItem1.setInventoryToken(1000L);
        inventoryItem1.setInventoryPosition(1000);
        inventoryItem1.setQuantity(1);
        inventoryItem1.setQuality((byte) 1);
        inventoryItem1.setStyle((byte) 1);
        inventoryItem1.setIsTradable(true);
        inventoryItem1.setIsCraftable(true);
        inventoryItem1.setCustomName("Sample #1");
        inventoryItem1.setItemEquipment(List.of(new ItemEquipment(1, 1)));
        inventoryItem1.setAttributes(List.of(new ItemAttribute(7, "1", 1.0, null)));

        Long itemId2 = 101L;
        Integer defIndex2 = 201;
        ObjectId objectId2 = new ObjectId();
        InventoryItem inventoryItem2 = new InventoryItem();
        inventoryItem2.setId(objectId2);
        inventoryItem2.setItemId(itemId2);
        inventoryItem2.setOperationId(operation1.getId());
        inventoryItem2.setSteamId(steamId);
        inventoryItem2.setOriginalId(itemId2);
        inventoryItem2.setDefIndex(defIndex2);
        inventoryItem2.setLevel((byte) 1);
        inventoryItem2.setInventoryToken(1000L);
        inventoryItem2.setInventoryPosition(1000);
        inventoryItem2.setQuantity(1);
        inventoryItem2.setQuality((byte) 1);
        inventoryItem2.setStyle((byte) 1);
        inventoryItem2.setIsTradable(true);
        inventoryItem2.setIsCraftable(true);
        inventoryItem2.setCustomName("Sample #2");
        inventoryItem2.setItemEquipment(List.of(new ItemEquipment(1, 2)));
        inventoryItem2.setAttributes(List.of(new ItemAttribute(8, "2", 1.0, null)));

        when(inventoryItemRepository.findAll(inventory)).thenReturn(List.of(inventoryItem1, inventoryItem2));

        Operation operation2 = new Operation();
        operation2.setId(new ObjectId());
        operation2.setSteamId(steamId);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(2);

        ItemDTO item1 = new ItemDTO();
        item1.setId(itemId1);
        item1.setOriginal_id(itemId1);
        item1.setDefindex(defIndex1);
        item1.setLevel((byte) 1);
        item1.setQuality((byte) 1);
        item1.setInventory(1000L);
        item1.setQuantity(1);
        item1.setFlag_cannot_craft(false);
        item1.setFlag_cannot_trade(false);
        item1.setStyle((byte) 1);
        item1.setCustom_name("Sample #1");
        ItemEquipDTO itemEquip11 = new ItemEquipDTO();
        itemEquip11.setEquip_class(1);
        itemEquip11.setSlot(1);
        item1.setEquipped(List.of(itemEquip11));
        ItemAttributeDTO itemAttribute11 = new ItemAttributeDTO();
        itemAttribute11.setDefindex(7);
        itemAttribute11.setValue("1");
        itemAttribute11.setFloat_value(1.0);
        item1.setAttributes(List.of(itemAttribute11));

        List<ItemDTO> items = List.of(item1);

        // when
        underTest.sync(inventory, operation2, items);

        // then
        ArgumentCaptor<Set<ObjectId>> updateArgumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(inventoryItemRepository, times(2)).updateAll(
                updateArgumentCaptor.capture(),
                ArgumentMatchers.anyList()
        );

        assertThat(updateArgumentCaptor.getAllValues().getFirst().size()).isEqualTo(1);
        // check ids is the same
        assertThat(updateArgumentCaptor.getAllValues().getFirst().iterator().next().compareTo(objectId2)).isEqualTo(0);

        ArgumentCaptor<List<InventoryItem>> insertArgumentCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(inventoryItemRepository).insertAll(insertArgumentCaptor.capture());
        assertThat(insertArgumentCaptor.getValue().size()).isEqualTo(0);
    }

    @Test
    void itShouldMarkInventoryItemsAsUpdated() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);

        Long itemId1 = 100L;
        Integer defIndex1 = 200;
        ObjectId objectId1 = new ObjectId();
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setId(objectId1);
        inventoryItem1.setItemId(itemId1);
        inventoryItem1.setOperationId(operation1.getId());
        inventoryItem1.setSteamId(steamId);
        inventoryItem1.setOriginalId(itemId1);
        inventoryItem1.setDefIndex(defIndex1);
        inventoryItem1.setLevel((byte) 1);
        inventoryItem1.setInventoryToken(1000L);
        inventoryItem1.setInventoryPosition(1000);
        inventoryItem1.setQuantity(1);
        inventoryItem1.setQuality((byte) 1);
        inventoryItem1.setStyle((byte) 1);
        inventoryItem1.setIsTradable(true);
        inventoryItem1.setIsCraftable(true);
        inventoryItem1.setCustomName("Sample #1");
        inventoryItem1.setItemEquipment(List.of(new ItemEquipment(1, 1)));
        inventoryItem1.setAttributes(List.of(new ItemAttribute(7, "1", 1.0, null)));

        Long itemId2 = 101L;
        Integer defIndex2 = 201;
        ObjectId objectId2 = new ObjectId();
        InventoryItem inventoryItem2 = new InventoryItem();
        inventoryItem2.setId(objectId2);
        inventoryItem2.setItemId(itemId2);
        inventoryItem2.setOperationId(operation1.getId());
        inventoryItem2.setSteamId(steamId);
        inventoryItem2.setOriginalId(itemId2);
        inventoryItem2.setDefIndex(defIndex2);
        inventoryItem2.setLevel((byte) 1);
        inventoryItem2.setInventoryToken(1000L);
        inventoryItem2.setInventoryPosition(1000);
        inventoryItem2.setQuantity(1);
        inventoryItem2.setQuality((byte) 1);
        inventoryItem2.setStyle((byte) 1);
        inventoryItem2.setIsTradable(true);
        inventoryItem2.setIsCraftable(true);
        inventoryItem2.setCustomName("Sample #2");
        inventoryItem2.setItemEquipment(List.of(new ItemEquipment(1, 2)));
        inventoryItem2.setAttributes(List.of(new ItemAttribute(8, "2", 1.0, null)));

        when(inventoryItemRepository.findAll(inventory)).thenReturn(List.of(inventoryItem1, inventoryItem2));

        Operation operation2 = new Operation();
        operation2.setId(new ObjectId());
        operation2.setSteamId(steamId);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(2);

        ItemDTO item1 = new ItemDTO();
        item1.setId(itemId1);
        item1.setOriginal_id(itemId1);
        item1.setDefindex(defIndex1);
        item1.setLevel((byte) 1);
        item1.setQuality((byte) 1);
        item1.setInventory(1000L);
        item1.setQuantity(1);
        item1.setFlag_cannot_craft(false);
        item1.setFlag_cannot_trade(false);
        item1.setStyle((byte) 1);
        item1.setCustom_name("Sample #1");
        ItemEquipDTO itemEquip11 = new ItemEquipDTO();
        itemEquip11.setEquip_class(1);
        itemEquip11.setSlot(1);
        item1.setEquipped(List.of(itemEquip11));
        ItemAttributeDTO itemAttribute11 = new ItemAttributeDTO();
        itemAttribute11.setDefindex(7);
        itemAttribute11.setValue("1");
        itemAttribute11.setFloat_value(1.0);
        item1.setAttributes(List.of(itemAttribute11));

        ItemDTO item2 = new ItemDTO();
        item2.setId(itemId2);
        item2.setOriginal_id(itemId2);
        item2.setDefindex(defIndex2);
        item2.setLevel((byte) 1);
        item2.setQuality((byte) 1);
        item2.setInventory(1000L);
        item2.setQuantity(1);
        item2.setFlag_cannot_craft(false);
        item2.setFlag_cannot_trade(false);
        item2.setStyle((byte) 1);
        item2.setCustom_name("Sample #2 - updated");
        ItemEquipDTO itemEquip21 = new ItemEquipDTO();
        itemEquip21.setEquip_class(1);
        itemEquip21.setSlot(2);
        item2.setEquipped(List.of(itemEquip21));
        ItemAttributeDTO itemAttribute21 = new ItemAttributeDTO();
        itemAttribute21.setDefindex(8);
        itemAttribute21.setValue("2");
        itemAttribute21.setFloat_value(1.0);
        item2.setAttributes(List.of(itemAttribute21));

        List<ItemDTO> items = List.of(item1, item2);

        // when
        underTest.sync(inventory, operation2, items);

        // then
        ArgumentCaptor<Set<ObjectId>> updateArgumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(inventoryItemRepository, times(1)).updateAll(
                updateArgumentCaptor.capture(),
                ArgumentMatchers.anyList()
        );

        assertThat(updateArgumentCaptor.getValue().size()).isEqualTo(1);
        assertThat(updateArgumentCaptor.getValue().iterator().next().compareTo(objectId2)).isEqualTo(0);

        ArgumentCaptor<List<InventoryItem>> insertArgumentCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(inventoryItemRepository).insertAll(insertArgumentCaptor.capture());
        assertThat(insertArgumentCaptor.getValue().size()).isEqualTo(1);
        assertThat(insertArgumentCaptor.getValue().getFirst().getItemId()).isEqualTo(itemId2);
        assertThat(insertArgumentCaptor.getValue().getFirst().getCustomName()).isEqualTo("Sample #2 - updated");
        assertThat(insertArgumentCaptor.getValue().getFirst().getIsActive()).isEqualTo(true);
    }

    @Test
    void itShouldSync() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        Operation operation1 = new Operation();
        operation1.setId(new ObjectId());
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);

        // item #1
        Long itemId1 = 100L;
        Integer defIndex1 = 200;
        ObjectId objectId1 = new ObjectId();
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setId(objectId1);
        inventoryItem1.setItemId(itemId1);
        inventoryItem1.setOperationId(operation1.getId());
        inventoryItem1.setSteamId(steamId);
        inventoryItem1.setOriginalId(itemId1);
        inventoryItem1.setDefIndex(defIndex1);
        inventoryItem1.setLevel((byte) 1);
        inventoryItem1.setInventoryToken(1000L);
        inventoryItem1.setInventoryPosition(1000);
        inventoryItem1.setQuantity(1);
        inventoryItem1.setQuality((byte) 1);
        inventoryItem1.setStyle((byte) 1);
        inventoryItem1.setIsTradable(true);
        inventoryItem1.setIsCraftable(true);
        inventoryItem1.setCustomName("Sample #1");
        inventoryItem1.setItemEquipment(List.of(new ItemEquipment(1, 1)));
        inventoryItem1.setAttributes(List.of(new ItemAttribute(7, "1", 1.0, null)));

        // item #2
        Long itemId2 = 101L;
        Integer defIndex2 = 201;
        ObjectId objectId2 = new ObjectId();
        InventoryItem inventoryItem2 = new InventoryItem();
        inventoryItem2.setId(objectId2);
        inventoryItem2.setItemId(itemId2);
        inventoryItem2.setOperationId(operation1.getId());
        inventoryItem2.setSteamId(steamId);
        inventoryItem2.setOriginalId(itemId2);
        inventoryItem2.setDefIndex(defIndex2);
        inventoryItem2.setLevel((byte) 1);
        inventoryItem2.setInventoryToken(1000L);
        inventoryItem2.setInventoryPosition(1000);
        inventoryItem2.setQuantity(1);
        inventoryItem2.setQuality((byte) 1);
        inventoryItem2.setStyle((byte) 1);
        inventoryItem2.setIsTradable(true);
        inventoryItem2.setIsCraftable(true);
        inventoryItem2.setCustomName("Sample #2");
        inventoryItem2.setItemEquipment(List.of(new ItemEquipment(1, 2)));
        inventoryItem2.setAttributes(List.of(new ItemAttribute(8, "2", 1.0, null)));

        Long itemId3 = 102L;
        Integer defIndex3 = 203;
        ObjectId objectId3 = new ObjectId();
        // item #3 - hidden
        InventoryItem inventoryItem3 = new InventoryItem();
        inventoryItem3.setId(objectId3);
        inventoryItem3.setIsActive(false);
        inventoryItem3.setItemId(itemId3);
        inventoryItem3.setOperationId(operation1.getId());
        inventoryItem3.setSteamId(steamId);
        inventoryItem3.setOriginalId(itemId3);
        inventoryItem3.setDefIndex(defIndex3);
        inventoryItem3.setLevel((byte) 1);
        inventoryItem3.setInventoryToken(1000L);
        inventoryItem3.setInventoryPosition(1000);
        inventoryItem3.setQuantity(1);
        inventoryItem3.setQuality((byte) 1);
        inventoryItem3.setStyle((byte) 1);
        inventoryItem3.setIsTradable(true);
        inventoryItem3.setIsCraftable(true);
        inventoryItem3.setCustomName("Sample #3");
        inventoryItem3.setItemEquipment(List.of(new ItemEquipment(1, 3)));
        inventoryItem3.setAttributes(List.of(new ItemAttribute(9, "3", 1.0, null)));

        // item #3 - active
        ObjectId objectId31 = new ObjectId();
        InventoryItem inventoryItem31 = new InventoryItem();
        inventoryItem31.setId(objectId31);
        inventoryItem31.setItemId(itemId3);
        inventoryItem31.setOperationId(operation1.getId());
        inventoryItem31.setSteamId(steamId);
        inventoryItem31.setOriginalId(itemId3);
        inventoryItem31.setDefIndex(defIndex3);
        inventoryItem31.setLevel((byte) 1);
        inventoryItem31.setInventoryToken(1000L);
        inventoryItem31.setInventoryPosition(1000);
        inventoryItem31.setQuantity(1);
        inventoryItem31.setQuality((byte) 1);
        inventoryItem31.setStyle((byte) 1);
        inventoryItem31.setIsTradable(true);
        inventoryItem31.setIsCraftable(true);
        inventoryItem31.setCustomName("Sample #3 - updated");
        inventoryItem31.setItemEquipment(List.of(new ItemEquipment(1, 3)));
        inventoryItem31.setAttributes(List.of(new ItemAttribute(9, "3", 1.0, null)));

        when(inventoryItemRepository.findAll(inventory))
                .thenReturn(List.of(inventoryItem1, inventoryItem2, inventoryItem31));

        // #1 - updated, #2 - deleted, #3 - the same, #4 - new
        ItemDTO item1 = new ItemDTO();
        item1.setId(itemId1);
        item1.setOriginal_id(itemId1);
        item1.setDefindex(defIndex1);
        item1.setLevel((byte) 1);
        item1.setQuality((byte) 1);
        item1.setInventory(1000L);
        item1.setQuantity(1);
        item1.setFlag_cannot_craft(false);
        item1.setFlag_cannot_trade(false);
        item1.setStyle((byte) 1);
        item1.setCustom_name("Sample #1 - updated");
        ItemEquipDTO itemEquip11 = new ItemEquipDTO();
        itemEquip11.setEquip_class(1);
        itemEquip11.setSlot(1);
        item1.setEquipped(List.of(itemEquip11));
        ItemAttributeDTO itemAttribute11 = new ItemAttributeDTO();
        itemAttribute11.setDefindex(7);
        itemAttribute11.setValue("1");
        itemAttribute11.setFloat_value(1.0);
        item1.setAttributes(List.of(itemAttribute11));

        ItemDTO item3 = new ItemDTO();
        item3.setId(itemId3);
        item3.setOriginal_id(itemId3);
        item3.setDefindex(defIndex3);
        item3.setLevel((byte) 1);
        item3.setQuality((byte) 1);
        item3.setInventory(1000L);
        item3.setQuantity(1);
        item3.setFlag_cannot_craft(false);
        item3.setFlag_cannot_trade(false);
        item3.setStyle((byte) 1);
        item3.setCustom_name("Sample #3 - updated");
        ItemEquipDTO itemEquip31 = new ItemEquipDTO();
        itemEquip31.setEquip_class(1);
        itemEquip31.setSlot(3);
        item3.setEquipped(List.of(itemEquip31));
        ItemAttributeDTO itemAttribute31 = new ItemAttributeDTO();
        itemAttribute31.setDefindex(9);
        itemAttribute31.setValue("3");
        itemAttribute31.setFloat_value(1.0);
        item3.setAttributes(List.of(itemAttribute31));

        Long itemId4 = 103L;
        Integer defIndex4 = 204;
        ItemDTO item4 = new ItemDTO();
        item4.setId(itemId4);
        item4.setOriginal_id(itemId4);
        item4.setDefindex(defIndex4);
        item4.setLevel((byte) 1);
        item4.setQuality((byte) 1);
        item4.setInventory(1000L);
        item4.setQuantity(1);
        item4.setFlag_cannot_craft(false);
        item4.setFlag_cannot_trade(false);
        item4.setStyle((byte) 1);
        item4.setCustom_name("Sample #4");
        ItemEquipDTO itemEquip41 = new ItemEquipDTO();
        itemEquip41.setEquip_class(1);
        itemEquip41.setSlot(4);
        item4.setEquipped(List.of(itemEquip41));
        ItemAttributeDTO itemAttribute41 = new ItemAttributeDTO();
        itemAttribute41.setDefindex(10);
        itemAttribute41.setValue("4");
        itemAttribute41.setFloat_value(1.0);
        item4.setAttributes(List.of(itemAttribute41));

        List<ItemDTO> items = List.of(item1, item3, item4);

        Operation operation2 = new Operation();
        operation2.setId(new ObjectId());
        operation2.setSteamId(steamId);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(2);

        // when
        underTest.sync(inventory, operation2, items);

        // then
        ArgumentCaptor<Set<ObjectId>> updateArgumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(inventoryItemRepository, times(2)).updateAll(
                updateArgumentCaptor.capture(),
                ArgumentMatchers.anyList()
        );

        // hide
        assertThat(updateArgumentCaptor.getAllValues().getFirst().size()).isEqualTo(1);
        assertThat(updateArgumentCaptor.getAllValues().getFirst().iterator().next().compareTo(objectId2)).isEqualTo(0);

        // update
        assertThat(updateArgumentCaptor.getAllValues().get(1).size()).isEqualTo(1);
        assertThat(updateArgumentCaptor.getAllValues().get(1).iterator().next().compareTo(objectId1)).isEqualTo(0);

        ArgumentCaptor<List<InventoryItem>> insertArgumentCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(inventoryItemRepository).insertAll(insertArgumentCaptor.capture());
        assertThat(insertArgumentCaptor.getValue().size()).isEqualTo(2);

        // create copy
        assertThat(insertArgumentCaptor.getValue().getFirst().getItemId()).isEqualTo(itemId1);
        assertThat(insertArgumentCaptor.getValue().getFirst().getCustomName()).isEqualTo("Sample #1 - updated");
        assertThat(insertArgumentCaptor.getValue().getFirst().getIsActive()).isEqualTo(true);

        // create new
        assertThat(insertArgumentCaptor.getValue().get(1).getItemId()).isEqualTo(itemId4);
        assertThat(insertArgumentCaptor.getValue().get(1).getCustomName()).isEqualTo("Sample #4");
    }
}
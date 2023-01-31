package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.mapper.PageMapper;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemAttributeDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemEquipDTO;
import com.dotashowcase.inventoryservice.support.SortBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    void canSyncWhenNoInventoryItems() {
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
        verify(inventoryItemRepository, never()).updateAll(new HashSet<>(), new ArrayList<>());

        ArgumentCaptor<List<InventoryItem>> inventoryItemsArgumentCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(inventoryItemRepository).insertAll(inventoryItemsArgumentCaptor.capture());

        assertThat(inventoryItemsArgumentCaptor.getValue().get(0).getItemId()).isEqualTo(item1.getId());
        assertThat(inventoryItemsArgumentCaptor.getValue().get(0).getSteamId()).isEqualTo(steamId);

        assertThat(inventoryItemsArgumentCaptor.getValue().get(1).getItemId()).isEqualTo(item2.getId());
        assertThat(inventoryItemsArgumentCaptor.getValue().get(1).getSteamId()).isEqualTo(steamId);
    }
}
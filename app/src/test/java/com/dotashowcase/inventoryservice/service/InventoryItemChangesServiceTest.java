package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.ItemAttribute;
import com.dotashowcase.inventoryservice.model.embedded.ItemEquipment;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryChangesDTO;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryItemChangesServiceTest {

    @Mock
    private InventoryItemDALRepository inventoryItemRepository;

    @Mock
    private OperationService operationService;

    private InventoryItemChangesService underTest;

    @BeforeEach
    void setUp() {
        underTest = new InventoryItemChangesServiceImpl(inventoryItemRepository, operationService);
    }

    @Test
    void canGetByVersion() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);
        Integer version = 1;

        Operation operation1 = new Operation();
        operation1.setId(new ObjectId());
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(version);

        Long itemId1 = 100L;
        Integer defIndex1 = 200;
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemId(itemId1);
        inventoryItem1.setOperationId(operation1.getId());
        inventoryItem1.setOperationType(Operation.Type.C);
        inventoryItem1.setSteamId(steamId);
        inventoryItem1.setOriginalId(itemId1);
        inventoryItem1.setDefIndex(defIndex1);
        inventoryItem1.setLevel((byte) 1);
        inventoryItem1.setInventoryToken(1000L);
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
        inventoryItem2.setOperationType(Operation.Type.C);
        inventoryItem2.setSteamId(steamId);
        inventoryItem2.setOriginalId(itemId2);
        inventoryItem2.setDefIndex(defIndex2);
        inventoryItem2.setLevel((byte) 1);
        inventoryItem2.setInventoryToken(1000L);
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
        InventoryItem inventoryItem3 = new InventoryItem();
        inventoryItem3.setItemId(itemId2);
        inventoryItem3.setOperationId(operation1.getId());
        inventoryItem3.setOperationType(Operation.Type.C);
        inventoryItem3.setDeleteOperationId(operation1.getId());
        inventoryItem3.setSteamId(steamId);
        inventoryItem3.setOriginalId(itemId3);
        inventoryItem3.setDefIndex(defIndex3);
        inventoryItem3.setLevel((byte) 1);
        inventoryItem3.setInventoryToken(1000L);
        inventoryItem3.setQuantity(1);
        inventoryItem3.setQuality((byte) 1);
        inventoryItem3.setStyle((byte) 1);
        inventoryItem3.setIsTradable(true);
        inventoryItem3.setIsCraftable(true);
        inventoryItem3.setCustomName("Sample #3");
        inventoryItem3.setItemEquipment(List.of(new ItemEquipment(1, 3)));
        inventoryItem3.setAttributes(List.of(new ItemAttribute(9, "2", 1.0, null)));

        when(operationService.getByVersion(inventory, version)).thenReturn(operation1);
        when(inventoryItemRepository.findAll(inventory, operation1))
                .thenReturn(List.of(inventoryItem1, inventoryItem2, inventoryItem3));

        // when
        InventoryChangesDTO expected = underTest.get(inventory, version);
        InventoryChangesDTO expected2 = underTest.get(inventory, 0);

        // then
        assertThat(expected.getCreate().size()).isEqualTo(2);
        assertThat(expected.getUpdate().size()).isEqualTo(0);
        assertThat(expected.getDelete().size()).isEqualTo(1);

        assertThat(expected2.getCreate().size()).isEqualTo(0);
        assertThat(expected2.getUpdate().size()).isEqualTo(0);
        assertThat(expected2.getDelete().size()).isEqualTo(0);
    }

    @Test
    void canGetAllVersions() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);
        Integer version1 = 1;
        Integer version2 = 1;

        Operation operation1 = new Operation();
        operation1.setId(new ObjectId());
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(version1);

        Operation operation2 = new Operation();
        operation2.setId(new ObjectId());
        operation2.setSteamId(steamId);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(version2);

        Long itemId1 = 100L;
        Integer defIndex1 = 200;
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemId(itemId1);
        inventoryItem1.setOperationId(operation1.getId());
        inventoryItem1.setOperationType(Operation.Type.C);
        inventoryItem1.setSteamId(steamId);
        inventoryItem1.setOriginalId(itemId1);
        inventoryItem1.setDefIndex(defIndex1);
        inventoryItem1.setLevel((byte) 1);
        inventoryItem1.setInventoryToken(1000L);
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
        inventoryItem2.setOperationType(Operation.Type.C);
        inventoryItem2.setSteamId(steamId);
        inventoryItem2.setOriginalId(itemId2);
        inventoryItem2.setDefIndex(defIndex2);
        inventoryItem2.setLevel((byte) 1);
        inventoryItem2.setInventoryToken(1000L);
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
        InventoryItem inventoryItem3 = new InventoryItem();
        inventoryItem3.setItemId(itemId2);
        inventoryItem3.setOperationId(operation2.getId());
        inventoryItem3.setOperationType(Operation.Type.C);
        inventoryItem3.setSteamId(steamId);
        inventoryItem3.setOriginalId(itemId3);
        inventoryItem3.setDefIndex(defIndex3);
        inventoryItem3.setLevel((byte) 1);
        inventoryItem3.setInventoryToken(1000L);
        inventoryItem3.setQuantity(1);
        inventoryItem3.setQuality((byte) 1);
        inventoryItem3.setStyle((byte) 1);
        inventoryItem3.setIsTradable(true);
        inventoryItem3.setIsCraftable(true);
        inventoryItem3.setCustomName("Sample #3");
        inventoryItem3.setItemEquipment(List.of(new ItemEquipment(1, 3)));
        inventoryItem3.setAttributes(List.of(new ItemAttribute(9, "2", 1.0, null)));

        when(operationService.getAll(inventory)).thenReturn(List.of(operation1, operation2));

        when(inventoryItemRepository.findAll(inventory, operation1))
                .thenReturn(List.of(inventoryItem1, inventoryItem2));
        when(inventoryItemRepository.findAll(inventory, operation2))
                .thenReturn(List.of(inventoryItem3));

        // when
        Map<Integer, InventoryChangesDTO> expected = underTest.get(inventory);

        // then
        assertThat(expected.get(version1).getCreate().size()).isEqualTo(2);
        assertThat(expected.get(version1).getUpdate().size()).isEqualTo(0);
        assertThat(expected.get(version1).getDelete().size()).isEqualTo(0);

        assertThat(expected.get(version2).getCreate().size()).isEqualTo(1);
        assertThat(expected.get(version2).getUpdate().size()).isEqualTo(0);
        assertThat(expected.get(version2).getDelete().size()).isEqualTo(0);
    }
}
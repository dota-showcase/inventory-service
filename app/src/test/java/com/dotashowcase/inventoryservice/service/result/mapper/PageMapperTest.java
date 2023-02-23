package com.dotashowcase.inventoryservice.service.result.mapper;

import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageMapperTest {

    private InventoryItemServiceResultMapper inventoryItemServiceResultMapper;

    private PageMapper<InventoryItem, InventoryItemDTO> underTest;

    @BeforeEach
    void setUp() {
        underTest = new PageMapper<>();
        inventoryItemServiceResultMapper = new InventoryItemServiceResultMapper();
    }

    @Test
    void catGetPageResult() {
        // given
        Long steamId1 = 100000000000L;

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId1);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());

        Long itemId1 = 100L;
        Integer defIndex1 = 200;
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemId(itemId1);
        inventoryItem1.setOperationId(operation1.getId());
        inventoryItem1.setSteamId(steamId1);
        inventoryItem1.setOriginalId(itemId1);
        inventoryItem1.setDefIndex(defIndex1);
        inventoryItem1.setLevel((byte)1);
        inventoryItem1.setInventoryToken(1L);
        inventoryItem1.setQuantity(1);
        inventoryItem1.setQuality((byte)1);
        inventoryItem1.setStyle((byte)1);
        inventoryItem1.setIsTradable(true);
        inventoryItem1.setIsCraftable(true);
        inventoryItem1.setCustomName("inventory item #1");

        Long itemId2 = 101L;
        Integer defIndex2 = 201;
        InventoryItem inventoryItem2 = new InventoryItem();
        inventoryItem2.setItemId(itemId2);
        inventoryItem2.setOperationId(operation1.getId());
        inventoryItem2.setSteamId(steamId1);
        inventoryItem2.setOriginalId(itemId2);
        inventoryItem2.setDefIndex(defIndex2);
        inventoryItem2.setLevel((byte)1);
        inventoryItem2.setInventoryToken(1L);
        inventoryItem2.setQuantity(1);
        inventoryItem2.setQuality((byte)1);
        inventoryItem2.setStyle((byte)1);
        inventoryItem2.setIsTradable(true);
        inventoryItem2.setIsCraftable(true);
        inventoryItem2.setCustomName("inventory item #2");

        Long itemId3 = 102L;
        Integer defIndex3 = 202;
        InventoryItem inventoryItem3 = new InventoryItem();
        inventoryItem3.setItemId(itemId3);
        inventoryItem3.setOperationId(operation1.getId());
        inventoryItem3.setSteamId(steamId1);
        inventoryItem3.setOriginalId(itemId3);
        inventoryItem3.setDefIndex(defIndex3);
        inventoryItem3.setLevel((byte)1);
        inventoryItem3.setInventoryToken(1L);
        inventoryItem3.setQuantity(1);
        inventoryItem3.setQuality((byte)1);
        inventoryItem3.setStyle((byte)1);
        inventoryItem3.setIsTradable(true);
        inventoryItem3.setIsCraftable(true);
        inventoryItem3.setCustomName("inventory item #2");

        List<InventoryItem> inventoryItems = List.of(inventoryItem1, inventoryItem2, inventoryItem3);

        Pageable firstPageWithTwoItems = PageRequest.of(0, 3);
        Page<InventoryItem> inventoryItemPage = new PageImpl<>(
                inventoryItems, firstPageWithTwoItems, inventoryItems.size()
        );

        // when
        PageResult<InventoryItemDTO> expected = underTest.getPageResult(
                inventoryItemPage, inventoryItemServiceResultMapper::getInventoryItemDTO
        );

        // then
        List<InventoryItemDTO> data = expected.getData();
        PageResult.Pagination pagination = expected.getPagination();

        assertThat(data.size()).isEqualTo(3);
        assertThat(data.get(0).getDefIndex()).isEqualTo(defIndex1);
        assertThat(data.get(1).getDefIndex()).isEqualTo(defIndex2);
        assertThat(data.get(2).getDefIndex()).isEqualTo(defIndex3);

        assertThat(pagination.getCurrentPage()).isEqualTo(0);
        assertThat(pagination.getItemsOnPage()).isEqualTo(3);
        assertThat(pagination.getTotalPages()).isEqualTo(1);
        assertThat(pagination.getTotalItems()).isEqualTo(3);
    }
}
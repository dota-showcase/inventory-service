package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.http.filter.InventoryItemFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import com.dotashowcase.inventoryservice.repository.InventoryItemDALRepository;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.mapper.InventoryItemServiceResultMapper;
import com.dotashowcase.inventoryservice.service.result.mapper.PageMapper;
import com.dotashowcase.inventoryservice.support.SortBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryItemServiceTest {

    @Mock
    private InventoryItemDALRepository inventoryItemRepository;

    @Mock
    private SortBuilder sortBuilder;

    @Mock
    private PageMapper<InventoryItem, InventoryItemDTO> pageMapper;

    private InventoryItemService underTest;

    private InventoryItemServiceResultMapper inventoryItemServiceResultMapper;

    @BeforeEach
    void setUp() {
        inventoryItemServiceResultMapper = new InventoryItemServiceResultMapper();
        underTest = new InventoryItemServiceImpl(inventoryItemRepository, sortBuilder, pageMapper);
    }

    @Test
    void canGet() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        InventoryItemFilter filter = InventoryItemFilter.builder()
                .defIndexes(List.of(200, 201, 203))
                .build();

        String sortByStr = "-defIndex";
        Sort sortBy = Sort.by(Sort.Direction.ASC, "defIndex");

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());

        Long itemId1 = 100L;
        Integer defIndex1 = 200;
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemId(itemId1);
        inventoryItem1.setOperationId(operation1.getId());
        inventoryItem1.setSteamId(steamId);
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
        inventoryItem2.setSteamId(steamId);
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

        List<InventoryItem> inventoryItems = List.of(inventoryItem1, inventoryItem2);

        when(sortBuilder.fromRequestParam(sortByStr)).thenReturn(sortBy);
        when(inventoryItemRepository.searchAll(inventory, filter, sortBy)).thenReturn(inventoryItems);

        // when
        List<InventoryItemDTO> expected = underTest.get(inventory, filter, sortByStr);

        // then
        assertThat(expected.size()).isEqualTo(inventoryItems.size());
        assertThat(expected.get(0).getDefIndex()).isEqualTo(defIndex1);
        assertThat(expected.get(1).getDefIndex()).isEqualTo(defIndex2);
    }

    @Test
    void canGetPage() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        InventoryItemFilter filter = InventoryItemFilter.builder()
                .defIndexes(List.of(200, 201, 203))
                .build();

        Pageable firstPageWithTwoItems = PageRequest.of(0, 2);

        String sortByStr = "-defIndex";
        Sort sortBy = Sort.by(Sort.Direction.ASC, "defIndex");

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());

        Long itemId1 = 100L;
        Integer defIndex1 = 200;
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemId(itemId1);
        inventoryItem1.setOperationId(operation1.getId());
        inventoryItem1.setSteamId(steamId);
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
        inventoryItem2.setSteamId(steamId);
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

        List<InventoryItem> inventoryItems = List.of(inventoryItem1, inventoryItem2);
        Page<InventoryItem> inventoryItemPage = new PageImpl<>(
                inventoryItems, firstPageWithTwoItems, inventoryItems.size()
        );

        when(sortBuilder.fromRequestParam(sortByStr)).thenReturn(sortBy);
        when(inventoryItemRepository.searchAll(inventory, firstPageWithTwoItems, filter, sortBy))
                .thenReturn(inventoryItemPage);

        // when
       underTest.get(inventory, firstPageWithTwoItems, filter, sortByStr);

        // then
        ArgumentCaptor<Page<InventoryItem>> pageArgumentCaptor = ArgumentCaptor.forClass((Class)Page.class);
        ArgumentCaptor<Function<InventoryItem, InventoryItemDTO>> lambdaArgumentCaptor
                = ArgumentCaptor.forClass((Class)Function.class);

        verify(inventoryItemRepository).searchAll(inventory, firstPageWithTwoItems, filter, sortBy);
        verify(pageMapper).getPageResult(pageArgumentCaptor.capture(), lambdaArgumentCaptor.capture());

        assertThat(pageArgumentCaptor.getValue().getTotalElements()).isEqualTo(2);
    }

    @Test
    void create() {
    }

    @Test
    void sync() {
    }

    @Test
    void delete() {
    }
}
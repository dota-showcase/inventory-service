package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import com.dotashowcase.inventoryservice.repository.InventoryRepository;
import com.dotashowcase.inventoryservice.repository.OperationRepository;
import com.dotashowcase.inventoryservice.service.exception.InventoryAlreadyExistsException;
import com.dotashowcase.inventoryservice.service.exception.InventoryException;
import com.dotashowcase.inventoryservice.service.exception.InventoryNotFoundException;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestOperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.OperationCountDTO;
import com.dotashowcase.inventoryservice.service.result.dto.OperationDTO;
import com.dotashowcase.inventoryservice.service.result.mapper.PageMapper;
import com.dotashowcase.inventoryservice.steamclient.SteamClient;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.UserInventoryResponseDTO;
import com.dotashowcase.inventoryservice.support.SortBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(MongoTestConfig.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemService inventoryItemService;

    @Mock
    private OperationService operationService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private SortBuilder sortBuilder;

    @Mock
    private SteamClient steamClient;

    @Mock
    private PageMapper<Inventory, InventoryWithLatestOperationDTO> pageMapper;

    private InventoryService underTest;

    @BeforeEach
    void setUp() {
        underTest = new InventoryServiceImpl(
                inventoryItemService, operationService, inventoryRepository, sortBuilder, steamClient, pageMapper
        );
    }

    @Test
    void itShouldGetAllWithSort() {
        // given
        String sortBy = "-steamId";

        when(sortBuilder.fromRequestParam(sortBy)).thenReturn(Sort.by(Sort.Direction.DESC, "steamId"));

        // when
        underTest.getAll(sortBy);

        // then
        verify(sortBuilder).fromRequestParam(sortBy);
        verify(inventoryRepository).findAll(any(Sort.class));
    }

    @Test
    void itShouldGetAllWithNoSort() {
        // given
        // when
        underTest.getAll(null);

        // then
        verify(sortBuilder).fromRequestParam(null);
        verify(inventoryRepository).findAll();
    }

    @Test
    void itShouldGetPage() {
        // given
        Long steamId1 = 100000000000L;
        Long steamId2 = 100000000001L;
        Long steamId3 = 100000000002L;

        Inventory inventory1 = new Inventory(steamId1);
        Inventory inventory2 = new Inventory(steamId2);
        Inventory inventory3 = new Inventory(steamId3);

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId1);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());

        Operation operation2 = new Operation();
        operation2.setSteamId(steamId2);
        operation2.setType(Operation.Type.C);
        operation2.setVersion(1);
        operation2.setMeta(new OperationMeta());

        Operation operation3 = new Operation();
        operation3.setSteamId(steamId3);
        operation3.setType(Operation.Type.C);
        operation3.setVersion(1);
        operation3.setMeta(new OperationMeta());

        Operation operation4 = new Operation();
        operation4.setSteamId(steamId3);
        operation4.setType(Operation.Type.U);
        operation4.setVersion(2);
        operation4.setMeta(new OperationMeta());

        String sortByStr = "-steamId";
        Sort sortBy = Sort.by(Sort.Direction.DESC, "steamId");
        Pageable firstPageWithAllItems = PageRequest.of(0, 4, sortBy);

        List<Inventory> firstPageInventories = List.of(inventory3, inventory2, inventory1);

        when(sortBuilder.fromRequestParam(sortByStr)).thenReturn(sortBy);

        Page<Inventory> inventoriesFirstPage = new PageImpl<>(
                firstPageInventories, firstPageWithAllItems, firstPageInventories.size()
        );

        when(inventoryRepository.findAll(firstPageWithAllItems)).thenReturn(inventoriesFirstPage);
        when(operationService.getAllLatest(List.of(steamId3, steamId2, steamId1)))
                .thenReturn(Map.of(steamId3, operation4, steamId2, operation2, steamId1, operation1));

        // when
        underTest.getPage(firstPageWithAllItems, sortByStr);

        // then
        ArgumentCaptor<Page<Inventory>> pageArgumentCaptor = ArgumentCaptor.forClass((Class)Page.class);
        ArgumentCaptor<List<InventoryWithLatestOperationDTO>> lambdaArgumentCaptor
                = ArgumentCaptor.forClass((Class)List.class);

        verify(inventoryRepository).findAll(firstPageWithAllItems);
        verify(pageMapper).getPageResultWithoutMapping(pageArgumentCaptor.capture(), lambdaArgumentCaptor.capture());

        assertThat(pageArgumentCaptor.getValue().getTotalElements()).isEqualTo(3);
    }

    @Test
    void itShouldGetBySteamId() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());

        when(inventoryRepository.findItemBySteamId(steamId)).thenReturn(inventory);
        when(operationService.getLatest(inventory)).thenReturn(operation1);

        // when
        InventoryWithLatestOperationDTO result = underTest.get(steamId);

        // then
        assertThat(result.getOperation().getVersion()).isEqualTo(1);
    }

    @Test
    void willThrowWhenInventoryNotFound() {
        // given
        Long steamId1 = 100000000000L;
        Long steamId2 = 100000000001L;
        Inventory inventory = new Inventory(steamId1);

        when(inventoryRepository.findItemBySteamId(steamId2)).thenReturn(null);

        // when
        // then
        assertThatThrownBy(() -> underTest.get(steamId2))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void itShouldCreateInventory() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);
        Inventory savedInventory = new Inventory(steamId);

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());

        UserInventoryResponseDTO inventoryResponseDTO = new UserInventoryResponseDTO();
        inventoryResponseDTO.setStatus(1);
        inventoryResponseDTO.setNumberBackpackSlots(1000);
        ItemDTO itemDTO1 = new ItemDTO();
        itemDTO1.setId(100L);
        itemDTO1.setOriginal_id(100L);
        itemDTO1.setDefindex(300);
        itemDTO1.setLevel((byte) 1);
        itemDTO1.setQuantity(1);
        itemDTO1.setQuality((byte) 1);
        itemDTO1.setInventory(1000L);

        inventoryResponseDTO.setItems(List.of(itemDTO1));

        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemId(100L);
        inventoryItem1.setSteamId(steamId);
        inventoryItem1.setOriginalId(100L);
        inventoryItem1.setDefIndex(300);
        inventoryItem1.setLevel((byte)1);
        inventoryItem1.setQuantity(1);
        inventoryItem1.setQuality((byte)1);
        inventoryItem1.setInventoryToken(1000L);
        inventoryItem1.setInventoryPosition(1000);

        when(inventoryRepository.findItemBySteamId(steamId)).thenReturn(null);
        when(steamClient.fetchUserInventory(steamId)).thenReturn(inventoryResponseDTO);
        when(inventoryRepository.save(inventory)).thenReturn(savedInventory);
        when(operationService.create(inventory, null, null)).thenReturn(operation1);
        when(inventoryItemService.create(savedInventory, operation1, inventoryResponseDTO.getItems()))
                .thenReturn(List.of(inventoryItem1));

        // when
        underTest.create(steamId);

        // then
        verify(steamClient).fetchUserInventory(steamId);
        verify(inventoryRepository).save(inventory);
        verify(operationService).create(savedInventory, null, null);
        verify(inventoryItemService).create(savedInventory, operation1, inventoryResponseDTO.getItems());
        verify(operationService).createAndSaveMeta(
                operation1,
                new OperationCountDTO(1, 0, 0, 0),
                1,
                1000
                );
    }

    @Test
    void willThrowWhenCreateExistingInventory() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        when(inventoryRepository.findItemBySteamId(steamId)).thenReturn(inventory);

        // when
        // then
        assertThatThrownBy(() -> underTest.create(steamId))
                .isInstanceOf(InventoryAlreadyExistsException.class);
    }

    @Test
    void itShouldUpdateInventory() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        Operation prevOperation = new Operation();
        prevOperation.setSteamId(steamId);
        prevOperation.setType(Operation.Type.C);
        prevOperation.setVersion(1);
        prevOperation.setMeta(new OperationMeta());

        Operation currentOperation = new Operation();
        currentOperation.setSteamId(steamId);
        currentOperation.setType(Operation.Type.U);
        currentOperation.setVersion(2);
        currentOperation.setMeta(new OperationMeta());

        UserInventoryResponseDTO inventoryResponseDTO = new UserInventoryResponseDTO();
        inventoryResponseDTO.setStatus(1);
        inventoryResponseDTO.setNumberBackpackSlots(1000);
        ItemDTO itemDTO1 = new ItemDTO();
        itemDTO1.setId(100L);
        itemDTO1.setOriginal_id(100L);
        itemDTO1.setDefindex(300);
        itemDTO1.setLevel((byte) 1);
        itemDTO1.setQuantity(1);
        itemDTO1.setQuality((byte) 1);
        itemDTO1.setInventory(1000L);

        inventoryResponseDTO.setItems(List.of(itemDTO1));

        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemId(100L);
        inventoryItem1.setSteamId(steamId);
        inventoryItem1.setOriginalId(100L);
        inventoryItem1.setDefIndex(300);
        inventoryItem1.setLevel((byte)1);
        inventoryItem1.setQuantity(1);
        inventoryItem1.setQuality((byte)1);
        inventoryItem1.setInventoryToken(1000L);
        inventoryItem1.setInventoryPosition(1000);

        OperationCountDTO operationCountDTO = new OperationCountDTO(1, 0, 0, 1);

        when(inventoryRepository.findItemBySteamId(steamId)).thenReturn(inventory);
        when(operationService.getLatest(inventory)).thenReturn(prevOperation);
        when(steamClient.fetchUserInventory(steamId)).thenReturn(inventoryResponseDTO);
        when(operationService.create(inventory, Operation.Type.U, prevOperation)).thenReturn(currentOperation);
        when(inventoryItemService.sync(inventory, currentOperation, inventoryResponseDTO.getItems()))
                .thenReturn(operationCountDTO);

        // when
        underTest.update(steamId);

        // then
        verify(steamClient).fetchUserInventory(steamId);
        verify(operationService).create(inventory, Operation.Type.U, prevOperation);
        verify(inventoryItemService).sync(inventory, currentOperation, inventoryResponseDTO.getItems());
        verify(operationService).createAndSaveMeta(
                currentOperation,
                operationCountDTO,
                1,
                inventoryResponseDTO.getNumberBackpackSlots()
        );
    }

    @Test
    void willThrowWhenUpdateNotExistingInventory() {
        // given
        Long steamId = 100000000000L;

        when(inventoryRepository.findItemBySteamId(steamId)).thenReturn(null);

        // when
        // then
        assertThatThrownBy(() -> underTest.update(steamId))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void willThrowWhenUpdateInventoryNoPreviousOperation() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        when(inventoryRepository.findItemBySteamId(steamId)).thenReturn(inventory);
        when(operationService.getLatest(inventory)).thenReturn(null);

        // when
        // then
        assertThatThrownBy(() -> underTest.update(steamId))
                .isInstanceOf(InventoryException.class)
                .hasMessageContaining("Cannot find Inventory Operation resource");
    }

    @Test
    void itShouldDeleteInventory() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);
        when(inventoryRepository.findItemBySteamId(steamId)).thenReturn(inventory);

        // when
        underTest.delete(steamId);

        // then
        verify(inventoryRepository).delete(inventory);
        verify(operationService).delete(inventory);
        verify(inventoryItemService).delete(inventory);
    }
}
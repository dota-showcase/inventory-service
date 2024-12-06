package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import com.dotashowcase.inventoryservice.repository.OperationRepository;
import com.dotashowcase.inventoryservice.service.exception.OperationNotFoundException;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.result.dto.OperationCountDTO;
import com.dotashowcase.inventoryservice.service.result.dto.OperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import com.dotashowcase.inventoryservice.service.result.mapper.PageMapper;
import com.dotashowcase.inventoryservice.support.SortBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DataMongoTest
@ExtendWith(MockitoExtension.class)
@Import(MongoTestConfig.class)
class OperationServiceTest {

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private SortBuilder sortBuilder;

    @Mock
    private PageMapper<Operation, OperationDTO> pageMapper;

    private OperationService underTest;

    @BeforeEach
    void setUp() {
        underTest = new OperationServiceImpl(operationRepository, sortBuilder, pageMapper);
    }

    @Test
    void itShouldGetAllLatestOperations() {
        // given
        Long steamId1 = 100000000000L;
        Long steamId2 = 100000000001L;
        List<Long> steamIds = List.of(steamId1, steamId2);

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId1);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());

        Operation operation2 = new Operation();
        operation2.setSteamId(steamId1);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(2);
        operation2.setMeta(new OperationMeta());

        Operation operation3 = new Operation();
        operation3.setSteamId(steamId2);
        operation3.setType(Operation.Type.C);
        operation3.setVersion(1);
        operation3.setMeta(new OperationMeta());

        List<Operation> operations = List.of(operation2, operation3);

//        when(operationRepository.findLatestByInventoriesNPlusOne(steamIds)).thenReturn(operations);
        when(operationRepository.aggregateLatestByInventories(steamIds)).thenReturn(operations);

        // when
        Map<Long, Operation> result = underTest.getAllLatest(List.of(steamId1, steamId2));

        // then
        ArgumentCaptor<List<Long>> steamIdListCaptor = ArgumentCaptor.forClass((Class) List.class);

        verify(operationRepository).findLatestByInventoriesNPlusOne(steamIdListCaptor.capture());
        assertThat(steamIdListCaptor.getValue()).isEqualTo(steamIds);

        Map<Long, Operation> expectedResult = new HashMap<>();
        expectedResult.put(steamId1, operation2);
        expectedResult.put(steamId2, operation3);

        assertThat(result).containsAllEntriesOf(expectedResult);
    }

    @Test
    void itShouldGetPageOperationFull() {
        // given
        Long steamId1 = 100000000000L;

        Inventory inventory1 = new Inventory(steamId1);

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId1);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());

        Operation operation2 = new Operation();
        operation2.setSteamId(steamId1);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(2);
        operation2.setMeta(new OperationMeta());

        Operation operation3 = new Operation();
        operation3.setSteamId(steamId1);
        operation3.setType(Operation.Type.U);
        operation3.setVersion(3);
        operation3.setMeta(new OperationMeta());

        Pageable firstPageWithAllItems = PageRequest.of(0, 4);

        List<Operation> firstPageOperations = List.of(operation3, operation2, operation1);

        String sortByStr = "-version";
        Sort sortBy = Sort.by(Sort.Direction.DESC, "version");

        when(sortBuilder.fromRequestParam(sortByStr)).thenReturn(sortBy);

        Page<Operation> operationFirstPage = new PageImpl<>(
                firstPageOperations, firstPageWithAllItems, firstPageOperations.size()
        );

        when(operationRepository.findPage(inventory1, firstPageWithAllItems, sortBy)).thenReturn(operationFirstPage);

        // when
        underTest.getPage(inventory1, firstPageWithAllItems, sortByStr);

        // then
        ArgumentCaptor<Page<Operation>> pageArgumentCaptor = ArgumentCaptor.forClass((Class)Page.class);
        ArgumentCaptor<Function<Operation, OperationDTO>> lambdaArgumentCaptor
                = ArgumentCaptor.forClass((Class)Function.class);

        verify(operationRepository).findPage(inventory1, firstPageWithAllItems, sortBy);
        verify(pageMapper).getPageResult(pageArgumentCaptor.capture(), lambdaArgumentCaptor.capture());

        assertThat(pageArgumentCaptor.getValue().getTotalElements()).isEqualTo(3);
    }

    @Test
    void itShouldGetPageOperationSecond() {
        // given
        Long steamId1 = 100000000000L;

        Inventory inventory1 = new Inventory(steamId1);

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId1);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());

        Operation operation2 = new Operation();
        operation2.setSteamId(steamId1);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(2);
        operation2.setMeta(new OperationMeta());

        Pageable secondPageWithAllItems = PageRequest.of(1, 1);

        List<Operation> secondPageOperations = List.of(operation1);

        String sortByStr = "-version";
        Sort sortBy = Sort.by(Sort.Direction.DESC, "version");

        when(sortBuilder.fromRequestParam(sortByStr)).thenReturn(sortBy);

        Page<Operation> operationSecondPage = new PageImpl<>(
                secondPageOperations, secondPageWithAllItems, secondPageOperations.size()
        );

        when(operationRepository.findPage(inventory1, secondPageWithAllItems, sortBy)).thenReturn(operationSecondPage);

        // when
        underTest.getPage(inventory1, secondPageWithAllItems, sortByStr);

        // then
        ArgumentCaptor<Page<Operation>> pageArgumentCaptor = ArgumentCaptor.forClass((Class)Page.class);
        ArgumentCaptor<Function<Operation, OperationDTO>> lambdaArgumentCaptor
                = ArgumentCaptor.forClass((Class)Function.class);

        verify(operationRepository).findPage(inventory1, secondPageWithAllItems, sortBy);
        verify(pageMapper).getPageResult(pageArgumentCaptor.capture(), lambdaArgumentCaptor.capture());

        assertThat(pageArgumentCaptor.getValue().getNumberOfElements()).isEqualTo(1);
    }

    @Test
    void itShouldGetLatestOperation() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        // when
        underTest.getLatest(inventory);

        // then
        ArgumentCaptor<Inventory> inventoryArgumentCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(operationRepository).findLatest(inventoryArgumentCaptor.capture());

        assertThat(inventoryArgumentCaptor.getValue()).isEqualTo(inventory);
    }

    @Test
    void itShouldGetLatestOperationByVersion() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);
        Operation operation1 = new Operation();
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());

        when(operationRepository.findLatest(inventory)).thenReturn(operation1);

        // when
        Operation expected = underTest.getByVersion(inventory, null);

        // then
        ArgumentCaptor<Inventory> inventoryArgumentCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(operationRepository).findLatest(inventoryArgumentCaptor.capture());

        assertThat(inventoryArgumentCaptor.getValue()).isEqualTo(inventory);

        assertThat(expected).extracting(Operation::getSteamId)
                .isEqualTo(steamId);
    }

    @Test
    void itShouldGetOperationByVersion() {
        // given
        Long steamId = 100000000000L;
        Integer version = 1;
        Inventory inventory = new Inventory(steamId);
        Operation operation1 = new Operation();
        operation1.setSteamId(steamId);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(version);
        operation1.setMeta(new OperationMeta());

        when(operationRepository.findByVersion(inventory, version)).thenReturn(operation1);

        // when
        Operation expected = underTest.getByVersion(inventory, version);

        // then
        ArgumentCaptor<Inventory> inventoryArgumentCaptor = ArgumentCaptor.forClass(Inventory.class);
        ArgumentCaptor<Integer> versionArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(operationRepository).findByVersion(inventoryArgumentCaptor.capture(), versionArgumentCaptor.capture());

        assertThat(inventoryArgumentCaptor.getValue()).isEqualTo(inventory);
        assertThat(versionArgumentCaptor.getValue()).isEqualTo(version);

        assertThat(expected).extracting(Operation::getSteamId)
                .isEqualTo(steamId);
    }

    @Test
    void willThrowWhenOperationByVersionNotFound() {
        // given
        Long steamId = 100000000000L;
        Integer version = 1;
        Inventory inventory = new Inventory(steamId);

        when(operationRepository.findByVersion(inventory, version)).thenReturn(null);
        when(operationRepository.findLatest(inventory)).thenReturn(null);

        // when
        // then
        assertThatThrownBy(() -> underTest.getByVersion(inventory, version))
                .isInstanceOf(OperationNotFoundException.class)
                .hasMessageContaining(
                        String.format("Operation with version %d for Inventory %d not exists", version, steamId)
                );

        assertThatThrownBy(() -> underTest.getByVersion(inventory, null))
                .isInstanceOf(OperationNotFoundException.class)
                .hasMessageContaining(
                        String.format("There are not Operations for Inventory %d", steamId)
                );
    }

    @Test
    void itShouldCreateOperationOnlyByInventory() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        // when
        underTest.create(inventory, null, null);

        // then
        ArgumentCaptor<Operation> operationArgumentCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(operationRepository).insertOne(operationArgumentCaptor.capture());

        Operation captorOperation = operationArgumentCaptor.getValue();

        assertThat(captorOperation).extracting(Operation::getSteamId)
                .isEqualTo(steamId);

        assertThat(captorOperation).extracting(Operation::getType)
                .isEqualTo(Operation.Type.C);

        assertThat(captorOperation).extracting(Operation::getVersion)
                .isEqualTo(1);
    }

    @Test
    void itShouldCreateOperationByInventoryAndType() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);
        Operation.Type type = Operation.Type.U;

        // when
        underTest.create(inventory, type, null);

        // then
        ArgumentCaptor<Operation> operationArgumentCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(operationRepository).insertOne(operationArgumentCaptor.capture());

        Operation captorOperation = operationArgumentCaptor.getValue();

        assertThat(captorOperation).extracting(Operation::getSteamId)
                .isEqualTo(steamId);

        assertThat(captorOperation).extracting(Operation::getType)
                .isEqualTo(type);

        assertThat(captorOperation).extracting(Operation::getVersion)
                .isEqualTo(1);
    }

    @Test
    void itShouldCreateOperationByAll() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);
        Operation.Type type = Operation.Type.U;
        int version = 2;

        Operation prevOperation = new Operation();
        prevOperation.setSteamId(steamId);
        prevOperation.setType(Operation.Type.C);
        prevOperation.setVersion(version);
        prevOperation.setMeta(new OperationMeta());

        // when
        underTest.create(inventory, type, prevOperation);

        // then
        ArgumentCaptor<Operation> operationArgumentCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(operationRepository).insertOne(operationArgumentCaptor.capture());

        Operation captorOperation = operationArgumentCaptor.getValue();

        assertThat(captorOperation).extracting(Operation::getSteamId)
                .isEqualTo(steamId);

        assertThat(captorOperation).extracting(Operation::getType)
                .isEqualTo(type);

        assertThat(captorOperation).extracting(Operation::getVersion)
                .isEqualTo(version + 1);
    }

    @Test
    void itShouldCreateAndSaveMeta() {
        // given
        Long steamId = 100000000000L;
        int version = 2;

        Operation operation = new Operation();
        operation.setSteamId(steamId);
        operation.setType(Operation.Type.C);
        operation.setVersion(version);

        OperationCountDTO operationCountDTO = new OperationCountDTO(30, 20, 10, 1000);
        Integer count = 500;
        Integer numSlots = 10000;

        // when
        underTest.createAndSaveMeta(operation, operationCountDTO, count, numSlots);

        // then
        ArgumentCaptor<Operation> operationArgumentCaptor = ArgumentCaptor.forClass(Operation.class);
        ArgumentCaptor<OperationMeta> metaArgumentCaptor = ArgumentCaptor.forClass(OperationMeta.class);
        verify(operationRepository).updateMeta(operationArgumentCaptor.capture(), metaArgumentCaptor.capture());

        Operation captorOperation = operationArgumentCaptor.getValue();
        OperationMeta captorOperationMeta = metaArgumentCaptor.getValue();
        OperationMeta innerOperationMeta = captorOperation.getMeta();

        assertThat(innerOperationMeta).extracting(OperationMeta::getCreateOperationCount)
                .isEqualTo(30)
                .isEqualTo(captorOperationMeta.getCreateOperationCount());

        assertThat(innerOperationMeta).extracting(OperationMeta::getUpdateOperationCount)
                .isEqualTo(20)
                .isEqualTo(captorOperationMeta.getUpdateOperationCount());

        assertThat(innerOperationMeta).extracting(OperationMeta::getDeleteOperationCount)
                .isEqualTo(10)
                .isEqualTo(captorOperationMeta.getDeleteOperationCount());

        assertThat(innerOperationMeta).extracting(OperationMeta::getItemCount)
                .isEqualTo(1000 + 30 - 10)
                .isEqualTo(captorOperationMeta.getItemCount());

        assertThat(innerOperationMeta).extracting(OperationMeta::getResponseCount)
                .isEqualTo(500)
                .isEqualTo(captorOperationMeta.getResponseCount());

        assertThat(innerOperationMeta).extracting(OperationMeta::getNumSlots)
                .isEqualTo(10000)
                .isEqualTo(captorOperationMeta.getNumSlots());
    }

    @Test
    void itShouldDeleteOperation() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        // when
        underTest.delete(inventory);

        // then
        verify(operationRepository).removeAll(inventory);
    }
}
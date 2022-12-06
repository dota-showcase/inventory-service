package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import com.dotashowcase.inventoryservice.repository.OperationRepository;
import com.dotashowcase.inventoryservice.service.exception.OperationNotFoundException;
import com.dotashowcase.inventoryservice.service.result.dto.OperationCountDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DataMongoTest
@ExtendWith(MockitoExtension.class)
class OperationServiceTest {

    @Mock
    private OperationRepository operationRepository;

    private OperationService underTest;

    @BeforeEach
    void setUp() {
        underTest = new OperationServiceImpl(operationRepository);
    }

    @Test
    void canGetAll() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        // when
        underTest.getAll(inventory);

        // then
        ArgumentCaptor<Inventory> inventoryArgumentCaptor = ArgumentCaptor.forClass(Inventory.class);
        ArgumentCaptor<Integer> limitArgumentCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(operationRepository)
                .findNLatest(inventoryArgumentCaptor.capture(), limitArgumentCaptor.capture());

        assertThat(inventoryArgumentCaptor.getValue()).isEqualTo(inventory);
        assertThat(limitArgumentCaptor.getValue()).isEqualTo(-1);
    }

    @Test
    void canGetAllByIds() {
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
        operation3.setSteamId(steamId1);
        operation3.setType(Operation.Type.U);
        operation3.setVersion(3);
        operation3.setMeta(new OperationMeta());

        Operation operation4 = new Operation();
        operation4.setSteamId(steamId2);
        operation4.setType(Operation.Type.C);
        operation4.setVersion(1);
        operation4.setMeta(new OperationMeta());

        List<Operation> operations = List.of(operation1, operation2, operation3, operation4);

        when(operationRepository.findByInventories(steamIds)).thenReturn(operations);

        // when
        Map<Long, List<Operation>> result = underTest.getAll(List.of(steamId1, steamId2));

        // then
        ArgumentCaptor<List<Long>> steamIdListCaptor = ArgumentCaptor.forClass((Class) List.class);

        verify(operationRepository).findByInventories(steamIdListCaptor.capture());
        assertThat(steamIdListCaptor.getValue()).isEqualTo(steamIds);

        Map<Long, List<Operation>> expectedResult = new HashMap<>();
        expectedResult.put(steamId1, List.of(operation1, operation2, operation3));
        expectedResult.put(steamId2, List.of(operation4));

        assertThat(result).containsAllEntriesOf(expectedResult);
    }

    @Test
    void canGetLatest() {
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
    void canGetByVersionLatest() {
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
    void canGetByVersion() {
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
    void canCreateOnlyByInventory() {
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
    void canCreateWithInventoryAndType() {
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
    void canCreateWithAll() {
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
    void canCreateAndSaveMeta() {
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
    void canDelete() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        // when
        underTest.delete(inventory);

        // then
        verify(operationRepository).removeAll(inventory);
    }
}
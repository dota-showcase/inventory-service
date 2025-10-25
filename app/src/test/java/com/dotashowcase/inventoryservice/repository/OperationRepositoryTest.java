package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(MongoTestConfig.class)
class OperationRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OperationDAL underTest;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        Long steamId1 = 100000000000L;
        Inventory inventory1 = new Inventory(steamId1);
        inventoryRepository.save(inventory1);

        // operation #1
        Operation operation1 = new Operation();
        operation1.setSteamId(steamId1);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);

        OperationMeta operationMeta1 = new OperationMeta();
        operationMeta1.setResponseCount(3);
        operationMeta1.setItemCount(3);
        operationMeta1.setCreateOperationCount(3);
        operationMeta1.setUpdateOperationCount(0);
        operationMeta1.setDeleteOperationCount(0);
        operationMeta1.setNumSlots(10120);
        operation1.setMeta(operationMeta1);

        mongoTemplate.insert(operation1);

        // operation #2
        Operation operation2 = new Operation();
        operation2.setSteamId(steamId1);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(2);

        OperationMeta operationMeta2 = new OperationMeta();
        operationMeta2.setResponseCount(3);
        operationMeta2.setItemCount(3);
        operationMeta2.setCreateOperationCount(0);
        operationMeta2.setUpdateOperationCount(0);
        operationMeta2.setDeleteOperationCount(0);
        operationMeta2.setNumSlots(10120);
        operation2.setMeta(operationMeta2);

        mongoTemplate.insert(operation2);

        // operation #3
        Operation operation3 = new Operation();
        operation3.setSteamId(steamId1);
        operation3.setType(Operation.Type.U);
        operation3.setVersion(3);

        OperationMeta operationMeta3 = new OperationMeta();
        operationMeta3.setResponseCount(5);
        operationMeta3.setItemCount(5);
        operationMeta3.setCreateOperationCount(2);
        operationMeta3.setUpdateOperationCount(0);
        operationMeta3.setDeleteOperationCount(0);
        operationMeta3.setNumSlots(10120);
        operation3.setMeta(operationMeta3);

        mongoTemplate.insert(operation3);

        Long steamId2 = 100000000001L;
        Inventory inventory2 = new Inventory(steamId2);
        inventoryRepository.save(inventory2);

        // operation #4
        Operation operation4 = new Operation();
        operation4.setSteamId(steamId2);
        operation4.setType(Operation.Type.C);
        operation4.setVersion(1);

        OperationMeta operationMeta4 = new OperationMeta();
        operationMeta4.setResponseCount(10);
        operationMeta4.setItemCount(10);
        operationMeta4.setCreateOperationCount(10);
        operationMeta4.setUpdateOperationCount(0);
        operationMeta4.setDeleteOperationCount(0);
        operationMeta4.setNumSlots(10520);
        operation4.setMeta(operationMeta4);

        mongoTemplate.insert(operation4);
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.remove(new Query(), Inventory.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), Operation.class.getAnnotation(Document.class).value());
    }

    @Test
    void itShouldAggregateLatestOperationsByInventories() {
        // given
        Long steamId1 = 100000000000L;
        Long steamId2 = 100000000001L;

        Integer steamId1Version3 = 3;
        Integer steamId2Version1 = 1;

        // when
        List<Operation> expected = underTest.aggregateLatestByInventories(new ArrayList<>() {{
            add(steamId1);
            add(steamId2);
        }});

        // then
        assertThat(expected)
                .extracting("steamId")
                .contains(steamId1, steamId2)
                .hasSize(2);

        assertThat(expected)
                .extracting("version")
                .contains(steamId1Version3, steamId2Version1)
                .hasSize(2);
    }

    @Test
    void itShouldFindPage() {
        // given
        Long steamId1 = 100000000000L;

        Integer steamId1Version3 = 3;
        Integer steamId1Version2 = 2;
        Integer steamId1Version1 = 1;

        Inventory inventory = inventoryRepository.findById(steamId1).get();

        Pageable firstPageWithAllItems = PageRequest.of(0, 10);
        Pageable secondPageWithOneItem = PageRequest.of(1, 2);

        Sort sortBy = Sort.by(Sort.Direction.DESC, "version");

        // when
        Page<Operation> firstPage = underTest.findPage(inventory, firstPageWithAllItems, sortBy);
        Page<Operation> secondPage = underTest.findPage(inventory, secondPageWithOneItem, sortBy);

        // then
        assertThat(firstPage.getContent())
                .extracting("version")
                .contains(steamId1Version3, steamId1Version2, steamId1Version1)
                .hasSize(3);

        assertThat(secondPage.getContent())
                .extracting("version")
                .contains(steamId1Version1)
                .hasSize(1);
    }

    @Test
    void itShouldFindLatestOperation() {
        // given
        Long steamId1 = 100000000000L;
        Inventory inventory = inventoryRepository.findById(steamId1).get();

        // when
        Operation expected = underTest.findLatest(inventory);

        // then
        assertThat(expected.getSteamId()).isEqualTo(steamId1);
        assertThat(expected.getVersion()).isEqualTo(3);
        assertThat(expected.getType()).isEqualTo(Operation.Type.U);
    }

    @Test
    void itShouldFindOperationByVersion() {
        // given
        Long steamId1 = 100000000000L;
        Inventory inventory = inventoryRepository.findById(steamId1).get();

        // when
        Operation byVersion0 = underTest.findByVersion(inventory, 0);
        Operation byVersion1 = underTest.findByVersion(inventory, 1);
        Operation byVersion2 = underTest.findByVersion(inventory, 3);
        Operation byVersion3 = underTest.findByVersion(inventory, 10);

        // then
        assertThat(byVersion0).isNull();
        assertThat(byVersion1.getVersion()).isEqualTo(1);
        assertThat(byVersion2.getVersion()).isEqualTo(3);
        assertThat(byVersion3).isNull();
    }

    @Test
    void itShouldInsertOneOperation() {
        // given
        Long steamId1 = 100000000000L;

        Operation operation = new Operation();
        operation.setSteamId(steamId1);
        operation.setType(Operation.Type.C);
        operation.setVersion(5);
        operation.setMeta(new OperationMeta());

        // when
        Operation expected = underTest.insertOne(operation);

        // then
        assertThat(expected.getVersion()).isEqualTo(5);
    }

    @Test
    void itShouldUpdateMetaOfOperation() {
        // given
        Long steamId1 = 100000000000L;

        Operation operation = new Operation();
        operation.setSteamId(steamId1);
        operation.setType(Operation.Type.C);
        operation.setVersion(5);
        operation.setMeta(new OperationMeta());

        Operation savedOperation = underTest.insertOne(operation);

        OperationMeta meta = new OperationMeta();
        meta.setItemCount(1);
        meta.setResponseCount(2);
        meta.setCreateOperationCount(3);
        meta.setUpdateOperationCount(4);
        meta.setDeleteOperationCount(5);
        meta.setNumSlots(6);

        // when
        long updateCount = underTest.updateMeta(savedOperation, meta);

        // then
        assertThat(updateCount).isEqualTo(1);

        Operation updatedOperation = mongoTemplate.findById(savedOperation.getId(), Operation.class);
        OperationMeta updatedMeta = updatedOperation.getMeta();

        assertThat(updatedMeta.getItemCount()).isEqualTo(1);
        assertThat(updatedMeta.getResponseCount()).isEqualTo(2);
        assertThat(updatedMeta.getCreateOperationCount()).isEqualTo(3);
        assertThat(updatedMeta.getUpdateOperationCount()).isEqualTo(4);
        assertThat(updatedMeta.getDeleteOperationCount()).isEqualTo(5);
        assertThat(updatedMeta.getNumSlots()).isEqualTo(6);
    }

    @Test
    void itShouldRemoveAllOperationsByInventory() {
        // given
        Long steamId1 = 100000000000L;
        Long steamId2 = 100000000001L;
        Inventory inventory = inventoryRepository.findById(steamId1).get();

        // when
        long removeCount = underTest.removeAll(inventory);

        // then
        assertThat(removeCount).isEqualTo(3);

        List<Operation> otherOperations = mongoTemplate.findAll(Operation.class);
        assertThat(otherOperations)
                .extracting("steamId")
                .contains(steamId2)
                .hasSize(1);
    }
}
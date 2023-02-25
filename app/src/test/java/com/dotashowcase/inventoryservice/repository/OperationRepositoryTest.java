package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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

        Operation operation1 = new Operation();
        operation1.setSteamId(steamId1);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());
        mongoTemplate.insert(operation1);

        Operation operation2 = new Operation();
        operation2.setSteamId(steamId1);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(2);
        operation2.setMeta(new OperationMeta());
        mongoTemplate.insert(operation2);

        Operation operation3 = new Operation();
        operation3.setSteamId(steamId1);
        operation3.setType(Operation.Type.U);
        operation3.setVersion(3);
        operation3.setMeta(new OperationMeta());
        mongoTemplate.insert(operation3);

        Long steamId2 = 100000000001L;
        Inventory inventory2 = new Inventory(steamId2);
        inventoryRepository.save(inventory2);

        Operation operation4 = new Operation();
        operation4.setSteamId(steamId2);
        operation4.setType(Operation.Type.C);
        operation4.setVersion(1);
        operation4.setMeta(new OperationMeta());
        mongoTemplate.insert(operation4);
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.remove(new Query(), Inventory.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), Operation.class.getAnnotation(Document.class).value());
    }

    @Test
    void itShouldFindOperationsByInventories() {
        // given
        Long steamId1 = 100000000000L;
        Long steamId2 = 100000000001L;

        // when
        List<Operation> expected = underTest.findByInventories(new ArrayList<>() {{
            add(steamId1);
            add(steamId2);
        }});

        // then
        assertThat(expected)
                .extracting("steamId")
                .contains(steamId1, steamId2)
                .hasSize(4);
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
    void itShouldFindNLatestOperations() {
        // given
        Long steamId1 = 100000000000L;
        Inventory inventory = inventoryRepository.findById(steamId1).get();

        // when
        List<Operation> nLatest1 = underTest.findNLatest(inventory, 1);
        List<Operation> nLatest2 = underTest.findNLatest(inventory, 2);
        List<Operation> nLatest3 = underTest.findNLatest(inventory, 3);
        List<Operation> nLatest4 = underTest.findNLatest(inventory, 4);
        List<Operation> nLatest5 = underTest.findNLatest(inventory, -1);

        // then
        assertThat(nLatest1)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(1);

        assertThat(nLatest2)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(nLatest3)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(3);

        assertThat(nLatest4)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(3);

        assertThat(nLatest5)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(3);
    }

    @Test
    void itShouldFindOperationByVersion() {
        // given
        Long steamId1 = 100000000000L;
        Inventory inventory = inventoryRepository.findById(steamId1).get();

        // when
        Operation byVersion1 = underTest.findByVersion(inventory, 1);
        Operation byVersion2 = underTest.findByVersion(inventory, 3);
        Operation byVersion3 = underTest.findByVersion(inventory, 10);

        // then
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
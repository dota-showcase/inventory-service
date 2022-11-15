package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InventoryItemRepositoryTest {

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    InventoryItemDALRepository underTest;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        Long steamId1 = 100000000000L;
        Inventory inventory1 = new Inventory(steamId1);
        inventoryRepository.save(inventory1);

        // create three items - #100, #101, #102
        Operation operation1 = new Operation();
        operation1.setSteamId(steamId1);
        operation1.setType(Operation.Type.C);
        operation1.setVersion(1);
        operation1.setMeta(new OperationMeta());
        mongoTemplate.insert(operation1);

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
        inventoryItem1.setStyle((byte)1);
        inventoryItem1.setCustomName("inventory item #1");
        mongoTemplate.insert(inventoryItem1);

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
        inventoryItem2.setStyle((byte)1);
        inventoryItem2.setCustomName("inventory item #2");
        mongoTemplate.insert(inventoryItem2);

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
        inventoryItem3.setStyle((byte)1);
        inventoryItem3.setCustomName("inventory item #3");
        mongoTemplate.insert(inventoryItem3);

        // update item #101
        Operation operation2 = new Operation();
        operation2.setSteamId(steamId1);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(2);
        operation2.setMeta(new OperationMeta());
        mongoTemplate.insert(operation2);

        InventoryItem inventoryItem4 = new InventoryItem();
        inventoryItem4.setItemId(itemId2);
        inventoryItem4.setOperationId(operation2.getId());
        inventoryItem4.setSteamId(steamId1);
        inventoryItem4.setOriginalId(itemId2);
        inventoryItem4.setDefIndex(defIndex2);
        inventoryItem4.setLevel((byte)1);
        inventoryItem4.setInventoryToken(1L);
        inventoryItem4.setQuantity(1);
        inventoryItem4.setStyle((byte)1);
        inventoryItem4.setCustomName("inventory item #2 - update");
        mongoTemplate.insert(inventoryItem4);

        // remove item #100
        Operation operation3 = new Operation();
        operation2.setSteamId(steamId1);
        operation2.setType(Operation.Type.U);
        operation2.setVersion(3);
        operation2.setMeta(new OperationMeta());
        mongoTemplate.insert(operation3);

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(inventoryItem1.getId())),
                Update.update("_odId", operation3.getId()),
                InventoryItem.class
        );

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(inventoryItem1.getId())),
                Update.update("isActive", false),
                InventoryItem.class
        );

        Long steamId2 = 100000000001L;
        Inventory inventory2 = new Inventory(steamId2);
        inventoryRepository.save(inventory2);

        // create one item - #200 for user #2
        Operation operation21 = new Operation();
        operation21.setSteamId(steamId1);
        operation21.setType(Operation.Type.C);
        operation21.setVersion(1);
        operation21.setMeta(new OperationMeta());
        mongoTemplate.insert(operation21);

        Long itemId21 = 200L;
        Integer defIndex21 = 300;
        InventoryItem inventoryItem21 = new InventoryItem();
        inventoryItem21.setItemId(itemId21);
        inventoryItem21.setOperationId(operation21.getId());
        inventoryItem21.setSteamId(steamId2);
        inventoryItem21.setOriginalId(itemId21);
        inventoryItem21.setDefIndex(defIndex21);
        inventoryItem21.setLevel((byte)1);
        inventoryItem21.setInventoryToken(1L);
        inventoryItem21.setQuantity(1);
        inventoryItem21.setStyle((byte)1);
        inventoryItem21.setCustomName("inventory item #2-1");
        mongoTemplate.insert(inventoryItem21);
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.remove(new Query(), Inventory.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), Operation.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), InventoryItem.class.getAnnotation(Document.class).value());
    }

    @Test
    void itShouldFindAllInventoryItems() {
        // given
        Long steamId1 = 100000000000L;
        Inventory inventory = inventoryRepository.findById(steamId1).get();

        Query query = new Query();
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));
        query.addCriteria(Criteria.where("type").is(Operation.Type.C));

        Operation operation = mongoTemplate.findOne(query, Operation.class);

        Operation operationNotItems = new Operation();
        operationNotItems.setSteamId(100000000003L);
        operationNotItems.setType(Operation.Type.C);
        operationNotItems.setVersion(1);
        operationNotItems.setMeta(new OperationMeta());
        mongoTemplate.insert(operationNotItems);

        // when
        List<InventoryItem> itemsAll = underTest.findAll(inventory);
        List<InventoryItem> itemsAllEmpty = underTest.findAll(new Inventory(100000000003L));

        List<InventoryItem> itemsOperation = underTest.findAll(inventory, operation);
        List<InventoryItem> itemsOperationEmpty = underTest.findAll(inventory, operationNotItems);

        // then
        assertThat(itemsAll)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(3);

        assertThat(itemsAllEmpty)
                .hasSize(0);

        assertThat(itemsOperation)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(3);

        assertThat(itemsOperationEmpty)
                .hasSize(0);
    }

    @Test
    void itShouldInsertAllInventoryItems() {
        // given
        Long steamId1 = 100000000000L;
        Operation operation = new Operation();
        operation.setSteamId(steamId1);
        operation.setType(Operation.Type.U);
        operation.setVersion(4);
        operation.setMeta(new OperationMeta());
        mongoTemplate.insert(operation);

        Long itemId1 = 103L;
        Integer defIndex1 = 203;
        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setItemId(itemId1);
        inventoryItem1.setOperationId(operation.getId());
        inventoryItem1.setSteamId(steamId1);
        inventoryItem1.setOriginalId(itemId1);
        inventoryItem1.setDefIndex(defIndex1);
        inventoryItem1.setLevel((byte)1);
        inventoryItem1.setInventoryToken(1L);
        inventoryItem1.setQuantity(1);
        inventoryItem1.setStyle((byte)1);
        inventoryItem1.setCustomName("inventory item #4");

        Long itemId2 = 104L;
        Integer defIndex2 = 204;
        InventoryItem inventoryItem2 = new InventoryItem();
        inventoryItem2.setItemId(itemId2);
        inventoryItem2.setOperationId(operation.getId());
        inventoryItem2.setSteamId(steamId1);
        inventoryItem2.setOriginalId(itemId2);
        inventoryItem2.setDefIndex(defIndex2);
        inventoryItem2.setLevel((byte)1);
        inventoryItem2.setInventoryToken(1L);
        inventoryItem2.setQuantity(1);
        inventoryItem2.setStyle((byte)1);
        inventoryItem2.setCustomName("inventory item #5");

        // when
        List<InventoryItem> items = underTest.insertAll(List.of(inventoryItem1, inventoryItem2));

        // then
        assertThat(items)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);
    }

    @Test
    void itShouldUpdateAllInventoryItems() {
        // given
        Long itemId1 = 101L;
        Long itemId2 = 102L; // exist x2
        Long itemId3 = 103L; // not exists

        List<InventoryItem> inventoryItems = mongoTemplate.find(
                Query.query(Criteria.where("itemId").in(List.of(itemId1, itemId2, itemId3))),
                InventoryItem.class
        );

        Set<ObjectId> ids = inventoryItems.stream()
                .map(InventoryItem::getId)
                .collect(Collectors.toSet());

        // when
        long updateCount = underTest.updateAll(
                ids,
                new ArrayList<>(List.of(
                        new AbstractMap.SimpleImmutableEntry<>("_isA", false),
                        new AbstractMap.SimpleImmutableEntry<>("qnt", 2)
                ))
        );

        long updateCountNotExistProp = underTest.updateAll(
                ids,
                new ArrayList<>(List.of(
                        new AbstractMap.SimpleImmutableEntry<>("_abc101", false)
                ))
        );

        long updateCountNotExistItem = underTest.updateAll(
                Set.of(new ObjectId("631fb4e3111ee61f2d109b4c")),
                new ArrayList<>(List.of(
                        new AbstractMap.SimpleImmutableEntry<>("_isA", false)
                ))
        );

        // then
        assertThat(updateCount).isEqualTo(3L);
        assertThat(updateCountNotExistProp).isEqualTo(0L); // TODO: fix
        assertThat(updateCountNotExistItem).isEqualTo(0L);
    }

    @Test
    void itShouldRemoveAllInventoryItems() {
        // given
        Long steamId1 = 100000000000L;
        Long steamId2 = 100000000001L;
        Inventory inventory = inventoryRepository.findById(steamId1).get();

        // when
        long removeCount = underTest.removeAll(inventory);

        // then
        assertThat(removeCount).isEqualTo(4L);

        List<InventoryItem> inventoryItems = mongoTemplate.findAll(InventoryItem.class);
        assertThat(inventoryItems)
                .extracting("steamId")
                .contains(steamId2)
                .hasSize(1);
    }
}
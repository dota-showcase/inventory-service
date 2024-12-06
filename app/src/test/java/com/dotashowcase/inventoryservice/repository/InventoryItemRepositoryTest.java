package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.config.AppConstant;
import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.http.filter.InventoryItemFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.ItemAttribute;
import com.dotashowcase.inventoryservice.model.embedded.ItemEquipment;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(MongoTestConfig.class)
class InventoryItemRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryItemDALRepository underTest;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        // #1 (4 total) - 100R 101A(101H) 102A
        // #2 (1 total) - 200A
        // A - active, H - hidden, R - removed (hidden)

        Long steamId1 = 100000000000L;
        Inventory inventory1 = new Inventory(steamId1);
        inventoryRepository.save(inventory1);

        // create three items - #100, #101, #102
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
        inventoryItem1.setInventoryPosition(1);
        inventoryItem1.setQuantity(1);
        inventoryItem1.setQuality((byte)1);
        inventoryItem1.setStyle((byte)1);
        inventoryItem1.setIsTradable(true);
        inventoryItem1.setIsCraftable(true);
        inventoryItem1.setCustomName("inventory item #1");
        inventoryItem1.setItemEquipment(List.of(new ItemEquipment(1, 1)));
        inventoryItem1.setAttributes(List.of(new ItemAttribute(7, "1", 1.20, null)));
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
        inventoryItem2.setInventoryToken(48L);
        inventoryItem2.setInventoryPosition(AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE);
        inventoryItem2.setQuantity(1);
        inventoryItem2.setQuality((byte)2);
        inventoryItem2.setStyle((byte)1);
        inventoryItem2.setIsTradable(true);
        inventoryItem2.setIsCraftable(true);
        inventoryItem2.setCustomName("inventory item #2");
        inventoryItem2.setItemEquipment(List.of(new ItemEquipment(1, 2)));
        inventoryItem2.setAttributes(List.of(new ItemAttribute(8, "2", 2.20, null)));
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
        inventoryItem3.setInventoryToken(49L);
        inventoryItem3.setInventoryPosition(AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE + 1);
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
        inventoryItem4.setInventoryToken(48L);
        inventoryItem4.setInventoryPosition(AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE);
        inventoryItem4.setQuantity(1);
        inventoryItem4.setQuality((byte)2);
        inventoryItem4.setStyle((byte)1);
        inventoryItem4.setIsTradable(true);
        inventoryItem4.setIsCraftable(true);
        inventoryItem4.setCustomName("inventory item #2 - update");
        inventoryItem4.setItemEquipment(List.of(new ItemEquipment(1, 2)));
        inventoryItem4.setAttributes(List.of(new ItemAttribute(8, "2", 2.20, null)));
        mongoTemplate.insert(inventoryItem4);

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(inventoryItem2.getId())),
                Update.update("_isA", false),
                InventoryItem.class
        );

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
                Update.update("_isA", false),
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
        inventoryItem21.setInventoryPosition(1);
        inventoryItem21.setQuantity(1);
        inventoryItem21.setStyle((byte)1);
        inventoryItem21.setQuality((byte)4);
        inventoryItem21.setIsTradable(true);
        inventoryItem21.setIsCraftable(true);
        inventoryItem21.setCustomName("inventory item #2-1");
        inventoryItem21.setItemEquipment(List.of(new ItemEquipment(1, 2)));
        inventoryItem2.setAttributes(List.of(new ItemAttribute(8, "2", 2.20, null)));
        mongoTemplate.insert(inventoryItem21);
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.remove(new Query(), Inventory.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), Operation.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), InventoryItem.class.getAnnotation(Document.class).value());
    }

    @Test
    void itShouldSearchAllInventoryItems() {
        // given
        Long steamId1 = 100000000000L;
        Inventory inventory = inventoryRepository.findById(steamId1).get();

        // add one more item - 103
        Operation operation = new Operation();
        operation.setSteamId(steamId1);
        operation.setType(Operation.Type.U);
        operation.setVersion(4);
        operation.setMeta(new OperationMeta());
        mongoTemplate.insert(operation);

        Long itemId1 = 103L;
        Integer defIndex1 = 203;
        InventoryItem inventoryItem4 = new InventoryItem();
        inventoryItem4.setItemId(itemId1);
        inventoryItem4.setOperationId(operation.getId());
        inventoryItem4.setSteamId(steamId1);
        inventoryItem4.setOriginalId(itemId1);
        inventoryItem4.setDefIndex(defIndex1);
        inventoryItem4.setQuality((byte)3);
        inventoryItem4.setStyle((byte)1);
        inventoryItem4.setIsTradable(true);
        inventoryItem4.setIsCraftable(true);
        inventoryItem4.setCustomName("inventory item #4");
        inventoryItem4.setItemEquipment(List.of(new ItemEquipment(1, 3)));
        inventoryItem4.setAttributes(List.of(new ItemAttribute(7, "1", 1.20, null)));
        mongoTemplate.insert(inventoryItem4);

        Pageable firstPageWithAllItems = PageRequest.of(0, 4);
        Pageable secondPageWithOneItem = PageRequest.of(1, 1);
        Pageable thirdPageWithZeroItem = PageRequest.of(2, 1);

        InventoryItemFilter filterItemIds = InventoryItemFilter.builder()
                .itemIds(List.of(100L, 101L, 103L, 300L, 999L))
                .build();

        // 200R, 201U, 203A - inventory #1; 300A - inventory #2; 999 - n/a
        InventoryItemFilter filterDefIndexes = InventoryItemFilter.builder()
                .defIndexes(List.of(200, 201, 203, 300, 999))
                .build();

        // 1R, 2U, 3A - inventory #1; 4A - inventory #2; 99 - n/a
        InventoryItemFilter filterQualities = InventoryItemFilter.builder()
                .qualities(List.of((byte)1, (byte)2, (byte)3, (byte)4, (byte)99))
                .build();

        InventoryItemFilter filterIsTradable = InventoryItemFilter.builder()
                .isTradable(true)
                .build();

        InventoryItemFilter filterIsCraftable = InventoryItemFilter.builder()
                .isCraftable(true)
                .build();

        InventoryItemFilter filterIsEquipped = InventoryItemFilter.builder()
                .isEquipped(true)
                .build();

        InventoryItemFilter filterHasAttribute = InventoryItemFilter.builder()
                .hasAttribute(true)
                .build();

        InventoryItemFilter filterAll = InventoryItemFilter.builder()
                .defIndexes(List.of(200, 201, 203, 300, 999))
                .qualities(List.of((byte)1, (byte)2, (byte)3, (byte)4, (byte)99))
                .isTradable(true)
                .isCraftable(true)
                .isEquipped(true)
                .hasAttribute(true)
                .build();

        Sort byDefIndex = Sort.by(Sort.Direction.ASC, "defIndex");
        Sort byDefIndexDesc = Sort.by(Sort.Direction.DESC, "defIndex");

        // when #0
        Page<InventoryItem> twoItemsByItemId = underTest
                .searchAll(inventory, firstPageWithAllItems, filterItemIds, byDefIndex);
        List<InventoryItem> twoItemsByItemId2 = underTest.searchAll(inventory, filterItemIds, byDefIndex);

        // when #1
        Page<InventoryItem> twoItemsByDefIndex = underTest
                .searchAll(inventory, firstPageWithAllItems, filterDefIndexes, byDefIndex);
        List<InventoryItem> twoItemsByDefIndex2 = underTest.searchAll(inventory, filterDefIndexes, byDefIndex);

        // when #2
        Page<InventoryItem> twoItemsByQualities = underTest
                .searchAll(inventory, firstPageWithAllItems, filterQualities, byDefIndex);
        List<InventoryItem> twoItemsByQualities2 = underTest.searchAll(inventory, filterQualities, byDefIndex);

        // when #3
        Page<InventoryItem> twoItemsByTradable = underTest
                .searchAll(inventory, firstPageWithAllItems, filterIsTradable, byDefIndex);
        List<InventoryItem> twoItemsByTradable2 = underTest.searchAll(inventory, filterIsTradable, byDefIndex);

        // when #4
        Page<InventoryItem> twoItemsByCraftable = underTest
                .searchAll(inventory, firstPageWithAllItems, filterIsCraftable, byDefIndex);
        List<InventoryItem> twoItemsByCraftable2 = underTest.searchAll(inventory, filterIsCraftable, byDefIndex);

        // when #5
        Page<InventoryItem> twoItemsByEquipped = underTest
                .searchAll(inventory, firstPageWithAllItems, filterIsEquipped, byDefIndex);
        List<InventoryItem> twoItemsByEquipped2 = underTest.searchAll(inventory, filterIsEquipped, byDefIndex);

        // when #6
        Page<InventoryItem> twoItemsByAttribute = underTest
                .searchAll(inventory, firstPageWithAllItems, filterHasAttribute, byDefIndex);
        List<InventoryItem> twoItemsByAttribute2 = underTest.searchAll(inventory, filterHasAttribute, byDefIndex);

        // when #7
        Page<InventoryItem> twoItemsByAll = underTest
                .searchAll(inventory, firstPageWithAllItems, filterAll, byDefIndex);
        List<InventoryItem> twoItemsByAll2 = underTest.searchAll(inventory, filterAll, byDefIndex);

        // when #8
        Page<InventoryItem> secondPageOneItemByAll = underTest
                .searchAll(inventory, secondPageWithOneItem, filterAll, byDefIndex);

        Page<InventoryItem> zeroItemByAll = underTest
                .searchAll(inventory, thirdPageWithZeroItem, filterAll, byDefIndex);

        // when #9
        Page<InventoryItem> twoItemsByAllDefindexDesc = underTest
                .searchAll(inventory, firstPageWithAllItems, filterAll, byDefIndexDesc);

        // then #0
        assertThat(twoItemsByItemId.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByItemId.getContent())
                .extracting("itemId")
                .containsSequence(101L, 103L);

        assertThat(twoItemsByItemId2)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByItemId2)
                .extracting("itemId")
                .containsSequence(101L, 103L);

        // then #1
        assertThat(twoItemsByDefIndex.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByDefIndex.getContent())
                .extracting("defIndex")
                .containsSequence(201, 203);

        assertThat(twoItemsByDefIndex2)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByDefIndex2)
                .extracting("defIndex")
                .containsSequence(201, 203);

        // then #2
        assertThat(twoItemsByQualities.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByQualities.getContent())
                .extracting("quality")
                .containsSequence((byte)2, (byte)3);

        assertThat(twoItemsByQualities2)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByQualities2)
                .extracting("quality")
                .containsSequence((byte)2, (byte)3);

        // then #3
        assertThat(twoItemsByTradable.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByTradable.getContent())
                .extracting("isTradable")
                .contains(true)
                .hasSize(2);

        assertThat(twoItemsByTradable2)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByTradable2)
                .extracting("isTradable")
                .contains(true)
                .hasSize(2);

        // then #4
        assertThat(twoItemsByCraftable.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByCraftable.getContent())
                .extracting("isCraftable")
                .contains(true)
                .hasSize(2);

        assertThat(twoItemsByCraftable2)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByCraftable2)
                .extracting("isCraftable")
                .contains(true)
                .hasSize(2);

        // then #5
        assertThat(twoItemsByEquipped.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByEquipped.getContent())
                .extracting("itemEquipment")
                .hasSize(2);

        assertThat(twoItemsByEquipped2)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByEquipped2)
                .extracting("itemEquipment")
                .hasSize(2);

        // then #6
        assertThat(twoItemsByAttribute.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByAttribute.getContent())
                .extracting("attributes")
                .hasSize(2);

        assertThat(twoItemsByAttribute2)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByAttribute2)
                .extracting("attributes")
                .hasSize(2);

        // then #7
        assertThat(twoItemsByAll.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByAll.getContent())
                .extracting("defIndex")
                .containsSequence(201, 203);

        assertThat(twoItemsByAll2)
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByAll2)
                .extracting("defIndex")
                .containsSequence(201, 203);

        // then #8
        assertThat(secondPageOneItemByAll.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(1);

        assertThat(secondPageOneItemByAll.getContent())
                .extracting("defIndex")
                .containsSequence(203);

        assertThat(zeroItemByAll.getContent())
                .hasSize(0);

        // then #9
        assertThat(twoItemsByAllDefindexDesc.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(2);

        assertThat(twoItemsByAllDefindexDesc.getContent())
                .extracting("defIndex")
                .containsSequence(201, 203);
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
                .hasSize(2);

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
    void itShouldFindPositionedPage() {
        // given
        Long steamId1 = 100000000000L;
        Inventory inventory = inventoryRepository.findById(steamId1).get();

        Query query = new Query();
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));
        query.addCriteria(Criteria.where("type").is(Operation.Type.C));

        Operation operation = mongoTemplate.findOne(query, Operation.class);

        inventory.setLatestOperation(operation);

        // when
        Page<InventoryItem> firstItemPage = underTest.findPositionedPage(inventory, 1);
        Page<InventoryItem> secondItemPage = underTest.findPositionedPage(inventory, 2);
        Page<InventoryItem> thirdItemPage = underTest.findPositionedPage(inventory, 3);

        // then
        assertThat(firstItemPage.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(1);

        assertThat(firstItemPage.getContent())
                .extracting("defIndex")
                .containsSequence(201);

        assertThat(secondItemPage.getContent())
                .extracting("steamId")
                .contains(steamId1)
                .hasSize(1);

        assertThat(secondItemPage.getContent())
                .extracting("defIndex")
                .containsSequence(202);

        assertThat(thirdItemPage.getContent())
                .hasSize(0);
    }

    @Test
    void itShouldFindPluckedField() {
        // given
        Long steamId1 = 100000000000L;
        Inventory inventory = inventoryRepository.findById(steamId1).get();

        // when
        List<Integer> itemDefIndexes = underTest.findPluckedField(inventory, "defIndex");

        // then
        assertThat(itemDefIndexes)
                .hasSize(2);

        assertThat(itemDefIndexes)
                .containsSequence(201, 202);
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
        assertThat(updateCountNotExistProp).isEqualTo(0L);
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
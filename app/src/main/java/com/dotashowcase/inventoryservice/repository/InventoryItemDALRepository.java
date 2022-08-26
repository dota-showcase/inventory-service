package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class InventoryItemDALRepository implements InventoryItemDAL {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<InventoryItem> findAll(Inventory inventory, Pageable pageable) {
        Query query = new Query();
        query.with(pageable);
        setDefaultParams(query, inventory);
        query.addCriteria(Criteria.where("isA").is(true));

        return PageableExecutionUtils.getPage(
                mongoTemplate.find(query, InventoryItem.class),
                pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), InventoryItem.class)
        );
    }

    @Override
    public Map<Long, InventoryItem> findAll(Inventory inventory) {
        Query query = new Query();
        setDefaultParams(query, inventory);
        query.addCriteria(Criteria.where("isA").is(true));

        List<InventoryItem> result = mongoTemplate.find(query, InventoryItem.class);

        return result.stream()
                .collect(Collectors.toMap(InventoryItem::getItemId, Function.identity()));
    }

    @Override
    public Map<Long, InventoryItem> findAll(Inventory inventory, HistoryAction action) {
        Query query = new Query();
        setDefaultParams(query, inventory);
        query.addCriteria(Criteria.where("hId").is(action.getId()));

        List<InventoryItem> result = mongoTemplate.find(query, InventoryItem.class);

        return result.stream()
                .collect(Collectors.toMap(InventoryItem::getItemId, Function.identity()));
    }

    @Override
    public List<InventoryItem> insertAll(List<InventoryItem> inventoryItems) {
        return (List<InventoryItem>) mongoTemplate.insertAll(inventoryItems);
    }

    @Override
    public long updateAll(Set<ObjectId> ids, AbstractMap.SimpleImmutableEntry<String, Object> updateEntry) {
        Criteria criteria = Criteria.where("_id").in(ids);
        Query query = new Query(criteria);
        Update update = new Update();
        update.set(updateEntry.getKey(), updateEntry.getValue());

        return mongoTemplate.updateMulti(query, update, InventoryItem.class).getModifiedCount();
    }

    @Override
    public long removeAll(Inventory inventory) {
        Query query = new Query();
        this.setDefaultParams(query, inventory);

        return mongoTemplate.remove(query, InventoryItem.class).getDeletedCount();
    }

//    @Override
//    public long removeAll(Inventory inventory, Set<Long> ids) {
//        Query query = new Query();
//        this.setDefaultParams(query, inventory);
//
//        query.addCriteria(Criteria.where("_id").in(ids));
//
//        return mongoTemplate.remove(query, InventoryItem.class).getDeletedCount();
//    }

    private void setDefaultParams(Query query, Inventory inventory) {
        query.fields().exclude("steamId");
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));
    }
}

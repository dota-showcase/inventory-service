package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.http.filter.InventoryItemFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InventoryItemDALRepository implements InventoryItemDAL {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<InventoryItem> searchAll(
            Inventory inventory,
            Pageable pageable,
            InventoryItemFilter filter,
            Sort sort
    ) {
        Query query = new Query();
        query.with(pageable);
        setDefaultParams(query, inventory);
        query.addCriteria(Criteria.where("_isA").is(true));
        applyFilter(query, filter);
        if (sort != null) {
            query.with(sort);
        }

        return PageableExecutionUtils.getPage(
                mongoTemplate.find(query, InventoryItem.class),
                pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), InventoryItem.class)
        );
    }

    @Override
    public List<InventoryItem> searchAll(Inventory inventory, InventoryItemFilter filter, Sort sort) {
        Query query = new Query();
        setDefaultParams(query, inventory);
        query.addCriteria(Criteria.where("_isA").is(true));
        applyFilter(query, filter);
        if (sort != null) {
            query.with(sort);
        }

       return mongoTemplate.find(query, InventoryItem.class);
    }

    @Override
    public List<InventoryItem> findAll(Inventory inventory) {
        Query query = new Query();
        setDefaultParams(query, inventory);
        query.addCriteria(Criteria.where("_isA").is(true));

        return mongoTemplate.find(query, InventoryItem.class);
    }

    @Override
    public List<InventoryItem> findAll(Inventory inventory, Operation operation) {
        Query query = new Query();
        setDefaultParams(query, inventory);
        query.addCriteria(Criteria.where("_oId").is(operation.getId()));

        return mongoTemplate.find(query, InventoryItem.class);
    }

    @Override
    public List<InventoryItem> insertAll(List<InventoryItem> inventoryItems) {
        return (List<InventoryItem>) mongoTemplate.insertAll(inventoryItems);
    }

    @Override
    public long updateAll(Set<ObjectId> ids, List<AbstractMap.SimpleImmutableEntry<String, Object>> updateEntry) {
        Update update = new Update();

        int count = 0;
        for (AbstractMap.SimpleImmutableEntry<String, Object> entry : updateEntry) {
            String key = entry.getKey();
            if (InventoryItem.fillable.contains(key)) {
                update.set(key, entry.getValue());
                ++count;
            }
        }

        if (count == 0) {
            return 0L;
        }

        Criteria criteria = Criteria.where("_id").in(ids);
        Query query = new Query(criteria);

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
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));
    }

    private void applyFilter(Query query, InventoryItemFilter filter) {
        // filter by defIndex
        if (filter.hasDefIndexes()) {
            List<Integer> defIndexes = filter.getDefIndexes();

            if (defIndexes.size() == 1) {
                query.addCriteria(Criteria.where("dIdx").is(defIndexes.get(0)));
            } else {
                query.addCriteria(Criteria.where("dIdx").in(defIndexes));
            }
        }

        // filter by quality
        if (filter.hasQualities()) {
            List<Byte> qualities = filter.getQualities();

            if (qualities.size() == 1) {
                query.addCriteria(Criteria.where("qlt").is(qualities.get(0)));
            } else {
                query.addCriteria(Criteria.where("qlt").in(qualities));
            }
        }

        // filter by isTradable
        if (filter.getIsTradable() != null) {
            query.addCriteria(Criteria.where("isTr").is(filter.getIsTradable()));
        }

        // filter by isCraftable
        if (filter.getIsCraftable() != null) {
            query.addCriteria(Criteria.where("isCr").is(filter.getIsCraftable()));
        }

        // filter by itemEquipment
        if (filter.getIsEquipped() != null) {
            if (filter.getIsEquipped()) {
                query.addCriteria(Criteria.where("equips").ne(null));
            } else {
                query.addCriteria(Criteria.where("equips").isNull());
            }
        }

        // filter by attributes
        if (filter.getHasAttribute() != null) {
            if (filter.getHasAttribute()) {
                query.addCriteria(Criteria.where("attrs").ne(null));
            } else {
                query.addCriteria(Criteria.where("attrs").isNull());
            }
        }
    }
}

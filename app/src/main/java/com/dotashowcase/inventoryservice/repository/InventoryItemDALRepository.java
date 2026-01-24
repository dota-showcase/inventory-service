package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.config.AppConstant;
import com.dotashowcase.inventoryservice.http.filter.InventoryItemChangeFilter;
import com.dotashowcase.inventoryservice.http.filter.InventoryItemFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.service.type.ChangeType;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.addFields;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.IndexOfArray.arrayOf;

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
        List<Criteria> defaultCriteria = getDefaultCriteria(inventory);
        List<Criteria> filterCriteria = getFilterCriteria(filter);

        // main query
        List<AggregationOperation> operations = new ArrayList<>();

        defaultCriteria.forEach(criteria -> operations.add(Aggregation.match(criteria)));
        operations.add(Aggregation.match(Criteria.where("_isA").is(true)));
        filterCriteria.forEach(criteria -> operations.add(Aggregation.match(criteria)));

        // main query pagination
        operations.add(Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()));
        operations.add(Aggregation.limit(pageable.getPageSize()));

        // main query sort
        if (sort == null) {
            if (filter.hasDefIndexes()) {
                operations.add(addFields()
                        .addField("defIndexSort").withValue(arrayOf(filter.getDefIndexes()).indexOf("$dIdx"))
                        .build()
                );

                operations.add(Aggregation.sort(Sort.by(Sort.Direction.ASC, "defIndexSort")));
            }
        } else {
            operations.add(Aggregation.sort(sort));
        }

        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<InventoryItem> results = mongoTemplate.aggregate(
                aggregation,
                InventoryItem.class.getAnnotation(Document.class).value(),
                InventoryItem.class
        );

        // count query
        Query countQuery = new Query();
        defaultCriteria.forEach(countQuery::addCriteria);

        countQuery.addCriteria(Criteria.where("_isA").is(true));

        filterCriteria.forEach(countQuery::addCriteria);

        return PageableExecutionUtils.getPage(
                results.getMappedResults(),
                pageable,
                () -> mongoTemplate.count(countQuery.limit(-1).skip(-1), InventoryItem.class)
        );
    }

    @Override
    public List<InventoryItem> searchAll(Inventory inventory, InventoryItemFilter filter, Sort sort) {
        List<Criteria> defaultCriteria = getDefaultCriteria(inventory);
        List<Criteria> filterCriteria = getFilterCriteria(filter);

        List<AggregationOperation> operations = new ArrayList<>();

        defaultCriteria.forEach(criteria -> operations.add(Aggregation.match(criteria)));
        operations.add(Aggregation.match(Criteria.where("_isA").is(true)));
        filterCriteria.forEach(criteria -> operations.add(Aggregation.match(criteria)));

        // sort
        if (sort == null) {
            if (filter.hasDefIndexes()) {
                operations.add(addFields()
                        .addField("defIndexSort").withValue(arrayOf(filter.getDefIndexes()).indexOf("$dIdx"))
                        .build()
                );

                operations.add(Aggregation.sort(Sort.by(Sort.Direction.ASC, "defIndexSort")));
            }
        } else {
            operations.add(Aggregation.sort(sort));
        }

        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<InventoryItem> results = mongoTemplate.aggregate(
                aggregation,
                InventoryItem.class.getAnnotation(Document.class).value(),
                InventoryItem.class
        );

        return results.getMappedResults();
    }

    @Override
    public List<InventoryItem> findAll(Inventory inventory) {
        Query query = new Query();

        List<Criteria> defaultCriteria = getDefaultCriteria(inventory);
        defaultCriteria.forEach(query::addCriteria);

        query.addCriteria(Criteria.where("_isA").is(true));

        return mongoTemplate.find(query, InventoryItem.class);
    }

    @Override
    public List<InventoryItem> findAll(
            Inventory inventory,
            Operation operation,
            ChangeType type,
            InventoryItemChangeFilter filter
    ) {
        Query query = new Query();

        List<Criteria> defaultCriteria = getDefaultCriteria(inventory);
        defaultCriteria.forEach(query::addCriteria);

        switch (type) {
            case update -> {
                query.addCriteria(Criteria.where("_oId").is(operation.getId()));
                query.addCriteria(Criteria.where("_oT").is(Operation.Type.U));
                query.addCriteria(Criteria.where("_odId").is(null));
            }
            case delete -> {
                query.addCriteria(Criteria.where("_odId").is(operation.getId()));
            }
            case null, default -> {
                query.addCriteria(Criteria.where("_oId").is(operation.getId()));
                query.addCriteria(Criteria.where("_oT").is(Operation.Type.C));
                query.addCriteria(Criteria.where("_odId").is(null));
            }
        }

        Integer lim = filter.getLim();
        if (lim != null && lim > 0) {
            query.limit(lim);
        }

        return mongoTemplate.find(query, InventoryItem.class);
    }

    @Override
    public Page<InventoryItem> findPositionedPage(Inventory inventory, int page) {
        Query query = new Query();

        List<Criteria> defaultCriteria = getDefaultCriteria(inventory);
        defaultCriteria.forEach(query::addCriteria);

        query.addCriteria(Criteria.where("_isA").is(true));

        // page #1 - [1, 13)
        // page #2 - [13, 25)
        // page #3 - [25, 37)
        int fromPosition = ((page - 1) * AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE) + 1;
        int toPosition = fromPosition + AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE;

        query.addCriteria(Criteria.where("pos").gte(fromPosition).lt(toPosition));
        query.with(Sort.by(Sort.Direction.ASC, "pos"));

        // (page - 1) - to make compatible with PageMapper and config 'one-indexed-parameters'
        Pageable pageable = PageRequest.of(page - 1, AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE);

        return new PageImpl<>(
                mongoTemplate.find(query, InventoryItem.class),
                pageable,
                inventory.getLatestOperation().getMeta().getNumSlots()
        );
    }

    @Override
    public List<Integer> findPluckedField(Inventory inventory, String fieldName) {
        Query query = new Query();

        List<Criteria> defaultCriteria = getDefaultCriteria(inventory);
        defaultCriteria.forEach(query::addCriteria);

        query.addCriteria(Criteria.where("_isA").is(true));
        query.with(Sort.by(Sort.Direction.ASC, "dIdx"));

        return mongoTemplate.findDistinct(query, fieldName, InventoryItem.class, Integer.class);
    }

    @Override
    public List<InventoryItem> insertAll(List<InventoryItem> inventoryItems) {
        return (List<InventoryItem>) mongoTemplate.insertAll(inventoryItems);
    }

    @Override
    public long updateAll(Set<ObjectId> ids, List<AbstractMap.SimpleImmutableEntry<String, Object>> updateEntry) {
        if (ids.isEmpty()) {
            return 0L;
        }

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

        List<Criteria> defaultCriteria = getDefaultCriteria(inventory);
        defaultCriteria.forEach(query::addCriteria);

        return mongoTemplate.remove(query, InventoryItem.class).getDeletedCount();
    }

    private List<Criteria> getDefaultCriteria(Inventory inventory) {
        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(Criteria.where("steamId").is(inventory.getSteamId()));

        return criteriaList;
    }

    private List<Criteria> getFilterCriteria(InventoryItemFilter filter) {
        List<Criteria> criteriaList = new ArrayList<>();

        // filter by itemId
        if (filter.hasItemIds()) {
            List<Long> itemIds = filter.getItemIds();

            if (itemIds.size() == 1) {
                criteriaList.add(Criteria.where("itemId").is(itemIds.getFirst()));
            } else {
                criteriaList.add(Criteria.where("itemId").in(itemIds));
            }
        }

        // filter by defIndex
        if (filter.hasDefIndexes()) {
            List<Integer> defIndexes = filter.getDefIndexes();

            if (defIndexes.size() == 1) {
                criteriaList.add(Criteria.where("dIdx").is(defIndexes.getFirst()));
            } else {
                criteriaList.add(Criteria.where("dIdx").in(defIndexes));
            }
        }

        // filter by quality
        if (filter.hasQualities()) {
            List<Byte> qualities = filter.getQualities();

            if (qualities.size() == 1) {
                criteriaList.add(Criteria.where("qlt").is(qualities.getFirst()));
            } else {
                criteriaList.add(Criteria.where("qlt").in(qualities));
            }
        }

        // filter by isTradable
        if (filter.getIsTradable() != null) {
            criteriaList.add(Criteria.where("isTr").is(filter.getIsTradable()));
        }

        // filter by isCraftable
        if (filter.getIsCraftable() != null) {
            criteriaList.add(Criteria.where("isCr").is(filter.getIsCraftable()));
        }

        // filter by itemEquipment
        if (filter.getIsEquipped() != null) {
            if (filter.getIsEquipped()) {
                criteriaList.add(Criteria.where("equips").ne(null));
            } else {
                criteriaList.add(Criteria.where("equips").isNull());
            }
        }

        // filter by attributes
        if (filter.getHasAttribute() != null) {
            if (filter.getHasAttribute()) {
                criteriaList.add(Criteria.where("attrs").ne(null));
            } else {
                criteriaList.add(Criteria.where("attrs").isNull());
            }
        }

        return criteriaList;
    }
}

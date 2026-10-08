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
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.addFields;

@Repository
public class InventoryItemDALRepository implements InventoryItemDAL {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public InventoryItemDALRepository(MongoTemplate mongoTemplate) {
        Assert.notNull(mongoTemplate, "MongoTemplate must not be null!");
        this.mongoTemplate = mongoTemplate;
    }

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

        // main query sort
        if (sort == null) {
            // default - inventory position
            sort = Sort.by(Sort.Direction.ASC, "pos");

            if (filter.hasDefIndexes()) {
                operations.add(getDefIndexSortField(filter));

                sort = Sort.by(Sort.Direction.ASC, "defIndexSort").and(sort);
            }
        } else {
            sort = getMappedSort(sort);
        }

        // _id tie-breaker - stable pages
        if (sort.getOrderFor("_id") == null) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "_id"));
        }

        operations.add(Aggregation.sort(sort));

        // main query pagination
        operations.add(Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()));
        operations.add(Aggregation.limit(pageable.getPageSize()));

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
                operations.add(getDefIndexSortField(filter));

                operations.add(Aggregation.sort(Sort.by(Sort.Direction.ASC, "defIndexSort")));
            }
        } else {
            operations.add(Aggregation.sort(getMappedSort(sort)));
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
        int pageSize = AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE;
        long totalSlots = inventory.getLatestOperation().getMeta().getNumSlots();

        int maxPage = (int) Math.ceil((double) totalSlots / pageSize);

        List<Criteria> criteria = getDefaultCriteria(inventory);
        criteria.add(Criteria.where("_isA").is(true));

        // page #1 - [1, 49), page #2 - [49, 97), ...
        // first item from requested page - skips empty pages in one query
        Query firstItemQuery = new Query();
        criteria.forEach(firstItemQuery::addCriteria);
        firstItemQuery.addCriteria(Criteria.where("pos").gte((page - 1) * pageSize + 1));
        firstItemQuery.with(Sort.by(Sort.Direction.ASC, "pos"));

        InventoryItem firstItem = mongoTemplate.findOne(firstItemQuery, InventoryItem.class);

        // no items found until last page
        if (firstItem == null || firstItem.getInventoryPosition() > maxPage * pageSize) {
            Pageable lastPageable = PageRequest.of(Math.max(0, maxPage - 1), pageSize);

            return new PageImpl<>(
                    List.of(),
                    lastPageable,
                    totalSlots
            );
        }

        int currentPage = (firstItem.getInventoryPosition() - 1) / pageSize + 1;
        int fromPosition = (currentPage - 1) * pageSize + 1;
        int toPosition = fromPosition + pageSize;

        Query pageQuery = new Query();
        criteria.forEach(pageQuery::addCriteria);
        pageQuery.addCriteria(Criteria.where("pos").gte(fromPosition).lt(toPosition));
        pageQuery.with(Sort.by(Sort.Direction.ASC, "pos"));

        List<InventoryItem> items = mongoTemplate.find(pageQuery, InventoryItem.class);

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);

        return new PageImpl<>(
                items,
                pageable,
                totalSlots
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
            if (InventoryItem.FILLABLE.contains(key)) {
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

    // api names -> stored names, e.g. defIndex -> dIdx
    private Sort getMappedSort(Sort sort) {
        MongoPersistentEntity<?> entity = mongoTemplate.getConverter()
                .getMappingContext()
                .getRequiredPersistentEntity(InventoryItem.class);

        return Sort.by(sort.stream()
                .map(order -> {
                    MongoPersistentProperty property = entity.getPersistentProperty(order.getProperty());

                    return property != null ? order.withProperty(property.getFieldName()) : order;
                })
                .toList());
    }

    // position in requested defIndexes - keeps their order
    private AggregationOperation getDefIndexSortField(InventoryItemFilter filter) {
        return addFields()
                .addField("defIndexSort")
                .withValue(
                        ConditionalOperators.ifNull(
                                ArrayOperators.IndexOfArray
                                        .arrayOf(filter.getDefIndexes())
                                        .indexOf("$dIdx")
                        ).then(Integer.MAX_VALUE)
                )
                .build();
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

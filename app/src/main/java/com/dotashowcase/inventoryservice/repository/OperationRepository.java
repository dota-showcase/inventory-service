package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.util.ArrayList;
import java.util.List;

@Repository
public class OperationRepository implements OperationDAL {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Operation> aggregateLatestByInventories(List<Long> inventorySteamIds) {
        AggregationOperation match = Aggregation.match(Criteria.where("steamId").in(inventorySteamIds));

        AggregationOperation sort = Aggregation.sort(Sort.by("version").descending());

        AggregationOperation group = Aggregation.group("steamId")
                .first("$$ROOT")
                .as("latestOperation");

        AggregationOperation project = Aggregation.project("latestOperation");

        Aggregation aggregation = Aggregation.newAggregation(match, sort, group, project);

        AggregationResults<Operation> results = mongoTemplate.aggregate(
                aggregation,
                Operation.class.getAnnotation(Document.class).value(),
                Operation.class
        );

        return results.getMappedResults();
    }

    @Override
    public List<Operation> findLatestByInventoriesNPlusOne(List<Long> inventorySteamIds) {

        List<Operation> result = new ArrayList<>();

        for (Long steamId: inventorySteamIds) {
            Query query = new Query();
            query.addCriteria(Criteria.where("steamId").is(steamId));
            query.with(Sort.by(Sort.Direction.DESC, "version"));

            result.add(mongoTemplate.findOne(query, Operation.class));
        }

        return result;
    }

    @Override
    public Page<Operation> searchAll(Inventory inventory, Pageable pageable, Sort sort) {
        Query query = new Query();
        query.with(pageable);
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));

        if (sort != null) {
            query.with(sort);
        }

        return PageableExecutionUtils.getPage(
                mongoTemplate.find(query, Operation.class),
                pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Operation.class)
        );
    }

    @Override
    public Operation findLatest(Inventory inventory) {
        Query query = new Query();
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));
        query.with(Sort.by(Sort.Direction.DESC, "version"));

        return mongoTemplate.findOne(query, Operation.class);
    }

    @Override
    public Operation findByVersion(Inventory inventory, int version) {
        Query query = new Query();
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));
        query.addCriteria(Criteria.where("version").is(version));

        return mongoTemplate.findOne(query, Operation.class);
    }

    @Override
    public Operation insertOne(Operation operation) {
        return mongoTemplate.insert(operation);
    }

    @Override
    public long updateMeta(Operation operation, OperationMeta meta) {
        Criteria criteria = Criteria.where("_id").is(operation.getId());
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("meta", meta);

        return mongoTemplate.updateFirst(query, update, Operation.class).getModifiedCount();
    }

    @Override
    public long removeAll(Inventory inventory) {
        Query query = new Query();
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));

        return mongoTemplate.remove(query, Operation.class).getDeletedCount();
    }
}

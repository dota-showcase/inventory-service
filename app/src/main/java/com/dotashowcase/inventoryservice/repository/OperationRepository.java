package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.http.filter.InventoryOperationFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

        AggregationResults<OperationAggregationResult> results = mongoTemplate.aggregate(
                aggregation,
                Operation.class.getAnnotation(Document.class).value(),
                OperationAggregationResult.class
        );

        return results.getMappedResults().stream().map(OperationAggregationResult::getLatestOperation).toList();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class OperationAggregationResult {
        private Long id;

        private Operation latestOperation;
    }

    @Override
    public Page<Operation> findPage(
            Inventory inventory,
            Pageable pageable,
            InventoryOperationFilter filter,
            Sort sort
    ) {
        Query query = new Query();
        query.with(pageable);
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));

        // filter by changes
        if (filter.getHasChanges() != null) {
            if (filter.getHasChanges()) {
                query.addCriteria(
                        new Criteria().orOperator(
                                Criteria.where("meta.createOperationCount").gt(0),
                                Criteria.where("meta.updateOperationCount").gt(0),
                                Criteria.where("meta.deleteOperationCount").gt(0)
                        )
                );
            } else {
                query.addCriteria(
                        new Criteria().andOperator(
                                Criteria.where("meta.createOperationCount").is(0),
                                Criteria.where("meta.updateOperationCount").is(0),
                                Criteria.where("meta.deleteOperationCount").is(0)
                        )
                );
            }
        }

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

package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OperationRepository implements OperationDAL {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Operation> findByInventories(List<Long> inventoryIds) {
        Query query = new Query();
        query.addCriteria(Criteria.where("steamId").in(inventoryIds));

        return mongoTemplate.find(query, Operation.class);
    }

    @Override
    public Operation findLatest(Inventory inventory) {
        Query query = new Query();
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));
        query.with(Sort.by(Sort.Direction.DESC, "version"));

        return mongoTemplate.findOne(query, Operation.class);
    }

//    @Override
//    public List<Operation> findLatest(Inventory inventory, int limit) {
//        Query query = new Query();
//        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));
//        query.with(Sort.by(Sort.Direction.DESC, "version"));
//        query.limit(limit);
//
//        return mongoTemplate.find(query, Operation.class);
//    }

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

package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.embedded.HistoryActionMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HistoryActionRepository implements HistoryActionDAL {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<HistoryAction> findByInventories(List<Long> inventoryIds) {
        Query query = new Query();
        query.addCriteria(Criteria.where("steamId").in(inventoryIds));

        return mongoTemplate.find(query, HistoryAction.class);
    }

    @Override
    public HistoryAction findLatest(Inventory inventory) {
        Query query = new Query();
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));
        query.with(Sort.by(Sort.Direction.DESC, "version"));

        return mongoTemplate.findOne(query, HistoryAction.class);
    }

    @Override
    public HistoryAction insertOne(HistoryAction historyAction) {
        return mongoTemplate.insert(historyAction);
    }

    @Override
    public long updateMeta(HistoryAction historyAction, HistoryActionMeta meta) {
        Criteria criteria = Criteria.where("_id").is(historyAction.getId());
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("meta", meta);

        return mongoTemplate.updateFirst(query, update, HistoryAction.class).getModifiedCount();
    }

    @Override
    public long removeAll(Inventory inventory) {
        Query query = new Query();
        query.addCriteria(Criteria.where("steamId").is(inventory.getSteamId()));

        return mongoTemplate.remove(query, HistoryAction.class).getDeletedCount();
    }
}

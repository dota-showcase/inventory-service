package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HistoryActionRepository extends MongoRepository<HistoryAction, ObjectId> {


}

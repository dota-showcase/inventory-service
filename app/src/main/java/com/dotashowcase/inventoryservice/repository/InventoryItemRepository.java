package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.InventoryItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

// TODO: remove
public interface InventoryItemRepository extends MongoRepository<InventoryItem, Long> {

//    @Query("{defindex:'?0'}")
//    List<InventoryItem> findItemByDefindex(Integer defindex);
}

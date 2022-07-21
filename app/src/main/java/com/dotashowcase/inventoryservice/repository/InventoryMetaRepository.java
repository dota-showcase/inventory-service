package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.model.InventoryMeta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface InventoryMetaRepository extends MongoRepository<InventoryMeta, Long> {
    @Query("{steamId:'?0'}")
    InventoryMeta findItemBySteamId(Long steamId);
}

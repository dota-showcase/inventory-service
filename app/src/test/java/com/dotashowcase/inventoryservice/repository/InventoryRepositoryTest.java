package com.dotashowcase.inventoryservice.repository;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.model.Inventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataMongoTest
@Import(MongoTestConfig.class)
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void itShouldFindInventoryBySteamId() {
        // given
        Long steamId = 100000000000L;
        Inventory inventory = new Inventory(steamId);

        underTest.save(inventory);

        // when
        Inventory expected = underTest.findItemBySteamId(steamId);
        Inventory notExisting = underTest.findItemBySteamId(100000000001L);

        // then
        assertThat(expected.getSteamId()).isEqualTo(steamId);
        assertThat(notExisting).isNull();
    }

    @Test
    void willThrowWhenInsertExistingInventory() {
        // given
        Long steamId = 100000000000L;

        underTest.insert(new Inventory(steamId));

        // when
        // then
        assertThatThrownBy(() -> underTest.insert(new Inventory(steamId)))
                .isInstanceOf(DuplicateKeyException.class);
    }
}

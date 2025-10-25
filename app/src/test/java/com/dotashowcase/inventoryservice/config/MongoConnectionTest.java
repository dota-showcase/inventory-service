package com.dotashowcase.inventoryservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@SpringBootTest
class MongoConnectionTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {
        assertNotNull(mongoTemplate);
    }
}
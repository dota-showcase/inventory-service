package com.dotashowcase.inventoryservice.config;

import com.mongodb.client.MongoClient;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import de.flapdoodle.embed.mongo.transitions.MongodStarter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({MongoAutoConfiguration.class})
@ConditionalOnClass({MongoClient.class, MongodStarter.class})
@ImportAutoConfiguration(EmbeddedMongoAutoConfiguration.class)
@TestConfiguration
@Profile("test")
public class MongoTestConfig {

}
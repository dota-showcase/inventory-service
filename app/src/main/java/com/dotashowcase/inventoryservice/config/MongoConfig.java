package com.dotashowcase.inventoryservice.config;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.text.MessageFormat;

@Configuration
@Profile("default,dev,prod")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private String port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.authentication-database}")
    private String authenticationDatabase;

    @Value("${spring.data.mongodb.auto-index-creation}")
    private Boolean autoIndexCreation;

    @Bean
    public MongoClient mongoClient() {
        String mongoURI = MessageFormat.format(
                "mongodb://{0}:{1}@{2}:{3}/{4}", username, password, host, port, authenticationDatabase
        );
        ConnectionString s = new ConnectionString(mongoURI);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(s).readConcern(ReadConcern.MAJORITY).writeConcern(WriteConcern.MAJORITY)
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                .build();

        return MongoClients.create(settings);
    }

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Primary
    @Bean
    public MongoDatabaseFactory mongoDbFactory() {
        return super.mongoDbFactory();
    }

    @Primary
    @Bean
    public MappingMongoConverter mongoConverter(
            MongoDatabaseFactory mongoFactory, MongoMappingContext mongoMappingContext
    ) throws Exception {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoFactory);
        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);

        // Remove custom "_class" column field from collection
        mongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));

        return mongoConverter;
    }

    @Override
    protected boolean autoIndexCreation() {
        return autoIndexCreation;
    }

    @Bean(name = "mongoTransactionManager")
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        TransactionOptions transactionOptions = TransactionOptions.builder()
                .readConcern(ReadConcern.MAJORITY)
                .writeConcern(WriteConcern.MAJORITY)
                .build();

        return new MongoTransactionManager(dbFactory, transactionOptions);
    }

    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoClient(), database);
    }
}

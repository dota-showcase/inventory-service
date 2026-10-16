package com.dotashowcase.inventoryservice.config;

import com.dotashowcase.inventoryservice.model.Inventory;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import static org.assertj.core.api.Assertions.assertThat;

class MongoConfigTest {

    // app config, not the embedded test one - no index creation, so no connection
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
            .withUserConfiguration(MongoConfig.class)
            .withPropertyValues(
                    "spring.data.mongodb.host=localhost",
                    "spring.data.mongodb.port=27017",
                    "spring.data.mongodb.database=test",
                    "spring.data.mongodb.username=user",
                    "spring.data.mongodb.password=password",
                    "spring.data.mongodb.authentication-database=admin",
                    "spring.data.mongodb.auto-index-creation=false"
            );

    @Test
    void itShouldUseOneConverterWithCustomConversionsAndNoTypeKey() {
        contextRunner.run(context -> {
            // given
            MongoTemplate mongoTemplate = context.getBean(MongoTemplate.class);
            Document document = new Document();

            // when
            mongoTemplate.getConverter().write(new Inventory(76561198000000001L), document);

            // then
            assertThat(context).getBeans(MappingMongoConverter.class).hasSize(1);
            assertThat(mongoTemplate.getConverter().getCustomConversions())
                    .isSameAs(context.getBean(MongoCustomConversions.class));
            assertThat(document).doesNotContainKey("_class");
        });
    }
}

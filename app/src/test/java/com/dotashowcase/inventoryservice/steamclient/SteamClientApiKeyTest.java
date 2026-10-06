package com.dotashowcase.inventoryservice.steamclient;

import com.dotashowcase.inventoryservice.steamclient.response.UserInventoryResponseParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class SteamClientApiKeyTest {

    // placeholders resolved as in the app
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
            .withBean(RestTemplate.class)
            .withBean(UserInventoryResponseParser.class)
            .withBean(InventoryStatusHandler.class)
            .withBean(SteamClient.class);

    @Test
    void willFailToStartWithoutApiKey() {
        // given
        // when
        // then
        contextRunner.run(context -> assertThat(context)
                .hasFailed()
                .getFailure()
                .rootCause()
                .hasMessageContaining("env.steam.api.key"));
    }

    @Test
    void itShouldStartWithApiKey() {
        // given
        // when
        // then
        contextRunner
                .withPropertyValues("env.steam.api.key=test_api_key")
                .run(context -> assertThat(context).hasNotFailed());
    }
}

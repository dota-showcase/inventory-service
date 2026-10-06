package com.dotashowcase.inventoryservice.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestTemplateConfigTest {

    @Test
    @Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD) // fails instead of hanging without timeout
    void willThrowWhenServerDoesNotRespond() throws Exception {
        // given
        RestTemplate restTemplate = new RestTemplateConfig().restTemplate(
                Duration.ofSeconds(1),
                Duration.ofMillis(200)
        );

        // accepts connection, never responds
        try (ServerSocket server = new ServerSocket(0)) {
            String url = "http://localhost:" + server.getLocalPort();

            // when
            // then
            assertThatThrownBy(() -> restTemplate.getForEntity(url, String.class))
                    .isInstanceOf(ResourceAccessException.class)
                    .hasRootCauseInstanceOf(SocketTimeoutException.class);
        }
    }
}

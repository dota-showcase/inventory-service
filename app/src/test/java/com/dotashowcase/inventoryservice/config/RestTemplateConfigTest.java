package com.dotashowcase.inventoryservice.config;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void itShouldRequestAndDecompressGzip() throws Exception {
        // given
        RestTemplate restTemplate = new RestTemplateConfig().restTemplate(
                Duration.ofSeconds(1),
                Duration.ofSeconds(1)
        );

        String body = "{\"result\":{\"status\":1}}";
        AtomicReference<String> acceptEncoding = new AtomicReference<>();

        // gzip only, as steam does on request
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            acceptEncoding.set(exchange.getRequestHeaders().getFirst("Accept-Encoding"));

            byte[] gzippedBody = gzip(body);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
            exchange.sendResponseHeaders(200, gzippedBody.length);

            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(gzippedBody);
            }
        });
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort();

            // when
            String response = restTemplate.getForObject(url, String.class);

            // then
            assertThat(acceptEncoding.get()).contains("gzip");
            assertThat(response).isEqualTo(body);
        } finally {
            server.stop(0);
        }
    }

    private static byte[] gzip(String value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            gzip.write(value.getBytes(StandardCharsets.UTF_8));
        }

        return bytes.toByteArray();
    }
}

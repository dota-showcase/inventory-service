package com.dotashowcase.inventoryservice.steamclient;

import com.dotashowcase.inventoryservice.steamclient.exception.InventoryStatusException;
import com.dotashowcase.inventoryservice.steamclient.exception.SteamClientException;
import com.dotashowcase.inventoryservice.steamclient.response.UserInventoryResponseParser;
import com.dotashowcase.inventoryservice.steamclient.response.dto.UserInventoryResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class SteamClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Autowired
    private UserInventoryResponseParser userInventoryResponseParser;

    @Autowired
    private InventoryStatusHandler inventoryStatusHandler;

    private SteamClient underTest;

    @BeforeEach
    void setUp() {
        underTest = new SteamClient(restTemplate, userInventoryResponseParser, inventoryStatusHandler);
    }

    @Test
    void itShouldFetchUserInventory() {
        // given
        String responseBody = """
                {
                    "result": {
                        "status": 1,
                        "num_backpack_slots": 10120,
                        "items": [
                            {
                                "id": 3120140571,
                                "original_id": 3053978672,
                                "defindex": 6671,
                                "level": 1,
                                "quality": 9,
                                "inventory": 1133,
                                "quantity": 1
                            },
                            {
                                "id": 8178590185,
                                "original_id": 2764010163,
                                "defindex": 6450,
                                "level": 1,
                                "quality": 4,
                                "inventory": 1105,
                                "quantity": 1
                            },
                            {
                                "id": 8348787622,
                                "original_id": 7697029422,
                                "defindex": 7559,
                                "level": 1,
                                "quality": 4,
                                "inventory": 9675,
                                "quantity": 1
                            }
                        ]
                    }
                }""";

        Long steamId = 100000000000L;

        givenSteamResponse(responseBody);

        // when
        UserInventoryResponseDTO expected = underTest.fetchUserInventory(steamId);

        // then
        assertThat(expected.hasItems()).isEqualTo(true);
        assertThat(expected.getItems().size()).isEqualTo(3);
    }

    @Test
    void willThrowWhenInventoryIsPrivate() {
        // given
        String responseBody = """
                {
                    "result": {
                        "status": 15
                    }
                }""";

        Long steamId = 100000000000L;

        givenSteamResponse(responseBody);

        // when
        // then
        assertThatThrownBy(() -> underTest.fetchUserInventory(steamId))
                .isInstanceOf(InventoryStatusException.class)
                .hasMessageContaining("Inventory status - User inventory is private");
    }

    @Test
    void willThrowWhenInventoryIsNotExists() {
        // given
        String responseBody = """
                {
                    "result": {
                        "status": 18
                    }
                }""";

        Long steamId = 100000000000L;

        givenSteamResponse(responseBody);

        // when
        // then
        assertThatThrownBy(() -> underTest.fetchUserInventory(steamId))
                .isInstanceOf(InventoryStatusException.class)
                .hasMessageContaining("Inventory status - The Steam ID given does not exist");
    }

    @Test
    void willThrowWhenNoItems() {
        // given
        String responseBody = """
                {
                    "result": {
                        "status": 1,
                        "num_backpack_slots": 10120,
                        "items": []
                    }
                }""";

        Long steamId = 100000000000L;

        givenSteamResponse(responseBody);

        // when
        // then
        assertThatThrownBy(() -> underTest.fetchUserInventory(steamId))
                .isInstanceOf(InventoryStatusException.class)
                .hasMessageContaining("Inventory status - Items not present");
    }

    @Test
    void willThrowWhenInventoryStatusIsMissing() {
        // given
        String responseBody = """
                {
                    "result": {
                        "num_backpack_slots": 10120,
                        "items": []
                    }
                }""";

        Long steamId = 100000000000L;

        givenSteamResponse(responseBody);

        // when
        // then
        assertThatThrownBy(() -> underTest.fetchUserInventory(steamId))
                .isInstanceOf(SteamClientException.class)
                .hasMessageContaining("no inventory status");
    }

    @Test
    void willNotExposeApiKeyOnIOError() {
        // given
        String apiKey = "SECRET_KEY";
        ReflectionTestUtils.setField(underTest, "steamApiKey", apiKey);

        Long steamId = 100000000000L;

        when(restTemplate.execute(
                        ArgumentMatchers.any(URI.class),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.isNull(),
                        ArgumentMatchers.<ResponseExtractor<Object>>any()
                )
        )
                .thenThrow(new ResourceAccessException(
                        "I/O error on GET request for \"https://api.steampowered.com/?key=" + apiKey + "\": timeout"
                ));

        // when
        // then
        assertThatThrownBy(() -> underTest.fetchUserInventory(steamId))
                .isInstanceOf(SteamClientException.class)
                .hasMessageNotContaining(apiKey);
    }

    @Test
    void willThrowWhenBodyIsMalformed() {
        // given
        String responseBody = """
                {
                    "result": {
                        "status": 1,
                        "items": [
                            {
                                "id": 3120140571,
                                "level": "high"
                            }
                        ]
                    }
                }""";

        Long steamId = 100000000000L;

        givenSteamResponse(responseBody);

        // when
        // then
        assertThatThrownBy(() -> underTest.fetchUserInventory(steamId))
                .isInstanceOf(SteamClientException.class)
                .hasMessageContaining("Failed to parse a response from steam");
    }

    @Test
    void willThrowWhenBodyReadFails() {
        // given
        // real RestTemplate - wraps body read errors
        RestTemplate realRestTemplate = new RestTemplate();
        MockRestServiceServer steamServer = MockRestServiceServer.bindTo(realRestTemplate).build();
        SteamClient steamClient = new SteamClient(realRestTemplate, userInventoryResponseParser, inventoryStatusHandler);

        InputStream timeout = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new SocketTimeoutException("Read timed out");
            }
        };
        InputStream responseBody = new SequenceInputStream(
                new ByteArrayInputStream("""
                        {
                            "result": {
                                "status": 1,
                                "items": [
                                    {
                                        "id": 3120140571,""".getBytes(StandardCharsets.UTF_8)),
                timeout
        );

        steamServer.expect(method(HttpMethod.GET))
                .andRespond(request -> new MockClientHttpResponse(responseBody, HttpStatus.OK));

        Long steamId = 100000000000L;

        // when
        // then
        // transport error, not a bad body
        assertThatThrownBy(() -> steamClient.fetchUserInventory(steamId))
                .isInstanceOf(SteamClientException.class)
                .hasMessageContaining("Failed to reach steam api");
    }

    // runs the client's extractor on the body, as RestTemplate does
    private void givenSteamResponse(String responseBody) {
        when(restTemplate.execute(
                        ArgumentMatchers.any(URI.class),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.isNull(),
                        ArgumentMatchers.<ResponseExtractor<Object>>any()
                )
        )
                .thenAnswer(invocation -> invocation.<ResponseExtractor<Object>>getArgument(3).extractData(
                        new MockClientHttpResponse(responseBody.getBytes(StandardCharsets.UTF_8), HttpStatus.OK)
                ));
    }
}

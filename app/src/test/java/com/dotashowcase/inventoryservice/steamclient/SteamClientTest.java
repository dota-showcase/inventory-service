package com.dotashowcase.inventoryservice.steamclient;

import com.dotashowcase.inventoryservice.steamclient.exception.InventoryStatusException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
    void canFetchUserInventory() {
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

        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                        ArgumentMatchers.any(URI.class),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.<Class<String>>any()
                )
        )
                .thenReturn(stringResponseEntity);

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

        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                        ArgumentMatchers.any(URI.class),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.<Class<String>>any()
                )
        )
                .thenReturn(stringResponseEntity);

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

        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                        ArgumentMatchers.any(URI.class),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.<Class<String>>any()
                )
        )
                .thenReturn(stringResponseEntity);

        // when
        // then
        assertThatThrownBy(() -> underTest.fetchUserInventory(steamId))
                .isInstanceOf(InventoryStatusException.class)
                .hasMessageContaining("Inventory status - The Steam ID given does not exist");
    }

    @Test
    void willThrowWheNoItems() {
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

        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                        ArgumentMatchers.any(URI.class),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.<Class<String>>any()
                )
        )
                .thenReturn(stringResponseEntity);

        // when
        // then
        assertThatThrownBy(() -> underTest.fetchUserInventory(steamId))
                .isInstanceOf(InventoryStatusException.class)
                .hasMessageContaining("Inventory status - Items not present");
    }
}
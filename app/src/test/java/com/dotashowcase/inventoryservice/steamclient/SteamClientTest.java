package com.dotashowcase.inventoryservice.steamclient;

import com.dotashowcase.inventoryservice.steamclient.response.UserInventoryResponseParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SteamClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserInventoryResponseParser userInventoryResponseParser;

    @Autowired
    private InventoryStatusHandler inventoryStatusHandler;

    private SteamClient underTest;

    @BeforeEach
    void setUp() {
        underTest = new SteamClient(restTemplate, userInventoryResponseParser, inventoryStatusHandler);
    }

    @Test
    void fetchUserInventory() {
        //
    }
}
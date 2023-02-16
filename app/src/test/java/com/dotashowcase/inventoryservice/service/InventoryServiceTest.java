package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.repository.InventoryRepository;
import com.dotashowcase.inventoryservice.steamclient.SteamClient;
import com.dotashowcase.inventoryservice.support.SortBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemService inventoryItemService;

    @Mock
    private OperationService operationService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private SortBuilder sortBuilder;

    @Mock
    private SteamClient steamClient;

    private InventoryService underTest;

    @BeforeEach
    void setUp() {
        underTest = new InventoryServiceImpl(
                inventoryItemService, operationService, inventoryRepository, sortBuilder, steamClient
        );
    }

    @Test
    void getAll() {
    }

    @Test
    void get() {
    }

    @Test
    void create() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void findInventory() {
    }
}
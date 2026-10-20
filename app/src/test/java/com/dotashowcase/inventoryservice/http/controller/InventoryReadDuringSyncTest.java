package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.repository.InventoryRepository;
import com.dotashowcase.inventoryservice.service.InventoryItemService;
import com.dotashowcase.inventoryservice.service.OperationService;
import com.dotashowcase.inventoryservice.service.result.dto.OperationCountDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MongoTestConfig.class)
class InventoryReadDuringSyncTest {

    private static final Long STEAM_ID = 76561198000000001L;

    private static final String INVENTORY_URL = "/api/v1/inventories/" + STEAM_ID;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OperationService operationService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @AfterEach
    void tearDown() {
        mongoTemplate.remove(new Query(), Inventory.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), Operation.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), InventoryItem.class.getAnnotation(Document.class).value());
    }

    @Test
    void itShouldReadEmptyInventoryWhileUpdateIsInProgress() throws Exception {
        // given
        Inventory inventory = inventoryRepository.insert(new Inventory(STEAM_ID));

        // version 1 - finished sync
        Operation operation1 = operationService.create(inventory, null, Operation.Trigger.manual, null);
        inventoryItemService.create(inventory, operation1, List.of(getItemDTO(1L, 1), getItemDTO(2L, 2)));
        operationService.createAndSaveMeta(operation1, new OperationCountDTO(2, 0, 0, 0), 2, 10120);

        // version 2 - sync started, counts not stored yet
        operationService.create(inventory, Operation.Type.U, Operation.Trigger.auto, operation1);

        // when
        // then
        mockMvc.perform(get(INVENTORY_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation.version").value(2))
                .andExpect(jsonPath("$.operation.meta.itemCount").value(0))
                .andExpect(jsonPath("$.operation.meta.numSlots").value(0));

        mockMvc.perform(get("/api/v1/inventories/search-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].operation.version").value(2))
                .andExpect(jsonPath("$.data[0].operation.meta.itemCount").value(0));

        mockMvc.perform(get(INVENTORY_URL + "/items/page-positioned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.pagination.totalItems").value(0));

        mockMvc.perform(get(INVENTORY_URL + "/operations/search-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].meta.itemCount").value(0))
                .andExpect(jsonPath("$.data[1].meta.itemCount").value(2));
    }

    @Test
    void itShouldListInventoryWithoutOperationWhileCreateIsInProgress() throws Exception {
        // given
        // create started - inventory stored, first operation not yet
        inventoryRepository.insert(new Inventory(STEAM_ID));

        // when
        // then
        mockMvc.perform(get("/api/v1/inventories/search-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].steamId").value(STEAM_ID))
                .andExpect(jsonPath("$.data[0].operation").value(nullValue()));
    }

    private ItemDTO getItemDTO(Long id, int position) {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setId(id);
        itemDTO.setOriginal_id(id);
        itemDTO.setDefindex(5000);
        itemDTO.setInventory((long) position);
        itemDTO.setQuantity(1);

        return itemDTO;
    }
}

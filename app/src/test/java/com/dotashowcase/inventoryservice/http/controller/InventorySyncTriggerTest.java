package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.repository.InventoryRepository;
import com.dotashowcase.inventoryservice.steamclient.SteamClient;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.UserInventoryResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// real services and storage - steam mocked
@SpringBootTest
@AutoConfigureMockMvc
@Import(MongoTestConfig.class)
class InventorySyncTriggerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @MockitoBean
    private SteamClient steamClient;

    @AfterEach
    void tearDown() {
        mongoTemplate.remove(new Query(), Inventory.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), Operation.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), InventoryItem.class.getAnnotation(Document.class).value());
    }

    @Test
    void itShouldShowTriggerOfEverySyncInHistory() throws Exception {
        // given
        // a steam id per test - syncs are rate limited
        Long steamId = 76561198000000011L;

        when(steamClient.fetchUserInventory(steamId)).thenReturn(getResponse(getItemDTO(1L)));

        mockMvc.perform(post("/api/v1/inventories/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"steamId\": " + steamId + ", \"trigger\": \"auto\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.operation.trigger").value("auto"));

        mockMvc.perform(put("/api/v1/inventories/" + steamId).param("trigger", "resync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation.trigger").value("resync"));

        // when
        // then
        mockMvc.perform(get("/api/v1/inventories/" + steamId + "/operations/search-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].version", contains(2, 1)))
                .andExpect(jsonPath("$.data[*].trigger", contains("resync", "auto")));

        mockMvc.perform(get("/api/v1/inventories/" + steamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation.trigger").value("resync"));
    }

    @Test
    void itShouldShowUnknownTriggerAsNull() throws Exception {
        // given
        Long steamId = 76561198000000012L;

        // version 1 - stored before triggers were tracked
        inventoryRepository.insert(new Inventory(steamId));
        mongoTemplate.getCollection(Operation.class.getAnnotation(Document.class).value())
                .insertOne(new org.bson.Document("steamId", steamId).append("version", 1).append("type", "C"));

        when(steamClient.fetchUserInventory(steamId)).thenReturn(getResponse(getItemDTO(1L)));

        // version 2 - client not aware of triggers
        mockMvc.perform(put("/api/v1/inventories/" + steamId))
                .andExpect(status().isOk());

        // when
        // then
        mockMvc.perform(get("/api/v1/inventories/" + steamId + "/operations/search-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].trigger").value(nullValue()))
                .andExpect(jsonPath("$.data[1].trigger").value(nullValue()));
    }

    private UserInventoryResponseDTO getResponse(ItemDTO... items) {
        UserInventoryResponseDTO response = new UserInventoryResponseDTO();
        response.setStatus(1);
        response.setNumberBackpackSlots(10120);
        response.setItems(List.of(items));

        return response;
    }

    private ItemDTO getItemDTO(Long id) {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setId(id);
        itemDTO.setOriginal_id(id);
        itemDTO.setDefindex(5000);
        itemDTO.setInventory(id);
        itemDTO.setQuantity(1);

        return itemDTO;
    }
}

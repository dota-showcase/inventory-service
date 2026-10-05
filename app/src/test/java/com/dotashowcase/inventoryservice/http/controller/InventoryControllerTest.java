package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MongoTestConfig.class)
class InventoryControllerTest {

    private static final String STEAM_ID_ERROR = "steamId: Steam Id must be in '7656119XXXXXXXXXX' format";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void willReturnValidationErrorWhenCreateWithInvalidSteamId() throws Exception {
        // given
        String requestBody = """
                {
                    "steamId": 100000000000
                }""";

        // when
        // then
        mockMvc.perform(post("/api/v1/inventories/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors[0]").value(STEAM_ID_ERROR))
                .andExpect(jsonPath("$.path").value("/api/v1/inventories/"));

        verifyNoInteractions(inventoryService);
    }

    @Test
    void willReturnValidationErrorWhenCreateWithoutSteamId() throws Exception {
        // given
        String requestBody = "{}";

        // when
        // then
        mockMvc.perform(post("/api/v1/inventories/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.validationErrors[0]").value(STEAM_ID_ERROR));

        verifyNoInteractions(inventoryService);
    }
}

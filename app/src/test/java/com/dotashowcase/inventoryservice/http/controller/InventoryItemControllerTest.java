package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MongoTestConfig.class)
class InventoryItemControllerTest {

    private static final Long STEAM_ID = 76561198000000001L;

    private static final String SEARCH_PAGE_URL = "/api/v1/inventories/" + STEAM_ID + "/items/search-page";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        when(inventoryService.findInventory(STEAM_ID)).thenReturn(new Inventory(STEAM_ID));
    }

    @Test
    void willReturnValidationErrorWhenSortFieldNotAllowed() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(post(SEARCH_PAGE_URL)
                        .param("sort", "-name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors[0]").value(startsWith("sort: Sorting by 'name' is not allowed")));
    }

    @Test
    void itShouldSortByAllowedField() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(post(SEARCH_PAGE_URL)
                        .param("sort", "-defIndex")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}

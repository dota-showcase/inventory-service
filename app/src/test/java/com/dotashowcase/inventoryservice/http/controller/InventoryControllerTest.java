package com.dotashowcase.inventoryservice.http.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.http.exception.AppExceptionHandler;
import com.dotashowcase.inventoryservice.http.ratelimiter.RateLimitHandler;
import com.dotashowcase.inventoryservice.service.InventoryService;
import com.dotashowcase.inventoryservice.service.exception.InventoryNotFoundException;
import com.dotashowcase.inventoryservice.steamclient.exception.InventoryStatusException;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MongoTestConfig.class)
class InventoryControllerTest {

    private static final String STEAM_ID_ERROR =
            "steamId: Steam Id must be a SteamID64 between 76561197960265729 and 76561202255233023";

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

    @Test
    void willReturnRetryAfterWhenUpdateRateLimited() throws Exception {
        // given
        Long steamId = 76561198000000001L;

        mockMvc.perform(put("/api/v1/inventories/" + steamId))
                .andExpect(status().isOk());

        // when
        // then
        mockMvc.perform(put("/api/v1/inventories/" + steamId))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "60"))
                .andExpect(header().string(RateLimitHandler.HEADER_RETRY_AFTER, "60"));

        verify(inventoryService, times(1)).update(steamId);
    }

    @Test
    void willHideInternalErrorDetails() throws Exception {
        // given
        Long steamId = 76561198000000001L;

        when(inventoryService.get(steamId)).thenThrow(new IllegalStateException("internal detail"));

        // when
        // then
        mockMvc.perform(get("/api/v1/inventories/" + steamId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(content().string(not(containsString("internal detail"))));
    }

    @Test
    void itShouldLogOnlyServerErrorsAsErrors() throws Exception {
        // given
        Long notFoundSteamId = 76561198000000002L;
        Long privateSteamId = 76561198000000003L;
        Long failingSteamId = 76561198000000004L;

        when(inventoryService.get(notFoundSteamId)).thenThrow(new InventoryNotFoundException());
        when(inventoryService.update(privateSteamId)).thenThrow(
                InventoryStatusException.itemsNotPresent(200, 15, "User inventory is private")
        );
        when(inventoryService.get(failingSteamId)).thenThrow(new IllegalStateException("internal detail"));

        ListAppender<ILoggingEvent> logEvents = new ListAppender<>();
        logEvents.start();
        Logger logger = (Logger) LoggerFactory.getLogger(AppExceptionHandler.class);
        logger.addAppender(logEvents);

        try {
            // when
            mockMvc.perform(get("/api/v1/inventories/" + notFoundSteamId)).andExpect(status().isNotFound());
            mockMvc.perform(put("/api/v1/inventories/" + privateSteamId)).andExpect(status().isBadRequest());
            mockMvc.perform(get("/api/v1/inventories/" + failingSteamId)).andExpect(status().isInternalServerError());

            // then
            assertThat(logEvents.list)
                    .extracting(ILoggingEvent::getLevel)
                    .containsExactly(Level.WARN, Level.WARN, Level.ERROR);
        } finally {
            logger.detachAppender(logEvents);
        }
    }
}

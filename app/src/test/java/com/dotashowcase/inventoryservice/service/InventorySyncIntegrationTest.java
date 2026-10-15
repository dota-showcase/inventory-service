package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.config.MongoTestConfig;
import com.dotashowcase.inventoryservice.http.filter.InventoryItemChangeFilter;
import com.dotashowcase.inventoryservice.http.filter.InventoryItemFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
import com.dotashowcase.inventoryservice.service.type.ChangeType;
import com.dotashowcase.inventoryservice.steamclient.SteamClient;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.UserInventoryResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// real services and storage - steam mocked
@SpringBootTest
@Import(MongoTestConfig.class)
class InventorySyncIntegrationTest {

    private static final Long STEAM_ID = 76561198000000001L;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private InventoryItemChangesService inventoryItemChangesService;

    @Autowired
    private OperationService operationService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockitoBean
    private SteamClient steamClient;

    @AfterEach
    void tearDown() {
        mongoTemplate.remove(new Query(), Inventory.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), Operation.class.getAnnotation(Document.class).value());
        mongoTemplate.remove(new Query(), InventoryItem.class.getAnnotation(Document.class).value());
    }

    @Test
    void itShouldRecordCreatedItemsOnCreate() {
        // given
        when(steamClient.fetchUserInventory(STEAM_ID))
                .thenReturn(getResponse(getItemDTO(1L, "#1"), getItemDTO(2L, "#2"), getItemDTO(3L, "#3")));

        // when
        inventoryService.create(STEAM_ID);

        // then
        Inventory inventory = inventoryService.findInventory(STEAM_ID);

        assertThat(getChangedItemIds(inventory, 1, ChangeType.create)).containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat(getChangedItemIds(inventory, 1, ChangeType.update)).isEmpty();
        assertThat(getChangedItemIds(inventory, 1, ChangeType.delete)).isEmpty();

        OperationMeta meta = operationService.getByVersion(inventory, 1).getMeta();
        assertThat(meta.getCreateOperationCount()).isEqualTo(3);
        assertThat(meta.getUpdateOperationCount()).isEqualTo(0);
        assertThat(meta.getDeleteOperationCount()).isEqualTo(0);
        assertThat(meta.getItemCount()).isEqualTo(3);
    }

    @Test
    void itShouldRecordChangesOnUpdate() {
        // given
        // #1 the same, #2 renamed, #3 gone, #4 new
        when(steamClient.fetchUserInventory(STEAM_ID))
                .thenReturn(getResponse(getItemDTO(1L, "#1"), getItemDTO(2L, "#2"), getItemDTO(3L, "#3")))
                .thenReturn(getResponse(getItemDTO(1L, "#1"), getItemDTO(2L, "#2 - renamed"), getItemDTO(4L, "#4")));

        inventoryService.create(STEAM_ID);

        // when
        inventoryService.update(STEAM_ID);

        // then
        Inventory inventory = inventoryService.findInventory(STEAM_ID);

        assertThat(getChangedItemIds(inventory, 2, ChangeType.create)).containsExactly(4L);
        assertThat(getChangedItemIds(inventory, 2, ChangeType.update)).containsExactly(2L);
        assertThat(getChangedItemIds(inventory, 2, ChangeType.delete)).containsExactly(3L);

        OperationMeta meta = operationService.getByVersion(inventory, 2).getMeta();
        assertThat(meta.getCreateOperationCount()).isEqualTo(1);
        assertThat(meta.getUpdateOperationCount()).isEqualTo(1);
        assertThat(meta.getDeleteOperationCount()).isEqualTo(1);
        assertThat(meta.getItemCount()).isEqualTo(3);

        // active items - as in steam
        assertThat(inventoryItemService.get(inventory, new InventoryItemFilter(), null))
                .extracting("customName")
                .containsExactlyInAnyOrder("#1", "#2 - renamed", "#4");
    }

    private List<Long> getChangedItemIds(Inventory inventory, int version, ChangeType type) {
        return inventoryItemChangesService.get(inventory, version, type, new InventoryItemChangeFilter())
                .stream()
                .map(InventoryItemDTO::getItemId)
                .toList();
    }

    private UserInventoryResponseDTO getResponse(ItemDTO... items) {
        UserInventoryResponseDTO response = new UserInventoryResponseDTO();
        response.setStatus(1);
        response.setNumberBackpackSlots(10120);
        response.setItems(List.of(items));

        return response;
    }

    private ItemDTO getItemDTO(Long id, String customName) {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setId(id);
        itemDTO.setOriginal_id(id);
        itemDTO.setDefindex(5000);
        itemDTO.setInventory(id);
        itemDTO.setQuantity(1);
        itemDTO.setCustom_name(customName);

        return itemDTO;
    }
}

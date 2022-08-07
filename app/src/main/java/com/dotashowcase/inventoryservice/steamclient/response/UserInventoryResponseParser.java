package com.dotashowcase.inventoryservice.steamclient.response;

import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.UserInventoryResponseDTO;
import com.dotashowcase.inventoryservice.steamclient.response.exception.BadResponseBodyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  "result": {
 *         "status": 1,
 *         "num_backpack_slots": 10120,
 *         "items": [
 *             {
 *                 "id": 674380448,
 *                 "original_id": 674380448,
 *                 "defindex": 15058,
 *                 "level": 1,
 *                 "quality": 4,
 *                 "inventory": 1,
 *                 "quantity": 1,
 *                 "flag_cannot_trade": true,
 *                 "flag_cannot_craft": true,
 *                 "attributes": [
 *                     {
 *                         "defindex": 9,
 *                         "value": 3,
 *                         "float_value": 4.20389539297445121e-45
 *                     },
 *                     {
 *                         "defindex": 153,
 *                         "value": 1065353216,
 *                         "float_value": 1
 *                     },
 *                     {
 *                         "defindex": 14,
 *                         "value": 14,
 *                         "float_value": 1.9618178500547439e-44
 *                     }
 *                 ]
 *             },
 *    ...
 */
@Component
@NoArgsConstructor
public class UserInventoryResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String NODE_RESULT = "result";
    private static final String NODE_ITEMS = "items";
    private static final String NODE_STATUS = "status";
    private static final String NODE_SLOTS = "num_backpack_slots";

    public UserInventoryResponseDTO run(String responseBody) throws BadResponseBodyException {
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw BadResponseBodyException.invalidBodyStructure(e.getMessage());
        }

        // parse root node
        JsonNode resultNode = rootNode.path(NODE_RESULT);

        if (resultNode instanceof MissingNode) {
            throw BadResponseBodyException.jsonNodeNotFound(NODE_RESULT);
        }

        UserInventoryResponseDTO userInventoryResponseDTO = new UserInventoryResponseDTO();

        // parse status and stats nodes
        if (resultNode.has(NODE_STATUS)) {
            JsonNode statusNode = resultNode.path(NODE_STATUS);
            userInventoryResponseDTO.setStatus(statusNode.asInt());
        }

        if (resultNode.has(NODE_SLOTS)) {
            JsonNode numSlotNode = resultNode.path(NODE_SLOTS);
            userInventoryResponseDTO.setNumberBackpackSlots(numSlotNode.asInt());
        }

        // parse items node
        if (!resultNode.has(NODE_ITEMS)) {
           return userInventoryResponseDTO;
        }

        JsonNode itemNode = resultNode.path(NODE_ITEMS);

        List <ItemDTO> items = new ArrayList<>();
        for (JsonNode item : itemNode) {
            items.add(objectMapper.convertValue(item, ItemDTO.class));
        }

        userInventoryResponseDTO.setItems(items);

        return userInventoryResponseDTO;
    }
}

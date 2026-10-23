package com.dotashowcase.inventoryservice.steamclient.response;

import com.dotashowcase.inventoryservice.steamclient.response.dto.UserInventoryResponseDTO;
import com.dotashowcase.inventoryservice.steamclient.response.exception.BadResponseBodyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

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

    // steam adds fields - unknown ones are ignored in nested nodes too
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final String NODE_RESULT = "result";

    // bound while streaming - no body string or json tree in memory, big inventories are several MB
    private record ResponseBody(UserInventoryResponseDTO result) {}

    /**
     * @throws BadResponseBodyException on invalid json or unexpected structure
     * @throws IOException on body read errors, e.g. timeout
     */
    public UserInventoryResponseDTO run(InputStream responseBody) throws IOException, BadResponseBodyException {
        if (responseBody == null) {
            throw BadResponseBodyException.invalidBodyStructure("empty body");
        }

        ResponseBody body;
        try {
            body = objectMapper.readValue(responseBody, ResponseBody.class);
        } catch (JsonProcessingException e) {
            IOException readError = findReadError(e);
            if (readError != null) {
                throw readError;
            }

            // reject whole response - skipped items would be synced as deleted
            throw BadResponseBodyException.invalidBodyStructure(e.getMessage());
        }

        if (body == null || body.result() == null) {
            throw BadResponseBodyException.jsonNodeNotFound(NODE_RESULT);
        }

        return body.result();
    }

    // jackson wraps read errors inside lists (items, attributes) - still a transport error, not a bad body
    private static IOException findReadError(JsonProcessingException e) {
        for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof IOException ioException && !(cause instanceof JsonProcessingException)) {
                return ioException;
            }
        }

        return null;
    }
}

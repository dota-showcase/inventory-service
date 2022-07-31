package com.dotashowcase.inventoryservice.steamclient;

import com.dotashowcase.inventoryservice.steamclient.exception.BadRequestException;
import com.dotashowcase.inventoryservice.steamclient.exception.InventoryStatusException;
import com.dotashowcase.inventoryservice.steamclient.response.UserInventoryResponseParser;
import com.dotashowcase.inventoryservice.steamclient.response.dto.UserInventoryResponseDTO;
import com.dotashowcase.inventoryservice.steamclient.response.exception.BadResponseBodyException;

import org.apache.http.client.utils.URIBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class SteamClient {

    private final RestTemplate restTemplate;

    private final UserInventoryResponseParser userInventoryResponseParser;

    private final InventoryStatusHandler inventoryStatusHandler;

    @Value("${env.steam.api.key: api_key}")
    private String steamApiKey;

    public SteamClient(
            RestTemplate restTemplate,
            UserInventoryResponseParser userInventoryResponseParser,
            InventoryStatusHandler inventoryStatusHandler
    ) {
        this.restTemplate = restTemplate;
        this.userInventoryResponseParser = userInventoryResponseParser;
        this.inventoryStatusHandler = inventoryStatusHandler;
    }

    /**
     * Makes request to get steam user inventory.
     *
     * @param steamId steam user id
     * @return inventory response
     * @throws BadRequestException on steam server errors
     * @throws InventoryStatusException on inventory status or wrong params errors
     */
    public UserInventoryResponseDTO fetchUserInventory(Long steamId)
            throws BadRequestException, InventoryStatusException {
        final URI uri = this.getInventoryURI(steamId);

        // retrieve data
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
        } catch (RestClientResponseException restClientResponseException) {
            throw BadRequestException.steamApiError(
                    restClientResponseException.getRawStatusCode(),
                    restClientResponseException.getMessage()
            );
        }

        // parse response data
        UserInventoryResponseDTO userInventoryResponseDTO;
        try {
            userInventoryResponseDTO = userInventoryResponseParser.run(response.getBody());
        } catch (BadResponseBodyException badResponseBodyException) {
            // TODO: add logs

            return new UserInventoryResponseDTO();
        }

        int inventoryStatus = userInventoryResponseDTO.getStatus();
        if (!inventoryStatusHandler.isOk(inventoryStatus) && !userInventoryResponseDTO.hasItems()) {
            throw InventoryStatusException.itemsNotPresent(
                    inventoryStatus, inventoryStatusHandler.getStatusMessage(inventoryStatus)
            );
        }

        return userInventoryResponseDTO;
    }

    private URI getInventoryURI(Long steamId) {
        URIBuilder builder = new URIBuilder()
                .setScheme("https")
                .setHost("api.steampowered.com")
                .setPath("/IEconItems_570/GetPlayerItems/v1")
                .addParameter("key", steamApiKey)
                .addParameter("steamid", steamId.toString());

       try {
           return builder.build();
       } catch (URISyntaxException uriSyntaxException) {
           throw new IllegalStateException("Failed to build steam inventory url");
       }
    }
}
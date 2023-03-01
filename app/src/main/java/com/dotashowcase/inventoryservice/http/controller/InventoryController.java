package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.http.ratelimiter.RateLimitHandler;
import com.dotashowcase.inventoryservice.http.request.InventoryCreateRequest;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.InventoryService;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestOperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithOperationsDTO;
import com.dotashowcase.inventoryservice.support.validator.SteamIdConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "inventory", description = "")
@Validated
@RestController
@RequestMapping("api/v1/")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RateLimitHandler handler;

    @Operation(summary = "foo", description = "bar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("inventories/")
    public List<InventoryWithOperationsDTO> index(@RequestParam(defaultValue = "-steamId") String sort) {
        return inventoryService.getAll(sort);
    }

    @GetMapping("inventories/{steamId}")
    public InventoryWithOperationsDTO get(@PathVariable @SteamIdConstraint Long steamId) {
        return inventoryService.get(steamId);
    }

    @PostMapping("inventories/")
    @ResponseBody
    public InventoryWithLatestOperationDTO create(@RequestBody @Valid InventoryCreateRequest inventoryCreateRequest) {
        return inventoryService.create(inventoryCreateRequest.getSteamId());
    }

    @PutMapping("inventories/{steamId}")
    @ResponseBody
    public ResponseEntity<InventoryWithLatestOperationDTO> update(@PathVariable @SteamIdConstraint Long steamId) {
        HttpHeaders responseHeaders = handler.run(steamId);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(inventoryService.update(steamId));
    }

    @DeleteMapping("inventories/{steamId}")
    public ResponseEntity<Inventory> delete(@PathVariable @SteamIdConstraint Long steamId) {
        inventoryService.delete(steamId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

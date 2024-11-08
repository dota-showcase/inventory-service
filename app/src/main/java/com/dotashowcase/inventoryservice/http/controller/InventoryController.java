package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.config.AppConstant;
import com.dotashowcase.inventoryservice.http.exception.response.ErrorResponse;
import com.dotashowcase.inventoryservice.http.exception.response.SteamErrorResponse;
import com.dotashowcase.inventoryservice.http.exception.response.ValidationErrorResponse;
import com.dotashowcase.inventoryservice.http.ratelimiter.RateLimitHandler;
import com.dotashowcase.inventoryservice.http.request.InventoryCreateRequest;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.InventoryService;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestOperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithOperationsDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import com.dotashowcase.inventoryservice.support.validator.SteamIdConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory", description = "The inventory API")
@Validated
@RestController
@RequestMapping("api/v1/")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RateLimitHandler rateLimitHandler;

    @Operation(description = "Get a list of all inventories, including operations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = InventoryWithOperationsDTO.class)))),
    })
    @GetMapping("inventories/")
    public List<InventoryWithOperationsDTO> index(@RequestParam(defaultValue = "-steamId") String sort) {
        return inventoryService.getAll(sort);
    }

    @Operation(description = "Get a list of all inventories, including operations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = InventoryWithOperationsDTO.class)))),
    })
    @GetMapping("inventories/search")
    public PageResult<InventoryWithLatestOperationDTO> search(
            @PageableDefault(size = AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE) Pageable pageable,
            @RequestParam(defaultValue = "-steamId") String sort
    ) {
        return inventoryService.getPage(pageable, sort);
    }

    @Operation(description = "Get an inventory, including operations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InventoryWithOperationsDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Inventory not exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @GetMapping("inventories/{steamId}")
    public InventoryWithOperationsDTO get(@PathVariable @SteamIdConstraint Long steamId) {
        return inventoryService.get(steamId);
    }

    @Operation(summary = "Load items from steam", description = "Create an inventory and items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InventoryWithLatestOperationDTO.class))
            ),
            @ApiResponse(responseCode = "400", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SteamErrorResponse.class))
            ),
            @ApiResponse(responseCode = "409", description = "Inventory already exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class))
            )
    })
    @PostMapping("inventories/")
    @ResponseBody
    public ResponseEntity<InventoryWithLatestOperationDTO> create(@RequestBody @Valid InventoryCreateRequest inventoryCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.create(inventoryCreateRequest.getSteamId()));
    }

    @Operation(
            summary = "Reload items from steam",
            description = "Update inventory items (rate limited - once per minute for steamId)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InventoryWithLatestOperationDTO.class))
            ),
            @ApiResponse(responseCode = "400", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(oneOf = {ErrorResponse.class, SteamErrorResponse.class}))
            ),
            @ApiResponse(responseCode = "404", description = "Inventory not exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class))
            ),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("inventories/{steamId}")
    @ResponseBody
    public ResponseEntity<InventoryWithLatestOperationDTO> update(@PathVariable @SteamIdConstraint Long steamId) {
        HttpHeaders responseHeaders = rateLimitHandler.run(steamId);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(inventoryService.update(steamId));
    }

    @Operation(description = "Delete an inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Inventory not exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class))
            )
    })
    @DeleteMapping("inventories/{steamId}")
    public ResponseEntity<Inventory> delete(@PathVariable @SteamIdConstraint Long steamId) {
        inventoryService.delete(steamId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.config.AppConstant;
import com.dotashowcase.inventoryservice.http.exception.response.ErrorResponse;
import com.dotashowcase.inventoryservice.http.exception.response.ValidationErrorResponse;
import com.dotashowcase.inventoryservice.http.filter.InventoryItemFilter;
import com.dotashowcase.inventoryservice.http.request.InventoryItemSearchRequest;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.InventoryItemChangesService;
import com.dotashowcase.inventoryservice.service.InventoryItemService;
import com.dotashowcase.inventoryservice.service.InventoryService;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryChangesDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryItemDTO;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Inventory Item", description = "Get inventory items and their changes")
@Validated
@RestController
@RequestMapping("api/v1/")
public class InventoryItemController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private InventoryItemChangesService inventoryItemChangesService;

    @Operation(description = "Get a list of user's inventory items, may be filtered")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = InventoryItemDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Inventory not exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PostMapping("inventories/{steamId}/items/search")
    public List<InventoryItemDTO> index(
            @PathVariable @SteamIdConstraint Long steamId,
            @RequestBody(required=false) @Valid InventoryItemSearchRequest inventoryItemSearchRequest,
            @RequestParam Optional<String> sort
    ) {
        InventoryItemFilter filter;

        if (inventoryItemSearchRequest != null) {
            filter = new InventoryItemFilter(
                    inventoryItemSearchRequest.getDefIndexes(),
                    inventoryItemSearchRequest.getQualities(),
                    inventoryItemSearchRequest.getIsTradable(),
                    inventoryItemSearchRequest.getIsCraftable(),
                    inventoryItemSearchRequest.getIsEquipped(),
                    inventoryItemSearchRequest.getHasAttribute()
            );
        } else {
            filter = new InventoryItemFilter();
        }

        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemService.get(inventory, filter, sort.orElse(null));
    }

    @Operation(description = "Get a paged list of user's inventory items, may be filtered")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PageResult.class)))),
            @ApiResponse(responseCode = "404", description = "Inventory not exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PostMapping("inventories/{steamId}/items/search-page")
    public PageResult<InventoryItemDTO> index(
            @PathVariable @SteamIdConstraint Long steamId,
            @PageableDefault(size = AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE) Pageable pageable,
            @RequestBody(required=false) @Valid InventoryItemSearchRequest inventoryItemSearchRequest,
            @RequestParam Optional<String> sort
    ) {
        InventoryItemFilter filter;

        if (inventoryItemSearchRequest != null) {
            filter = new InventoryItemFilter(
                    inventoryItemSearchRequest.getDefIndexes(),
                    inventoryItemSearchRequest.getQualities(),
                    inventoryItemSearchRequest.getIsTradable(),
                    inventoryItemSearchRequest.getIsCraftable(),
                    inventoryItemSearchRequest.getIsEquipped(),
                    inventoryItemSearchRequest.getHasAttribute()
            );
        } else {
            filter = new InventoryItemFilter();
        }

        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemService.getPage(inventory, pageable, filter, sort.orElse(null));
    }

    @Operation(description = "Get a paged position list of user's inventory items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PageResult.class)))),
            @ApiResponse(responseCode = "404", description = "Inventory not exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @GetMapping("inventories/{steamId}/items/page-positioned")
    public PageResult<InventoryItemDTO> getPagedPositionedList(
            @PathVariable @SteamIdConstraint Long steamId,
            @RequestParam Optional<Integer> page
    ) {
        Inventory inventory = inventoryService.findInventoryWithLatestOperation(steamId);

        return inventoryItemService.getPagePositioned(inventory, page.filter((val) -> val > 0).orElse(1));
    }

    @Operation(description = "Get a unique list of all dota ids (defIndex) user's inventory items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PageResult.class)))),
            @ApiResponse(responseCode = "404", description = "Inventory not exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @GetMapping("inventories/{steamId}/items/def-indexes")
    public List<Integer> getDefIndexes(@PathVariable @SteamIdConstraint Long steamId) {
        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemService.getAllDefIndexes(inventory);
    }

//    @Operation(description = "Get list of user's inventory items grouped by operations")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", content = @Content(
//                    mediaType = "application/json",
//                    array = @ArraySchema(schema = @Schema(implementation = InventoryChangesDTO.class)))),
//            @ApiResponse(responseCode = "404", description = "Inventory not exists", content = @Content(
//                    mediaType = "application/json",
//                    schema = @Schema(implementation = ErrorResponse.class))
//            ),
//            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
//                    mediaType = "application/json",
//                    schema = @Schema(implementation = ValidationErrorResponse.class)))
//    })
//    @GetMapping("inventories/{steamId}/changes")
//    public Map<Integer, InventoryChangesDTO> getChanges(@PathVariable @SteamIdConstraint Long steamId) {
//        Inventory inventory = inventoryService.findInventory(steamId);
//
//        return inventoryItemChangesService.get(inventory);
//    }

    @Operation(description = "Get a list of user's inventory items by an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = InventoryChangesDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Inventory not exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @GetMapping("inventories/{steamId}/changes/{version}")
    public InventoryChangesDTO getChanges(
            @PathVariable @SteamIdConstraint Long steamId,
            @PathVariable Integer version
    ) {
        Inventory inventory = inventoryService.findInventory(steamId);

        return inventoryItemChangesService.get(inventory, version == -1 ? null : version);
    }
}

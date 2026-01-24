package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.config.AppConstant;
import com.dotashowcase.inventoryservice.http.exception.response.ErrorResponse;
import com.dotashowcase.inventoryservice.http.exception.response.ValidationErrorResponse;
import com.dotashowcase.inventoryservice.http.filter.InventoryOperationFilter;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.InventoryService;
import com.dotashowcase.inventoryservice.service.OperationService;
import com.dotashowcase.inventoryservice.service.result.dto.OperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import com.dotashowcase.inventoryservice.support.validator.SteamIdConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "InventoryOperation", description = "The inventory operation API")
@Validated
@RestController
@RequestMapping("api/v1/")
public class InventoryOperationController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OperationService operationService;

    @Operation(description = "Get a paged list of inventory operations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = OperationDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Inventory not exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "422", description = "Validation failed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @GetMapping("inventories/{steamId}/operations/search-page")
    public PageResult<OperationDTO> search(
            @PathVariable @SteamIdConstraint Long steamId,
            @PageableDefault(size = AppConstant.DEFAULT_INVENTORY_ITEMS_PER_PAGE) Pageable pageable,
            @RequestParam(defaultValue = "-version") String sort,
            @RequestParam Optional<Boolean> hasChanges
    ) {
        Inventory inventory = inventoryService.findInventory(steamId);

        InventoryOperationFilter filter = new InventoryOperationFilter(hasChanges.orElse(null));

        return operationService.getPage(inventory, pageable, filter, sort);
    }
}

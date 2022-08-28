package com.dotashowcase.inventoryservice.http.controller;

import com.dotashowcase.inventoryservice.http.request.InventoryCreateRequest;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.service.InventoryService;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithLatestOperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.InventoryWithOperationsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("inventories/")
    public List<InventoryWithOperationsDTO> index(@RequestParam(defaultValue = "-steamId") String sort) {
        return this.inventoryService.getAll(sort);
    }

    @GetMapping("inventories/{steamId}")
    public InventoryWithOperationsDTO get(@PathVariable Long steamId) {
        return this.inventoryService.get(steamId);
    }

    @PostMapping("inventories/")
    @ResponseBody
    public InventoryWithLatestOperationDTO create(@RequestBody InventoryCreateRequest inventoryCreateRequest) {
        return this.inventoryService.create(inventoryCreateRequest.getSteamId());
    }

    @PutMapping("inventories/{steamId}")
    @ResponseBody
    public InventoryWithLatestOperationDTO update(@PathVariable Long steamId) {
        return this.inventoryService.update(steamId);
    }

    @DeleteMapping("inventories/{steamId}")
    public ResponseEntity<Inventory> delete(@PathVariable Long steamId) {
        this.inventoryService.delete(steamId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

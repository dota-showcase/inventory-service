package com.dotashowcase.inventoryservice.controller;

import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.request.InventoryCreateRequest;
import com.dotashowcase.inventoryservice.service.InventoryService;
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
    public List<Inventory> index(@RequestParam(defaultValue = "-steamId") String sort) {
        return this.inventoryService.getAll(sort);
    }

    @GetMapping("inventories/{steamId}")
    public Inventory get(@PathVariable Long steamId) {
        return this.inventoryService.get(steamId);
    }

    @PostMapping("inventories/")
    @ResponseBody
    public Inventory create(@RequestBody InventoryCreateRequest inventoryCreateRequest) {
        return this.inventoryService.create(inventoryCreateRequest.getSteamId());
    }

    @PutMapping("inventories/{steamId}")
    @ResponseBody
    public Inventory update(@PathVariable Long steamId) {
        return this.inventoryService.update(steamId);
    }

    @DeleteMapping("inventories/{steamId}")
    public ResponseEntity<Inventory> delete(@PathVariable Long steamId) {
        this.inventoryService.delete(steamId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

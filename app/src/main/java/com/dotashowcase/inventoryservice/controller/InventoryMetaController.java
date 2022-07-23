package com.dotashowcase.inventoryservice.controller;

import com.dotashowcase.inventoryservice.model.InventoryMeta;
import com.dotashowcase.inventoryservice.request.InventoryCreateRequest;
import com.dotashowcase.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("inventories/")
    public List<InventoryMeta> index(@RequestParam(defaultValue = "-steamId") String sort) {
        return this.inventoryService.getAll(sort);
    }

    @PostMapping("inventories/")
    @ResponseBody
    public InventoryMeta create(@RequestBody InventoryCreateRequest inventoryCreateRequest) {
        return this.inventoryService.create(inventoryCreateRequest.getSteamId());
    }
}

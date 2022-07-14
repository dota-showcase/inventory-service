package com.dotashowcase.inventoryservice.controller;

import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

//    @Value("${spring.application.name}")
//    private String appName;
//
//    @Value("${spring.profiles.active}")
//    private String activeProfile;
//
//    @GetMapping("/")
//    public String index() {
//        return "APP " + appName + " Running in " + activeProfile;
//    }

    @GetMapping("/{defindex}")
    public List<InventoryItem> index(@PathVariable int defindex) {
        return this.inventoryService.getAllByDefindex(defindex);
    }
}

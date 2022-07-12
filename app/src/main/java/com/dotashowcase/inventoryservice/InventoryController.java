package com.dotashowcase.inventoryservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}

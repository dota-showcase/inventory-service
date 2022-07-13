package com.dotashowcase.inventoryservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @GetMapping("/")
    public String index() {
        return "APP " + appName + " Running in " + activeProfile;
    }
}

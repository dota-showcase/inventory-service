package com.dotashowcase.inventoryservice.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI(@Value("${app.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info().title("Inventory Service - API docs | Dota Showcase")
                        .description("APIs to store and track changes of steam user's dota 2 inventory items.")
                        .version(appVersion));
    }
}

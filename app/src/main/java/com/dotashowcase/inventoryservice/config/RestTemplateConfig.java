package com.dotashowcase.inventoryservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    RestTemplate restTemplate(
            @Value("${env.steam.api.connect-timeout:5s}") Duration connectTimeout,
            @Value("${env.steam.api.read-timeout:30s}") Duration readTimeout
    ) {
        // default is no timeouts - hung steam call blocks forever
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return new RestTemplate(requestFactory);
    }
}

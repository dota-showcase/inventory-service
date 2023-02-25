package com.dotashowcase.inventoryservice.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

//    final CloseableHttpClient httpClient;
//
//    public RestTemplateConfig(CloseableHttpClient httpClient) {
//        this.httpClient = httpClient;
//    }
//
//    @Bean
//    public RestTemplate restTemplate() {
//       return new RestTemplate(clientHttpRequestFactory());
//    }
//
//    @Bean
//    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
//        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
//        clientHttpRequestFactory.setHttpClient(httpClient);
//
//        return clientHttpRequestFactory;
//    }
//
//    @Bean
//    public TaskScheduler taskScheduler() {
//        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//
//        scheduler.setThreadNamePrefix("poolScheduler");
//        scheduler.setPoolSize(50);
//
//        return scheduler;
//    }
}

package com.example.retail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean("classicRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

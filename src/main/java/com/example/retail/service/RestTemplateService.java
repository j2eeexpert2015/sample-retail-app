package com.example.retail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateService.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RestTemplateService(RestTemplate restTemplate, @Value("${base.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public String callExternalApi() {
        String url = baseUrl + "/delay/2";
        logger.info("Calling external API using RestTemplate at {}", url);
        return restTemplate.getForObject(url, String.class);
    }
}

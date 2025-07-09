package com.example.retail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RestClientService {

    private static final Logger logger = LoggerFactory.getLogger(RestClientService.class);

    private final RestClient restClient;
    private final String baseUrl;

    public RestClientService(RestClient restClient, @Value("${base.url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public String callExternalApi() {
        logger.info("Calling external API using RestClient at {}", baseUrl + "/delay/2");
        return restClient.get().uri("/delay/2").retrieve().body(String.class);
    }
}

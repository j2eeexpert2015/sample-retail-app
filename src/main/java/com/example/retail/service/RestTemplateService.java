// src/main/java/com/example/retail/service/RestTemplateService.java
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

    @Value("${server.port}")
    private int serverPort; // current app port

    public RestTemplateService(RestTemplate restTemplate, @Value("${base.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /** Calls an external API and logs only the total time at the end. */
    public String callExternalApi() {
        String url = baseUrl + "/delay/2";
        long start = System.nanoTime();
        String response = restTemplate.getForObject(url, String.class);
        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        logger.info("External API GET {} completed in {} ms", url, durationMs);
        return response;
    }

    /** Calls another controller in this app via HTTP and logs only the total time at the end. */
    public String callInternalEndpoint() {
        String url = "http://localhost:" + serverPort + "/order/hello";
        long start = System.nanoTime();
        String response = restTemplate.getForObject(url, String.class);
        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        logger.info("Internal API GET {} completed in {} ms", url, durationMs);
        return response;
    }
}

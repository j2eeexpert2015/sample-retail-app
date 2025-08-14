// src/main/java/com/example/retail/service/RestTemplateService.java
package com.example.retail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/*
 Uses the explicitly named RestTemplate bean "classicRestTemplate" to avoid ambiguity.
 Provides external and internal calls for baseline comparisons against RestClient variants.
*/
@Service
public class RestTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateService.class);

    private final RestTemplate classicRestTemplate; // bean: "classicRestTemplate"

    @Value("${base.url}")
    private String externalBaseUrl;

    @Value("${server.port:8080}")
    private int serverPort;

    public RestTemplateService(@Qualifier("classicRestTemplate") RestTemplate classicRestTemplate) {
        this.classicRestTemplate = classicRestTemplate;
    }

    public String callExternalUsingClassicRestTemplate() {
        String url = externalBaseUrl + "/delay/2";
        long start = System.nanoTime();
        String body = classicRestTemplate.getForObject(url, String.class);
        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        logger.info("Classic RestTemplate → external {} completed in {} ms", url, durationMs);
        return body;
    }

    public String callInternalUsingClassicRestTemplate() {
        String url = "http://localhost:" + serverPort + "/order/hello";
        long start = System.nanoTime();
        String body = classicRestTemplate.getForObject(url, String.class);
        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        logger.info("Classic RestTemplate → internal {} completed in {} ms", url, durationMs);
        return body;
    }
}

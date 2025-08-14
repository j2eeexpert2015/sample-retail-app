// src/main/java/com/example/retail/service/RestClientService.java
package com.example.retail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/*
 Uses two RestClient beans:
 - "restClient": JDK HttpClient-based; optionally uses a VT-per-task executor based on spring.threads.virtual.enabled
 - "pooledHttpRestClient": Apache HttpClient 5 with an explicit connection pool
 Exposes external and internal call methods for each to support side-by-side load testing.
*/
@Service
public class RestClientService {

    private static final Logger logger = LoggerFactory.getLogger(RestClientService.class);

    private final RestClient restClient; // bean: "restClient"
    private final RestClient pooledHttpRestClient;          // bean: "pooledHttpRestClient"

    @Value("${base.url}")
    private String externalBaseUrl;

    @Value("${server.port:8080}")
    private int serverPort;

    public RestClientService(
            @Qualifier("restClient") RestClient restClient,
            @Qualifier("pooledHttpRestClient") RestClient pooledHttpRestClient) {
        this.restClient = restClient;
        this.pooledHttpRestClient = pooledHttpRestClient;
    }

    // ---------- Virtual-thread-capable RestClient ----------
    public String callExternalUsingRestClient() {
        String suffix = "/delay/2"; // path is relative because baseUrl was set in builder
        logger.info("VT-capable RestClient → external {}{}", externalBaseUrl, suffix);
        return restClient.get().uri(suffix).retrieve().body(String.class);
    }

    public String callInternalUsingRestClient() {
        String url = "http://localhost:" + serverPort + "/order/hello";
        logger.info("VT-capable RestClient → internal {}", url);
        return restClient.get().uri(url).retrieve().body(String.class);
    }

    // ---------- Pooled-HTTP RestClient (Apache) ----------
    public String callExternalUsingPooledHttpClient() {
        String suffix = "/delay/2";
        logger.info("Pooled HTTP RestClient → external {}{}", externalBaseUrl, suffix);
        return pooledHttpRestClient.get().uri(suffix).retrieve().body(String.class);
    }

    public String callInternalUsingPooledHttpClient() {
        String url = "http://localhost:" + serverPort + "/order/hello";
        logger.info("Pooled HTTP RestClient → internal {}", url);
        return pooledHttpRestClient.get().uri(url).retrieve().body(String.class);
    }
}

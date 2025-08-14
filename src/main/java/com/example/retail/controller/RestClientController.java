// src/main/java/com/example/retail/controller/RestClientController.java
package com.example.retail.controller;

import com.example.retail.service.RestClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 Exposes RestClient-based scenarios for load testing.

 Routes
 - /restclient/external       → uses the default RestClient bean ("restClient")
 - /restclient/internal       → same default RestClient, calls an internal endpoint
 - /restclient/pooledexternal → uses the Apache pooled RestClient ("pooledHttpRestClient")
 - /restclient/pooledinternal → same pooled client, internal call

 Notes
 - The default RestClient is JDK HttpClient–backed and may attach a VT-per-task executor
   depending on spring.threads.virtual.enabled (see config).
 - The pooled client is Apache HttpClient 5 with tunable connection pool caps for
   demonstrating pool bottlenecks under load.
*/
@RestController
@RequestMapping("/restclient")
public class RestClientController {

    private static final Logger logger = LoggerFactory.getLogger(RestClientController.class);

    private final RestClientService service;

    public RestClientController(RestClientService service) {
        this.service = service;
    }

    // ---- default RestClient (JDK HttpClient–backed) ----
    @GetMapping("/external")
    public String callExternalUsingRestClient() {
        logger.info("Controller: restclient default external");
        return service.callExternalUsingRestClient();
    }

    @GetMapping("/internal")
    public String callInternalUsingRestClient() {
        logger.info("Controller: restclient default internal");
        return service.callInternalUsingRestClient();
    }

    // ---- Apache pooled RestClient ----
    @GetMapping("/pooledexternal")
    public String pooledExternal() {
        logger.info("Controller: restclient pooled external");
        return service.callExternalUsingPooledHttpClient();
    }

    @GetMapping("/pooledinternal")
    public String pooledInternal() {
        logger.info("Controller: restclient pooled internal");
        return service.callInternalUsingPooledHttpClient();
    }
}

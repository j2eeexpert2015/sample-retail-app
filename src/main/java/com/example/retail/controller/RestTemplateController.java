// src/main/java/com/example/retail/controller/RestTemplateController.java
package com.example.retail.controller;

import com.example.retail.service.RestTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 Exposes RestTemplate-based scenarios for baseline comparisons:
 - /resttemplate/classic/* → Classic RestTemplate
*/
@RestController
@RequestMapping("/resttemplate")
public class RestTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateController.class);

    private final RestTemplateService service;

    public RestTemplateController(RestTemplateService service) {
        this.service = service;
    }

    @GetMapping("external")
    public String classicExternal() {
        logger.info("Controller: resttemplate classic external");
        return service.callExternalUsingClassicRestTemplate();
    }

    @GetMapping("internal")
    public String classicInternal() {
        logger.info("Controller: resttemplate classic internal");
        return service.callInternalUsingClassicRestTemplate();
    }
}

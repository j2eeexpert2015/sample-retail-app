package com.example.retail.controller;

import com.example.retail.service.RestTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resttemplate") // Class-level mapping
public class RestTemplateController {

    private final RestTemplateService service;

    public RestTemplateController(RestTemplateService service) {
        this.service = service;
    }

    @GetMapping("/external")
    public String invokeExternalAPI() {
        return service.callExternalApi();
    }

    @GetMapping("/internal")
    public String invokeInternalEndpoint() {
        return service.callInternalEndpoint();
    }
}

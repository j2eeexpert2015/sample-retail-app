package com.example.retail.controller;

import com.example.retail.service.RestTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestTemplateController {

    private final RestTemplateService service;

    public RestTemplateController(RestTemplateService service) {
        this.service = service;
    }

    @GetMapping("/resttemplate")
    public String useRestTemplate() {
        return service.callExternalApi();
    }
}

package com.example.retail.controller;

import com.example.retail.service.RestClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restclient") // Class-level mapping
public class RestClientController {

    private final RestClientService service;

    public RestClientController(RestClientService service) {
        this.service = service;
    }

    @GetMapping("/external")
    public String callInternalEndpoint() {
        return service.callExternalApi();
    }
}

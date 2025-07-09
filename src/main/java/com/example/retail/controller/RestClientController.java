package com.example.retail.controller;

import com.example.retail.service.RestClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestClientController {

    private final RestClientService service;

    public RestClientController(RestClientService service) {
        this.service = service;
    }

    @GetMapping("/restclient")
    public String useRestClient() {
        return service.callExternalApi();
    }
}

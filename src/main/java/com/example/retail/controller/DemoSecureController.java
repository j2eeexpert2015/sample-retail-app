package com.example.retail.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoSecureController {
    @GetMapping("/security-demo/hello")
    public String hello()
    {
        return "Hello, secured world!";
    }
}

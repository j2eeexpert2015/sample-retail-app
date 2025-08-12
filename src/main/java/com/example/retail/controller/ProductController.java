package com.example.retail.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.retail.service.ProductService;

@RestController
@RequestMapping("/order")
public class ProductController {

    // Injecting the ProductService to handle business logic for orders
    @Autowired
    private ProductService productService;

    // Places an order by calling the database service
    public ResponseEntity<String> placeOrder() {
        productService.callDBService();
        return ResponseEntity.ok("success");
    }

    // HTTP GET endpoint to place an order using a product ID
    @GetMapping("/place")
    public ResponseEntity<String> placeOrder(@RequestParam String productId) {
        return placeOrder();
    }

    // HTTP GET endpoint that returns a simple "Hello World" message
    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        System.out.println("Hello world from product controller");
        return ResponseEntity.ok("Hello World");
    }
}

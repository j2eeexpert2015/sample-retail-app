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

    @Autowired
    private ProductService productService;

    @GetMapping("/place_old")
    public ResponseEntity<String> placeOrder() {
    	productService.callDBService();
        return ResponseEntity.ok("success");
    }
    
    @GetMapping("/place")
    public ResponseEntity<String> placeOrder(@RequestParam String productId) {
        return placeOrder();
    }

}
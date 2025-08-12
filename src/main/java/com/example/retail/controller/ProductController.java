package com.example.retail.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    // Logger instance for this controller
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    // Injecting the ProductService to handle business logic for orders
    @Autowired
    private ProductService productService;

    // Places an order by calling the database service
    public ResponseEntity<String> placeOrder() {
        logger.info("Processing order request");
        productService.callDBService();
        logger.info("Order processed successfully");
        return ResponseEntity.ok("success");
    }

    // HTTP GET endpoint to place an order using a product ID
    @GetMapping("/place")
    public ResponseEntity<String> placeOrder(@RequestParam String productId) {
        logger.info("Placing order for product ID: {}", productId);
        return placeOrder();
    }

    // HTTP GET endpoint that returns a simple "Hello World" message
    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() throws InterruptedException {
        logger.info("Hello endpoint called - request started");
        Thread.sleep(2000); // 2 second delay
        logger.info("Hello endpoint processing completed");
        return ResponseEntity.ok("Hello World");
    }
}

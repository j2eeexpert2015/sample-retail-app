package com.example.retail.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VirtualThreadController {
    @GetMapping("/test-virtual-threads")
    public String testVirtualThreads() {
        Thread.ofVirtual().start(() -> {
            try {
                // Simulate pinning with synchronized block
                synchronized (this) {
                    Thread.sleep(30); // Longer than 20ms to trigger pinning event
                }
                System.out.println("Virtual thread executed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        return "Virtual thread started";
    }
}

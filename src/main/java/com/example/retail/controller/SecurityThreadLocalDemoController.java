package com.example.retail.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.StructuredTaskScope;

/**
 * Demo controller showing ThreadLocal behavior with virtual threads
 * Demonstrates how Spring Security's ThreadLocal-based context doesn't propagate to child virtual threads
 */
@RestController
public class SecurityThreadLocalDemoController {
    private static final Logger log = LoggerFactory.getLogger(SecurityThreadLocalDemoController.class);

    // Baseline endpoint - shows ThreadLocal working normally on main request thread
    @GetMapping("/security-demo/threadlocal/baseline")
    public String threadLocalBaseline() {
        // This works fine - we're on the main request thread where ThreadLocal is available
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return "ThreadLocal baseline user = " + (authentication != null ? authentication.getName() : "null");
    }

    // Structured concurrency endpoint - demonstrates ThreadLocal limitation with virtual threads
    @GetMapping("/security-demo/threadlocal/structured")
    public String threadLocalStructured() throws Exception {
        // Create a structured task scope for managing virtual threads
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            // Fork a new virtual thread - this is where the problem occurs
            var sub = scope.fork(() -> {
                // PROBLEM: Child virtual thread doesn't inherit ThreadLocal values
                // SecurityContextHolder will likely return null here
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                log.info("ThreadLocal child virtual thread auth = {}", authentication);
                return "ThreadLocal user(from child thread)=" + (authentication != null ? authentication.getName() : "null");
            });

            // Wait for the virtual thread to complete
            scope.join();
            // Return the result - will show "null" demonstrating ThreadLocal failure
            return sub.get();
        }
    }
}
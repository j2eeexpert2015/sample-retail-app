package com.example.retail.controller;

import com.example.retail.security.ScopedAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.StructuredTaskScope;

/**
 * Demo controller showing ScopedValue behavior with virtual threads
 * Demonstrates how ScopedValue automatically propagates context to child virtual threads
 * Compare this with SecurityThreadLocalDemoController to see the difference
 */

@RestController
public class SecurityScopedValueDemoController {
    private static final Logger log = LoggerFactory.getLogger(SecurityScopedValueDemoController.class);

    // Baseline endpoint - shows ScopedValue working normally on main request thread
    // This endpoint proves that ScopedValue access works just like ThreadLocal on the main thread
    @GetMapping("/security-demo/scopedvalue/baseline")
    public String scopedBaseline() {
        // Access authentication through our ScopedValue instead of Spring's ThreadLocal
        // On the main request thread, this behaves identically to SecurityContextHolder
        var authentication = ScopedAuth.AUTH.get();
        return "ScopedValue baseline user = " + (authentication != null ? authentication.getName() : "null");
    }

    // Structured concurrency endpoint - demonstrates ScopedValue success with virtual threads
    // This is where ScopedValue shines compared to ThreadLocal approach
    @GetMapping("/security-demo/scopedvalue/structured")
    public String scopedStructured() throws Exception {
        // Create a structured task scope for managing virtual threads
        // This is identical code to what ThreadLocal version uses
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {

            // Fork a new virtual thread to do work in parallel
            // The magic happens here: ScopedValue automatically carries context to child thread
            var subtask = scope.fork(() -> {
                // SOLUTION: ScopedValue automatically propagates to child virtual threads
                // Unlike ThreadLocal, this will successfully return the authentication object
                // The same Authentication object bound in ScopedAuthBindingFilter is available here
                var authentication = ScopedAuth.AUTH.get();

                // Log the result to show successful context propagation
                log.info("ScopedValue child virtual thread auth= {}", authentication != null ? authentication.getName() : null);

                // Return response showing we have the actual authenticated user
                return "ScopedValue user(from child thread)=" + (authentication != null ? authentication.getName() : "null");
            });

            // Wait for the virtual thread to complete (structured concurrency pattern)
            scope.join();

            // Return the result from child thread - will show actual username
            // This demonstrates successful context propagation across virtual thread boundary
            return subtask.get();
        }
    }
}
package com.example.retail.controller;

import com.example.retail.security.ScopedAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.StructuredTaskScope;

@RestController
public class SecurityScopedValueDemoController {
    private static final Logger log = LoggerFactory.getLogger(SecurityScopedValueDemoController.class);

    @GetMapping("/security-demo/sv/threadlocal/baseline")
    public String threadLocalBaseline() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return "ThreadLocal baseline user = " + (authentication != null ? authentication.getName() : "null");
    }

    @GetMapping("/security-demo/sv/threadlocal/structured")
    public String threadLocalStructured() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            var sub = scope.fork(() -> {
                var authentication = SecurityContextHolder.getContext().getAuthentication(); // likely null in child vthread
                log.info("ThreadLocal child virtual thread auth = {}", authentication);
                return "ThreadLocal user(from child thread)=" + (authentication != null ? authentication.getName() : "null");
            });
            scope.join();
            return sub.get();
        }
    }

    @GetMapping("/security-demo/sv/scopedvalue/baseline")
    public String scopedBaseline() {
        var authentication = ScopedAuth.AUTH.get();
        return "ScopedValue baseline user = " + (authentication != null ? authentication.getName() : "null");
    }

    @GetMapping("/security-demo/sv/scopedvalue/structured")
    public String scopedStructured() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            var subtask = scope.fork(() -> {
                var authentication = ScopedAuth.AUTH.get(); // propagated to child vthread
                log.info("ScopedValue child virtual thread auth= {}", authentication != null ? authentication.getName() : null);
                return "ScopedValue user(from child thread)=" + (authentication != null ? authentication.getName() : "null");
            });
            scope.join();
            return subtask.get();
        }
    }
}

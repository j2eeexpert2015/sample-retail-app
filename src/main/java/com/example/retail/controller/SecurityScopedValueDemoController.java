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

    // Moved under /security-demo/sv/** to avoid clashes with existing controller(s)
    @GetMapping("/security-demo/sv/threadlocal/baseline")
    public String threadLocalBaseline() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        return "ThreadLocal baseline user = " + (a != null ? a.getName() : "null");
    }

    @GetMapping("/security-demo/sv/threadlocal/structured")
    public String threadLocalStructured() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            var sub = scope.fork(() -> {
                var a = SecurityContextHolder.getContext().getAuthentication(); // likely null in child vthread
                log.info("[ThreadLocal] child vthread auth = {}", a);
                return "child(ThreadLocal) user=" + (a != null ? a.getName() : "null");
            });
            scope.join();
            return sub.get();
        }
    }

    @GetMapping("/security-demo/sv/scopedvalue/baseline")
    public String scopedBaseline() {
        var a = ScopedAuth.AUTH.get();
        return "ScopedValue baseline user = " + (a != null ? a.getName() : "null");
    }

    @GetMapping("/security-demo/sv/scopedvalue/structured")
    public String scopedStructured() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            var sub = scope.fork(() -> {
                var a = ScopedAuth.AUTH.get(); // propagated to child vthread
                log.info("[ScopedValue] child vthread user = {}", a != null ? a.getName() : null);
                return "child(ScopedValue) user=" + (a != null ? a.getName() : "null");
            });
            scope.join();
            return sub.get();
        }
    }
}

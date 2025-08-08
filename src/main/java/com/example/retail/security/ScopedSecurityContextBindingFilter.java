package com.example.retail.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.lang.ScopedValue;

/**
 * Binds a fresh SecurityContext to a ScopedValue for the lifetime of each HTTP request.
 * Uses ScopedValue.where(...).call(...) so we can propagate checked exceptions cleanly.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class ScopedSecurityContextBindingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // Install our ScopedValue-backed strategy first
        SecurityContextHolder.setContextHolderStrategy(new ScopedSecurityContextHolderStrategy());

        // Create the per-request context DIRECTLY to avoid recursion
        SecurityContext requestContext = new SecurityContextImpl();

        try {
            // Bind the context for this request and execute the filter chain
            ScopedValue.where(ScopedSecurityContextHolderStrategy.CONTEXT, requestContext)
                    .call(() -> {
                        try {
                            chain.doFilter(req, res);
                        } finally {
                            // Clear fallback storage to avoid leaks on reused threads
                            SecurityContextHolder.clearContext();
                        }
                        return null; // Callable requires a return
                    });
        } catch (Exception e) {
            if (e instanceof ServletException se) throw se;
            if (e instanceof IOException ie) throw ie;
            throw new RuntimeException(e);
        }
    }
}

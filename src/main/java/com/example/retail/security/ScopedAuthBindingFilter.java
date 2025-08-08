package com.example.retail.security;

import com.example.retail.security.ScopedAuth;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** Binds the current Authentication into a ScopedValue for the duration of a request. */
@Component
public class ScopedAuthBindingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ScopedValue.where(ScopedAuth.AUTH, auth).run(() -> {
            try {
                chain.doFilter(req, res);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

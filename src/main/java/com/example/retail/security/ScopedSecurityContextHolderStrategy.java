package com.example.retail.security;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextImpl;

import java.lang.ScopedValue;

/**
 * SecurityContextHolderStrategy backed by a ScopedValue (with a tiny ThreadLocal fallback).
 * Prefer the per-request ScopedValue; fall back to ThreadLocal only for out-of-scope callers.
 */
public final class ScopedSecurityContextHolderStrategy implements SecurityContextHolderStrategy {

    // Per-request scoped slot (bound by ScopedSecurityContextBindingFilter)
    static final ScopedValue<SecurityContext> CONTEXT = ScopedValue.newInstance();

    // Fallback for rare calls outside a bound scope
    private static final ThreadLocal<SecurityContext> FALLBACK = new ThreadLocal<>();

    @Override
    public void clearContext() {
        // ScopedValue unbinds automatically at scope end; just clear fallback to avoid leaks
        FALLBACK.remove();
    }

    @Override
    public SecurityContext getContext() {
        // Prefer request-scoped context if bound
        if (CONTEXT.isBound()) {
            return CONTEXT.get();
        }
        // Else fallback (may be null)
        SecurityContext fallbackCtx = FALLBACK.get();
        if (fallbackCtx != null) {
            return fallbackCtx;
        }
        // Else create a fresh empty context (DO NOT call SecurityContextHolder here)
        return new SecurityContextImpl();
    }

    @Override
    public void setContext(SecurityContext context) {
        // ScopedValue has no "set"; keep outside-scope writes in fallback
        FALLBACK.set(context);
    }

    @Override
    public SecurityContext createEmptyContext() {
        // Return a new instance directly to avoid recursion
        return new SecurityContextImpl();
    }
}

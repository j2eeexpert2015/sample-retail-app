package com.example.retail.security;

import org.springframework.security.core.Authentication;

// Holder for Authentication bound to a ScopedValue
public final class ScopedAuth {
    private ScopedAuth() {}
    public static final ScopedValue<Authentication> AUTH = ScopedValue.newInstance();
}

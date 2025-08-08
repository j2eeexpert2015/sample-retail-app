package com.example.retail.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration // Spring configuration class
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityDemoFilterChain (HttpSecurity http) throws Exception {
        /*
         * This configuration is tied to our ScopedValue-based SecurityContextHolderStrategy.
         *
         * The key part: We add ScopedSecurityContextBindingFilter BEFORE the default
         * SecurityContextHolderFilter. This ensures:
         *   1. A fresh SecurityContext is bound into a ScopedValue at the start of the request.
         *   2. That ScopedValue is automatically cleared when the scope ends (end of request).
         *   3. Any code running in child virtual threads or structured tasks inherits the same
         *      security context without manual propagation.
         *
         * Without this filter, Spring Security would still rely on its default ThreadLocal.
         */

        http
                // Restrict this config to our demo endpoints
                .securityMatcher("/security-demo/**")

                // Require authentication for all demo endpoints
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())

                // Use HTTP Basic authentication for simplicity
                .httpBasic(Customizer.withDefaults())

                // Disable CSRF for demo purposes only
                .csrf(csrf -> csrf.disable())

                // Bind ScopedValue context before Spring Security's holder filter
                //.addFilterBefore(new ScopedSecurityContextBindingFilter(),
                        //SecurityContextHolderFilter.class);
                ;
        return http.build();
    }
}

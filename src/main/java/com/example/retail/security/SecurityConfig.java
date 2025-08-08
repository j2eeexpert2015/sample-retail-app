package com.example.retail.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration // Spring configuration class
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityDemoFilterChain(HttpSecurity http) throws Exception {
        http
                // Restrict this config to our demo endpoints
                .securityMatcher("/security-demo/**")

                // Require authentication for all demo endpoints
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())

                // Use HTTP Basic authentication for simplicity
                .httpBasic(Customizer.withDefaults())

                // Disable CSRF for demo/demo
                .csrf(csrf -> csrf.disable())

                /*
                 * Bind Authentication into a ScopedValue AFTER BasicAuthenticationFilter
                 * so Authentication is already populated for the current request.
                 */
                .addFilterAfter(scopedAuthBindingFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    ScopedAuthBindingFilter scopedAuthBindingFilter() {
        return new ScopedAuthBindingFilter();
    }
}

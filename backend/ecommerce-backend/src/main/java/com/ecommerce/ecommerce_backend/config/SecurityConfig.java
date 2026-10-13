package com.ecommerce.ecommerce_backend.config;

import com.ecommerce.ecommerce_backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for REST API
                .csrf(csrf -> csrf.disable())

                // JWT based authentication (no session)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // ==========================
                        // PUBLIC APIs
                        // ==========================


                        .requestMatchers(
        "/api/auth/**",
        "/actuator/health",
        "/actuator/health/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/error"
).permitAll()

                        // ==========================
                        // PRODUCT APIs
                        // ==========================
                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                        .hasAnyRole("ADMIN", "CUSTOMER")

                        .requestMatchers(HttpMethod.POST, "/api/products/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/products/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                        .hasRole("ADMIN")

                        // ==========================
                        // CATEGORY APIs
                        // ==========================
                        .requestMatchers(HttpMethod.GET, "/api/categories/**")
                        .hasAnyRole("ADMIN", "CUSTOMER")

                        .requestMatchers(HttpMethod.POST, "/api/categories/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/categories/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**")
                        .hasRole("ADMIN")

                        // ==========================
                        // CART APIs
                        // ==========================
                        .requestMatchers("/api/cart/**")
                        .hasAnyRole("ADMIN", "CUSTOMER")

                        // ==========================
                        // ORDER APIs
                        // ==========================

                        // Admin order APIs (must come before the general orders rule)
                        .requestMatchers("/api/orders/admin/**")
                        .hasRole("ADMIN")

                        // Normal order APIs
                        .requestMatchers("/api/orders/**")
                        .hasAnyRole("ADMIN", "CUSTOMER")

                        // ==========================
                        // USER MANAGEMENT APIs
                        // ==========================
                        .requestMatchers("/api/users/admin/**")
                        .hasRole("ADMIN")

                        // ==========================
                        // OTHER APIs
                        // ==========================
                        .anyRequest()
                        .authenticated()
                )

                // Add JWT filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

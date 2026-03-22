package com.dsar.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration:
 * - HTTP Basic Auth (stateless)
 * - Role-based access: ADMIN and CUSTOMER
 * - CORS configured for local dev (Vue on :5173)
 *
 * Seeded users:
 *   Bhargava  / myinfy124   → ROLE_ADMIN
 *   Pavan     / pavan@123   → ROLE_CUSTOMER
 *   Nagaraju  / nag@123     → ROLE_CUSTOMER
 *   Krishna   / krishna@123 → ROLE_CUSTOMER
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var manager = new InMemoryUserDetailsManager();

        manager.createUser(User.withUsername("Bhargava")
                .password(encoder.encode("myinfy124"))
                .roles("ADMIN")
                .build());

        manager.createUser(User.withUsername("Pavan")
                .password(encoder.encode("pavan@123"))
                .roles("CUSTOMER")
                .build());

        manager.createUser(User.withUsername("Nagaraju")
                .password(encoder.encode("nag@123"))
                .roles("CUSTOMER")
                .build());

        manager.createUser(User.withUsername("Krishna")
                .password(encoder.encode("krishna@123"))
                .roles("CUSTOMER")
                .build());

        return manager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth info endpoint
                .requestMatchers("/api/auth/me").authenticated()
                // Customers submit; admins can GET all
                .requestMatchers(HttpMethod.POST,   "/api/requests").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET,    "/api/requests").authenticated()
                .requestMatchers(HttpMethod.GET,    "/api/requests/**").authenticated()
                // Customers can edit their own PENDING request description
                .requestMatchers(HttpMethod.PATCH,  "/api/requests/**").hasRole("CUSTOMER")
                // Only admins can change status
                .requestMatchers(HttpMethod.PUT,    "/api/requests/**").hasRole("ADMIN")
                // Audit trail is admin-only
                .requestMatchers("/api/audit/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // Suppress WWW-Authenticate header so the browser does NOT show
            // its native Basic Auth dialog — the Vue app handles login instead.
            .httpBasic(basic -> basic.authenticationEntryPoint(
                (request, response, ex) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\"}");
                }
            ));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

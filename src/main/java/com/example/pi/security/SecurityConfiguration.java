package com.example.pi.security;

import com.example.pi.filter.RateLimitingFilter;
import com.example.pi.filter.RequestResponseLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Spring Security configuration.
 *
 * Improvements:
 *  - All URL patterns updated to /api/v1/** (doc item #40)
 *  - Constructor injection (doc item #30)
 *  - Management endpoints secured — only health, info, metrics, prometheus exposed (doc item #50)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(UserDetailsService userDetailsService,
                                  JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints — public (login, public-register, refresh)
                .requestMatchers("/api/v1/auth/login",
                                 "/api/v1/auth/public-register",
                                 "/api/v1/auth/refresh").permitAll()
                // Logout requires a valid JWT
                .requestMatchers("/api/v1/auth/logout").authenticated()
                // Admin register requires JWT
                .requestMatchers("/api/v1/auth/register").hasRole("ADMIN")
                // Actuator health/info — public, others secured (doc item #50)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                // Admin-only customer management
                .requestMatchers(
                        org.springframework.http.HttpMethod.POST, "/api/v1/customers")
                    .hasRole("ADMIN")
                .requestMatchers(
                        org.springframework.http.HttpMethod.PUT,    "/api/v1/customers/**")
                    .hasRole("ADMIN")
                .requestMatchers(
                        org.springframework.http.HttpMethod.DELETE, "/api/v1/customers/**")
                    .hasRole("ADMIN")
                .requestMatchers(
                        org.springframework.http.HttpMethod.GET,
                        "/api/v1/customers",
                        "/api/v1/customers/filter/**",
                        "/api/v1/customers/transactions/all",
                        "/api/v1/customers/transactions/latest",
                        "/api/v1/customers/Banking")
                    .hasRole("ADMIN")
                // User + admin: own account operations
                .requestMatchers("/api/v1/customers/*/transactions",
                                 "/api/v1/customers/*/balance",
                                 "/api/v1/customers/*/payment",
                                 "/api/v1/customers/transactions/mode/**")
                    .hasAnyRole("ADMIN", "USER")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    /**
     * Register RequestResponseLoggingFilter as a FilterRegistrationBean.
     * This prevents Spring AOP from trying to proxy the Servlet Filter.
     * Order = -100 ensures it runs before other filters.
     */
    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> requestResponseLoggingFilterRegistration() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registration =
            new FilterRegistrationBean<>(new RequestResponseLoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(-100);
        return registration;
    }

    /**
     * Rate limiting filter — 100 requests per minute per IP (doc item #64).
     * Registered before the JWT filter so unauthenticated floods are rejected early.
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration() {
        FilterRegistrationBean<RateLimitingFilter> registration =
            new FilterRegistrationBean<>(new RateLimitingFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(-90);
        return registration;
    }
}

package com.example.pi.security;

import com.example.pi.filter.RequestResponseLoggingFilter;
import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/customers/AddCustomers").hasRole("ADMIN")
                .requestMatchers("/customers/UpdateCustomer", "/customers/DeleteCustomer/**")
                    .hasRole("ADMIN")
                .requestMatchers("/customers/DisplayAllCustomers",
                                 "/customers/FindByCustomerID/**",
                                 "/customers/FilterCustomersByIDPoof/**",
                                 "/customers/AllTransactionsHistory",
                                 "/customers/LatestTransactions")
                    .hasRole("ADMIN")
                .requestMatchers("/customers/Ministatement/**",
                                 "/customers/CheckBalance/**",
                                 "/customers/CheckTransactionMode/**",
                                 "/customers/Banking")
                    .hasAnyRole("ADMIN", "USER")
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
        return http.getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder())
            .and()
            .build();
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
        registration.setOrder(-100); // Run this filter first
        return registration;
    }
}

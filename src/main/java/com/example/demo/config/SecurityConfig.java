package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter; // Adjust package if needed
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Make sure this class exists!

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/api/users/register",
                    "/api/users/login",
                    "/h2-console/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions().disable());

        // Register your JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

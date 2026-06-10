package com.example.project.config;

import com.example.project.repository.TokenBlacklistRepository;
import com.example.project.repository.UserRepository;
import com.example.project.security.JwtAuthenticationFilter;
import com.example.project.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final UserRepository userRepository;

    @Autowired
    public SecurityConfig(JwtUtils jwtUtils,
                          TokenBlacklistRepository tokenBlacklistRepository,
                          UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtils, tokenBlacklistRepository, userRepository);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login", "/api/v1/auth/login",
                                "/api/auth/register", "/api/v1/auth/register",
                                "/api/auth/refresh", "/api/v1/auth/refresh",
                                "/api/auth/logout", "/api/v1/auth/logout",
                                "/api/auth/forgot-password", "/api/v1/auth/forgot-password",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/change-password", "/api/v1/auth/change-password").authenticated()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/api/v1/patient/**").hasRole("PATIENT")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // For H2 console (if used) frameOptions
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        try {
            return authConfig.getAuthenticationManager();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}


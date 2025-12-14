package com.ms.user.config;

import com.ms.user.auth.JwtAuthenticationFilter;
import com.ms.user.auth.ServiceAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ServiceAuthenticationFilter serviceAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (Swagger/OpenAPI)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        
                        // Service-to-service endpoints (require SERVICE role via X-Service-Secret header)
                        .requestMatchers("POST", "/users").hasAnyRole("SERVICE", "ADMIN") // Para Auth Service - registro de usuários
                        .requestMatchers("POST", "/users/validate-credentials").hasAnyRole("SERVICE", "ADMIN") // Para Auth Service - validação de credenciais
                        .requestMatchers("GET", "/users/email/{email}").hasAnyRole("SERVICE", "ADMIN") // Para Auth Service - buscar por email
                        .requestMatchers("GET", "users/{id}").hasAnyRole("SERVICE", "ADMIN") // Para Auth Service - buscar por id
                        
                        // User endpoints (USER role can access /me)
                        .requestMatchers("GET", "/users/me").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("PUT", "/users/me").hasAnyRole("USER", "ADMIN")
                        
                        // Admin endpoints (all other endpoints require ADMIN role)
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(serviceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

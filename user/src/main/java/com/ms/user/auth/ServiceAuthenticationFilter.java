package com.ms.user.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filter for service-to-service authentication using a shared secret header.
 * This allows internal services (like Auth Service) to access protected endpoints
 * without exposing them publicly.
 */
@Component
@RequiredArgsConstructor
public class ServiceAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.service.secret:}")
    private String serviceSecret;

    private static final String SERVICE_SECRET_HEADER = "X-Service-Secret";
    private static final String SERVICE_ROLE = "SERVICE";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Only process if service secret is configured
        if (serviceSecret == null || serviceSecret.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String serviceSecretHeader = request.getHeader(SERVICE_SECRET_HEADER);

        // If service secret header is present and valid, authenticate as SERVICE
        if (serviceSecretHeader != null && serviceSecretHeader.equals(serviceSecret)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    "internal-service",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + SERVICE_ROLE))
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}


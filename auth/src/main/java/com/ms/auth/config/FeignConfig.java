package com.ms.auth.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign configuration to add service-to-service authentication header
 * for communication with User Service.
 */
@Configuration
public class FeignConfig {

    @Value("${app.service.secret:}")
    private String serviceSecret;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Add service secret header for service-to-service authentication
            if (serviceSecret != null && !serviceSecret.isBlank()) {
                requestTemplate.header("X-Service-Secret", serviceSecret);
            }
        };
    }
}


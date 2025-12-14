package com.ms.order.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

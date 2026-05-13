package com.atm.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("lb://auth-service"))
                
                // ATM Management Service Routes
                .route("atm-management-service", r -> r.path("/api/atm/**")
                        .uri("lb://atm-management-service"))
                
                // Alert Processor Service Routes
                .route("alert-processor-service", r -> r.path("/api/alerts/**")
                        .uri("lb://alert-processor-service"))
                
                // Notification Service Routes
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .uri("lb://notification-service"))
                
                // WebSocket Routes (for Notification Service)
                .route("notification-websocket", r -> r.path("/ws-alerts/**")
                        .uri("lb://notification-service"))
                
                // Eureka Server Routes (for admin access)
                .route("eureka-server", r -> r.path("/eureka/**")
                        .uri("http://localhost:8761"))
                
                .build();
    }
}

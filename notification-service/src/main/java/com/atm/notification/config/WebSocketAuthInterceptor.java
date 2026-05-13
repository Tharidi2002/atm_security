package com.atm.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        // Extract JWT token from query parameters or headers
        String token = extractToken(request);
        
        if (token == null) {
            log.warn("WebSocket connection attempt without JWT token");
            return false;
        }
        
        try {
            // For now, we'll use a simple token validation
            // In production, you'd validate the JWT token properly
            Long userId = validateToken(token);
            
            if (userId != null) {
                attributes.put("userId", userId);
                log.info("WebSocket handshake approved for user: {}", userId);
                return true;
            }
        } catch (Exception e) {
            log.error("Token validation failed during WebSocket handshake", e);
        }
        
        return false;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket handshake failed", exception);
        }
    }
    
    private String extractToken(ServerHttpRequest request) {
        // Try to get token from query parameter
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }
        
        // Try to get token from Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }
    
    private Long validateToken(String token) {
        // Mock validation - in production, validate JWT properly
        // For testing, accept tokens like "user-123" and return 123
        if (token.startsWith("user-")) {
            try {
                return Long.parseLong(token.substring(5));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // For development, accept any token and return user 1
        if (token.equals("dev-token")) {
            return 1L;
        }
        
        return null;
    }
}

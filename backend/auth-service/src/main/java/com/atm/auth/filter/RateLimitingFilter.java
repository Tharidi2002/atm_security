package com.atm.auth.filter;

import com.atm.auth.service.RateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1) // High priority to execute before other filters
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Autowired
    private RateLimitingService rateLimitingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
        
        String clientKey = rateLimitingService.getClientKey(request);
        String requestURI = request.getRequestURI();
        
        // Skip rate limiting for health checks and static resources
        if (isExemptPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check different rate limits based on endpoint
        boolean allowed = true;
        String limitType = "minute";
        
        if (isAuthEndpoint(requestURI)) {
            // Stricter limits for authentication endpoints
            allowed = rateLimitingService.isAllowed(clientKey, "minute") &&
                     rateLimitingService.isAllowed(clientKey, "hour") &&
                     rateLimitingService.isAllowed(clientKey, "day");
            limitType = "auth";
        } else {
            // Standard limits for other endpoints
            allowed = rateLimitingService.isAllowed(clientKey, "minute");
            limitType = "standard";
        }
        
        if (!allowed) {
            logger.warn("Rate limit exceeded for client: {} on endpoint: {}", clientKey, requestURI);
            
            // Set rate limit headers
            response.setHeader("X-RateLimit-Limit", String.valueOf(getLimitForType(limitType)));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(getResetTimeForType(limitType)));
            
            // Return 429 Too Many Requests
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Rate limit exceeded\", " +
                "\"message\": \"Too many requests. Please try again later.\", " +
                "\"limitType\": \"" + limitType + "\"}"
            );
            return;
        }
        
        // Add rate limit headers for successful requests
        long remaining = rateLimitingService.getRemainingRequests(clientKey, "minute");
        response.setHeader("X-RateLimit-Limit", String.valueOf(getLimitForType(limitType)));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(getResetTimeForType(limitType)));
        
        filterChain.doFilter(request, response);
    }

    private boolean isExemptPath(String requestURI) {
        return requestURI.contains("/actuator") ||
               requestURI.contains("/health") ||
               requestURI.contains("/favicon") ||
               requestURI.endsWith(".css") ||
               requestURI.endsWith(".js") ||
               requestURI.endsWith(".png") ||
               requestURI.endsWith(".jpg") ||
               requestURI.endsWith(".gif");
    }

    private boolean isAuthEndpoint(String requestURI) {
        return requestURI.contains("/api/auth/") ||
               requestURI.contains("/signin") ||
               requestURI.contains("/signup") ||
               requestURI.contains("/login") ||
               requestURI.contains("/register");
    }

    private int getLimitForType(String limitType) {
        switch (limitType) {
            case "auth":
                return 20; // Stricter limit for auth endpoints
            case "minute":
                return 100;
            case "hour":
                return 1000;
            case "day":
                return 10000;
            default:
                return 100;
        }
    }

    private long getResetTimeForType(String limitType) {
        long currentTime = System.currentTimeMillis();
        switch (limitType) {
            case "minute":
                return currentTime + 60000; // 1 minute
            case "hour":
                return currentTime + 3600000; // 1 hour
            case "day":
                return currentTime + 86400000; // 24 hours
            default:
                return currentTime + 60000; // 1 minute
        }
    }
}

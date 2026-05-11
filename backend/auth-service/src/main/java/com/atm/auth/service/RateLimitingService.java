package com.atm.auth.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitingService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingService.class);

    @Value("${atm.app.rateLimit.requestsPerMinute:100}")
    private int requestsPerMinute;

    @Value("${atm.app.rateLimit.requestsPerHour:1000}")
    private int requestsPerHour;

    @Value("${atm.app.rateLimit.requestsPerDay:10000}")
    private int requestsPerDay;

    // Caffeine cache for rate limiters
    private final Cache<String, RateLimiter> rateLimiterCache;

    public RateLimitingService() {
        this.rateLimiterCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    public boolean isAllowed(String key, String limitType) {
        RateLimiter rateLimiter = getRateLimiter(key, limitType);
        
        try {
            boolean allowed = rateLimiter.acquirePermission();
            if (!allowed) {
                logger.warn("Rate limit exceeded for key: {} - {}", key, limitType);
            }
            return allowed;
        } catch (Exception e) {
            logger.error("Error checking rate limit for key: {}", key, e);
            return true; // Fail open for security
        }
    }

    public long getRemainingRequests(String key, String limitType) {
        RateLimiter rateLimiter = getRateLimiter(key, limitType);
        
        try {
            RateLimiter.Metrics metrics = rateLimiter.getMetrics();
            return (long) metrics.getAvailablePermissions();
        } catch (Exception e) {
            logger.error("Error getting remaining requests for key: {}", key, e);
            return 0;
        }
    }

    public long getWaitTime(String key, String limitType) {
        RateLimiter rateLimiter = getRateLimiter(key, limitType);
        
        try {
            Duration waitTime = Duration.ofMillis(rateLimiter.reservePermission());
            return waitTime.toMillis();
        } catch (Exception e) {
            logger.error("Error getting wait time for key: {}", key, e);
            return 0;
        }
    }

    private RateLimiter getRateLimiter(String key, String limitType) {
        String cacheKey = key + ":" + limitType;
        
        return rateLimiterCache.get(cacheKey, k -> {
            RateLimiterConfig config;
            
            switch (limitType.toLowerCase()) {
                case "minute":
                    config = RateLimiterConfig.custom()
                            .limitForPeriod(requestsPerMinute)
                            .limitRefreshPeriod(Duration.ofMinutes(1))
                            .timeoutDuration(Duration.ofSeconds(1))
                            .build();
                    break;
                case "hour":
                    config = RateLimiterConfig.custom()
                            .limitForPeriod(requestsPerHour)
                            .limitRefreshPeriod(Duration.ofHours(1))
                            .timeoutDuration(Duration.ofSeconds(1))
                            .build();
                    break;
                case "day":
                    config = RateLimiterConfig.custom()
                            .limitForPeriod(requestsPerDay)
                            .limitRefreshPeriod(Duration.ofDays(1))
                            .timeoutDuration(Duration.ofSeconds(1))
                            .build();
                    break;
                default:
                    config = RateLimiterConfig.custom()
                            .limitForPeriod(requestsPerMinute)
                            .limitRefreshPeriod(Duration.ofMinutes(1))
                            .timeoutDuration(Duration.ofSeconds(1))
                            .build();
                    break;
            }
            
            return RateLimiter.of(cacheKey, config);
        });
    }

    public String getClientKey(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        // Create a unique key based on IP and User-Agent
        return clientIp + ":" + (userAgent != null ? userAgent.hashCode() : "unknown");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    public void cleanupExpiredLimiters() {
        // Caffeine automatically handles cleanup based on expireAfterWrite
        // This method can be called periodically for manual cleanup if needed
        logger.debug("Rate limiter cleanup completed");
    }
}

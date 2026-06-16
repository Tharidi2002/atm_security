package com.atmsecurity.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    @Value("${gateway.rate-limit.requests-per-minute:120}")
    private int requestsPerMinute;

    private final Map<String, RateBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientKey = resolveClientKey(exchange);
        RateBucket bucket = buckets.computeIfAbsent(clientKey, k -> new RateBucket(requestsPerMinute));

        if (!bucket.tryConsume()) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private String resolveClientKey(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private static class RateBucket {
        private final int maxRequests;
        private final AtomicInteger counter = new AtomicInteger(0);
        private volatile long windowStart = Instant.now().getEpochSecond();

        RateBucket(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        synchronized boolean tryConsume() {
            long now = Instant.now().getEpochSecond();
            if (now - windowStart >= 60) {
                windowStart = now;
                counter.set(0);
            }
            return counter.incrementAndGet() <= maxRequests;
        }
    }
}

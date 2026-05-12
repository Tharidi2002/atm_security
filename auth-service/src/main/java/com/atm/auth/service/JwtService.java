package com.atm.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public JwtService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateToken(String username, Long userId, String role, Long bankId) {
        Instant now = Instant.now();
        
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("bankId", bankId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtExpiration, ChronoUnit.MILLIS)))
                .signWith(getSigningKey())
                .compact();
    }
    
    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        String tokenId = UUID.randomUUID().toString();
        
        String refreshToken = Jwts.builder()
                .subject(username)
                .claim("tokenId", tokenId)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshExpiration, ChronoUnit.MILLIS)))
                .signWith(getSigningKey())
                .compact();
        
        // Store refresh token in Redis
        String redisKey = "refresh_token:" + username + ":" + tokenId;
        redisTemplate.opsForValue().set(redisKey, refreshToken, refreshExpiration, TimeUnit.MILLISECONDS);
        
        return refreshToken;
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.warn("JWT token security exception: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token claims string is empty: {}", e.getMessage());
            return false;
        }
    }
    
    public Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }
    
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
    
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }
    
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
    
    public Long extractBankId(String token) {
        return extractClaims(token).get("bankId", Long.class);
    }
    
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
    
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }
    
    public void invalidateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            String username = claims.getSubject();
            
            // Add token to blacklist in Redis
            String jti = claims.getId();
            if (jti == null) {
                jti = UUID.randomUUID().toString();
            }
            
            long expiration = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (expiration > 0) {
                String blacklistKey = "blacklist_token:" + jti;
                redisTemplate.opsForValue().set(blacklistKey, "true", expiration, TimeUnit.MILLISECONDS);
            }
            
            log.info("Token invalidated for user: {}", username);
        } catch (Exception e) {
            log.error("Error invalidating token: {}", e.getMessage());
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        try {
            Claims claims = extractClaims(token);
            String jti = claims.getId();
            
            if (jti != null) {
                String blacklistKey = "blacklist_token:" + jti;
                return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
            }
        } catch (Exception e) {
            log.error("Error checking token blacklist: {}", e.getMessage());
        }
        return false;
    }
    
    public boolean validateRefreshToken(String token) {
        if (!isRefreshToken(token) || !validateToken(token)) {
            return false;
        }
        
        try {
            Claims claims = extractClaims(token);
            String username = claims.getSubject();
            String tokenId = claims.get("tokenId", String.class);
            
            // Check if refresh token exists in Redis
            String redisKey = "refresh_token:" + username + ":" + tokenId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.error("Error validating refresh token: {}", e.getMessage());
            return false;
        }
    }
    
    public void invalidateRefreshToken(String token) {
        try {
            Claims claims = extractClaims(token);
            String username = claims.getSubject();
            String tokenId = claims.get("tokenId", String.class);
            
            // Remove refresh token from Redis
            String redisKey = "refresh_token:" + username + ":" + tokenId;
            redisTemplate.delete(redisKey);
            
            log.info("Refresh token invalidated for user: {}", username);
        } catch (Exception e) {
            log.error("Error invalidating refresh token: {}", e.getMessage());
        }
    }
}

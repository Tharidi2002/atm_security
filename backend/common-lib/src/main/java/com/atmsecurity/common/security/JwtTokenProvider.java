package com.atmsecurity.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username, String role, Long bankId, List<String> permissions) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("bankId", bankId)
                .claim("permissions", permissions)
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpirationMs());

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(username)
                .claim("userId", userId)
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public Long getUserId(String token) {
        return getClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String getRole(String token) {
        return getClaim(token, claims -> claims.get("role", String.class));
    }

    public Long getBankId(String token) {
        return getClaim(token, claims -> claims.get("bankId", Long.class));
    }

    public String getTokenType(String token) {
        return getClaim(token, claims -> claims.get("type", String.class));
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissions(String token) {
        return getClaim(token, claims -> (List<String>) claims.get("permissions"));
    }

    public Map<String, Object> extractClaims(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return claims;
    }

    private <T> T getClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return resolver.apply(claims);
    }
}

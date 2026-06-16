package com.atmsecurity.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpirationMs = 900000;
    private long refreshTokenExpirationMs = 604800000;
    private String issuer = "atm-security-system";
}

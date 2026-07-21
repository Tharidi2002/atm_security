package com.security.alarm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class WebConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // ============================================================
        // LOCAL DEVELOPMENT - Allow all for testing
        // ============================================================
        config.setAllowedOrigins(Arrays.asList("*"));
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        
        // ============================================================
        // PRODUCTION - Uncomment for production
        // ============================================================
        // config.setAllowedOrigins(Arrays.asList(
        //     "https://alarm-security-system-java.vercel.app",
        //     "https://alarm-security-system-jawa.vercel.app"
        // ));
        // config.setAllowedOriginPatterns(Arrays.asList(
        //     "https://alarm-security-system-java.vercel.app",
        //     "https://alarm-security-system-jawa.vercel.app"
        // ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
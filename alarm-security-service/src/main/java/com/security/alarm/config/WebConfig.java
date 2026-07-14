package com.security.alarm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ============================================================
        // ALLOW ALL ORIGINS - For Production (Temporary)
        // ============================================================
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // ============================================================
        // ALLOW ALL METHODS
        // ============================================================
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
        
        // ============================================================
        // ALLOW ALL HEADERS
        // ============================================================
        configuration.setAllowedHeaders(List.of("*"));
        
        // ============================================================
        // EXPOSE ALL HEADERS
        // ============================================================
        configuration.setExposedHeaders(List.of("*"));
        
        // ============================================================
        // CREDENTIALS - False for now
        // ============================================================
        configuration.setAllowCredentials(false);
        
        // ============================================================
        // PREFLIGHT CACHE - 1 hour
        // ============================================================
        configuration.setMaxAge(3600L);
        
        // ============================================================
        // REGISTER CORS MAPPINGS
        // ============================================================
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/api/auth/**", configuration);
        source.registerCorsConfiguration("/api/admin/**", configuration);
        source.registerCorsConfiguration("/api/alerts/**", configuration);
        source.registerCorsConfiguration("/api/reports/**", configuration);
        
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        
        System.out.println("✅ CORS Configuration initialized with ALLOW_ALL");
        
        return bean;
    }
}
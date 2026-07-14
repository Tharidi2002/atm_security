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
        
        // 💡 මෙතනට ඔයාගේ java සහ jawa කියන domains දෙකම සහ පොදු "*" එකත් ඇතුළත් කළා ආරක්ෂාවට
        configuration.setAllowedOriginPatterns(List.of(
                "https://alarm-security-system-java.vercel.app",
                "https://alarm-security-system-jawa.vercel.app",
                "http://localhost:5173",
                "http://localhost:3000",
                "*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // 👈 හැමදේටම කලින් මේක රන් වෙන්න ඕනේ
        return bean;
    }
}
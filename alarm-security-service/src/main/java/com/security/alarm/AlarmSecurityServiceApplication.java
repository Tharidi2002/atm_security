package com.security.alarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.security.alarm"})
public class AlarmSecurityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlarmSecurityServiceApplication.class, args);
    }
}
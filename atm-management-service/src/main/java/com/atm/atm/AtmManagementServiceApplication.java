package com.atm.atm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AtmManagementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AtmManagementServiceApplication.class, args);
    }
}

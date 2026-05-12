package com.atm.atm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class AtmManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(AtmManagementApplication.class, args);
    }
}

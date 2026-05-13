package com.atm.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class AlertProcessorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertProcessorServiceApplication.class, args);
    }
}

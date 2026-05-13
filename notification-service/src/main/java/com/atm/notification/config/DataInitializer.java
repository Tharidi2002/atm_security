package com.atm.notification.config;

import com.atm.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final NotificationService notificationService;
    
    @Override
    public void run(String... args) throws Exception {
        // Create sample notifications if database is empty
        try {
            notificationService.createSampleNotifications();
            log.info("Data initialization completed");
        } catch (Exception e) {
            log.error("Error during data initialization", e);
        }
    }
}

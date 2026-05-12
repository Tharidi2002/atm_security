package com.atm.alert.client;

import com.atm.alert.entity.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.notification-service}")
    private String notificationServiceUrl;
    
    public void sendAlertNotification(Alert alert) {
        try {
            webClient.post()
                    .uri(notificationServiceUrl + "/api/notifications/alert")
                    .bodyValue(alert)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.info("Alert notification sent successfully for alert ID: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Failed to send alert notification for alert ID: {}", alert.getId(), e);
            // In production, you might want to implement retry logic or dead letter queue
        }
    }
    
    public void sendEscalationNotification(Alert alert) {
        try {
            webClient.post()
                    .uri(notificationServiceUrl + "/api/notifications/escalation")
                    .bodyValue(alert)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.info("Escalation notification sent successfully for alert ID: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Failed to send escalation notification for alert ID: {}", alert.getId(), e);
        }
    }
}

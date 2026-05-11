package com.atm.alert.service;

import com.atm.alert.entity.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // TODO: Listen to Kafka notification topic (temporarily disabled)
    // @KafkaListener(topics = "${atm.kafka.topic.notifications}", groupId = "websocket-group")
    public void handleNotification(String message) {
        try {
            logger.info("Received notification from Kafka: {}", message);
            
            // Parse the notification message
            Map<String, Object> notification = parseNotification(message);
            
            // Send to WebSocket topic
            String topic = "/topic/alerts";
            
            // Send to specific users if needed
            String bankName = (String) notification.get("bankName");
            if (bankName != null) {
                // Send to bank-specific topic
                messagingTemplate.convertAndSend("/topic/alerts/" + bankName.toLowerCase(), notification);
            }
            
            // Send to general alerts topic
            messagingTemplate.convertAndSend(topic, notification);
            
            // Send to critical alerts topic for high priority alerts
            String priority = (String) notification.get("priority");
            if ("HIGH".equals(priority)) {
                messagingTemplate.convertAndSend("/topic/critical-alerts", notification);
            }
            
            logger.info("Notification sent via WebSocket: {}", notification.get("alertId"));
            
        } catch (Exception e) {
            logger.error("Error processing notification from Kafka: {}", e.getMessage(), e);
        }
    }

    public void sendAlertToUser(String username, Alert alert) {
        try {
            Map<String, Object> alertData = new HashMap<>();
            alertData.put("id", alert.getId());
            alertData.put("message", alert.getMessage());
            alertData.put("severity", alert.getSeverity().toString());
            alertData.put("category", alert.getCategory().toString());
            alertData.put("bankName", alert.getBankName());
            alertData.put("location", alert.getLocation());
            alertData.put("isAcknowledged", alert.getIsAcknowledged());
            alertData.put("createdAt", alert.getCreatedAt());
            alertData.put("type", "USER_ALERT");

            // Send to specific user
            messagingTemplate.convertAndSendToUser(username, "/queue/alerts", alertData);
            
            logger.info("Alert sent to user {}: {}", username, alert.getId());
            
        } catch (Exception e) {
            logger.error("Error sending alert to user {}: {}", username, e.getMessage(), e);
        }
    }

    public void sendSystemNotification(String message, String type) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("message", message);
            notification.put("type", type);
            notification.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/system-notifications", notification);
            
            logger.info("System notification sent: {}", type);
            
        } catch (Exception e) {
            logger.error("Error sending system notification: {}", e.getMessage(), e);
        }
    }

    public void broadcastAlertUpdate(Alert alert, String updateType) {
        try {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("id", alert.getId());
            updateData.put("message", alert.getMessage());
            updateData.put("severity", alert.getSeverity().toString());
            updateData.put("category", alert.getCategory().toString());
            updateData.put("bankName", alert.getBankName());
            updateData.put("location", alert.getLocation());
            updateData.put("isAcknowledged", alert.getIsAcknowledged());
            updateData.put("acknowledgedBy", alert.getAcknowledgedBy());
            updateData.put("acknowledgedAt", alert.getAcknowledgedAt());
            updateData.put("updatedAt", alert.getUpdatedAt());
            updateData.put("type", updateType);

            // Send to general alerts topic
            messagingTemplate.convertAndSend("/topic/alert-updates", updateData);
            
            // Send to bank-specific topic
            if (alert.getBankName() != null) {
                messagingTemplate.convertAndSend("/topic/alerts/" + alert.getBankName().toLowerCase(), updateData);
            }
            
            logger.info("Alert update broadcasted: {} - {}", alert.getId(), updateType);
            
        } catch (Exception e) {
            logger.error("Error broadcasting alert update: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> parseNotification(String message) {
        // Simple JSON parsing (in production, use proper JSON library)
        Map<String, Object> notification = new HashMap<>();
        
        try {
            // This is a simplified parser - in production use Jackson or Gson
            String[] parts = message.split(",");
            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replace("\"", "").replace("{", "").replace("}", "");
                    String value = keyValue[1].trim().replace("\"", "").replace("{", "").replace("}", "");
                    notification.put(key, value);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing notification message: {}", e.getMessage());
            // Return basic notification structure
            notification.put("message", message);
            notification.put("type", "NOTIFICATION");
        }
        
        return notification;
    }
}

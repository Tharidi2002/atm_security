package com.atm.alert.service;

import com.atm.alert.entity.Alert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Temporarily disabled for testing without Kafka
// @Service
public class KafkaAlertProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaAlertProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    @Qualifier("alertsTopic")
    private String alertsTopic;

    @Autowired
    @Qualifier("notificationsTopic")
    private String notificationsTopic;

    @Autowired
    @Qualifier("auditTopic")
    private String auditTopic;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishAlert(Alert alert) {
        try {
            Map<String, Object> alertMessage = new HashMap<>();
            alertMessage.put("id", alert.getId());
            alertMessage.put("message", alert.getMessage());
            alertMessage.put("phoneNumber", alert.getPhoneNumber());
            alertMessage.put("severity", alert.getSeverity().toString());
            alertMessage.put("category", alert.getCategory().toString());
            alertMessage.put("bankName", alert.getBankName());
            alertMessage.put("location", alert.getLocation());
            alertMessage.put("isAcknowledged", alert.getIsAcknowledged());
            alertMessage.put("createdAt", alert.getCreatedAt());
            alertMessage.put("type", "ALERT_CREATED");

            String message = objectMapper.writeValueAsString(alertMessage);
            kafkaTemplate.send(alertsTopic, alert.getPhoneNumber(), message);
            
            logger.info("Alert published to Kafka: {}", alert.getId());
            
            // Also send to notification topic for real-time updates
            publishNotification(alert);
            
        } catch (Exception e) {
            logger.error("Error publishing alert to Kafka: {}", e.getMessage(), e);
        }
    }

    public void publishAlertUpdate(Alert alert, String updateType) {
        try {
            Map<String, Object> alertMessage = new HashMap<>();
            alertMessage.put("id", alert.getId());
            alertMessage.put("message", alert.getMessage());
            alertMessage.put("phoneNumber", alert.getPhoneNumber());
            alertMessage.put("severity", alert.getSeverity().toString());
            alertMessage.put("category", alert.getCategory().toString());
            alertMessage.put("bankName", alert.getBankName());
            alertMessage.put("location", alert.getLocation());
            alertMessage.put("isAcknowledged", alert.getIsAcknowledged());
            alertMessage.put("acknowledgedBy", alert.getAcknowledgedBy());
            alertMessage.put("acknowledgedAt", alert.getAcknowledgedAt());
            alertMessage.put("updatedAt", alert.getUpdatedAt());
            alertMessage.put("type", updateType);

            String message = objectMapper.writeValueAsString(alertMessage);
            kafkaTemplate.send(alertsTopic, alert.getPhoneNumber(), message);
            
            logger.info("Alert update published to Kafka: {} - {}", alert.getId(), updateType);
            
            // Also send to notification topic for real-time updates
            publishNotification(alert);
            
        } catch (Exception e) {
            logger.error("Error publishing alert update to Kafka: {}", e.getMessage(), e);
        }
    }

    public void publishNotification(Alert alert) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("alertId", alert.getId());
            notification.put("message", alert.getMessage());
            notification.put("severity", alert.getSeverity().toString());
            notification.put("category", alert.getCategory().toString());
            notification.put("bankName", alert.getBankName());
            notification.put("location", alert.getLocation());
            notification.put("isAcknowledged", alert.getIsAcknowledged());
            notification.put("createdAt", alert.getCreatedAt());
            notification.put("type", "NOTIFICATION");
            
            // Add notification priority based on severity
            if (alert.getSeverity().toString().equals("CRITICAL")) {
                notification.put("priority", "HIGH");
            } else if (alert.getSeverity().toString().equals("WARNING")) {
                notification.put("priority", "MEDIUM");
            } else {
                notification.put("priority", "LOW");
            }

            String message = objectMapper.writeValueAsString(notification);
            kafkaTemplate.send(notificationsTopic, "alert-" + alert.getId(), message);
            
            logger.info("Notification published to Kafka: {}", alert.getId());
            
        } catch (Exception e) {
            logger.error("Error publishing notification to Kafka: {}", e.getMessage(), e);
        }
    }

    public void publishAuditLog(String action, String entity, String entityId, String username, Map<String, Object> details) {
        try {
            Map<String, Object> auditLog = new HashMap<>();
            auditLog.put("action", action);
            auditLog.put("entity", entity);
            auditLog.put("entityId", entityId);
            auditLog.put("username", username);
            auditLog.put("timestamp", LocalDateTime.now());
            auditLog.put("details", details);

            String message = objectMapper.writeValueAsString(auditLog);
            kafkaTemplate.send(auditTopic, entity + "-" + entityId, message);
            
            logger.info("Audit log published to Kafka: {} - {}", action, entityId);
            
        } catch (Exception e) {
            logger.error("Error publishing audit log to Kafka: {}", e.getMessage(), e);
        }
    }
}

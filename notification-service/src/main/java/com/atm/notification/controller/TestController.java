package com.atm.notification.controller;

import com.atm.notification.dto.NotificationRequest;
import com.atm.notification.entity.Notification;
import com.atm.notification.enums.NotificationType;
import com.atm.notification.enums.SentVia;
import com.atm.notification.service.NotificationService;
import com.atm.notification.service.WebSocketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Endpoints", description = "Testing endpoints for development")
public class TestController {
    
    private final NotificationService notificationService;
    private final WebSocketService webSocketService;
    
    @PostMapping("/send-sample")
    public ResponseEntity<Map<String, Object>> sendSampleNotification() {
        log.info("Creating sample notification for user ID 1");
        
        NotificationRequest sampleRequest = NotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.ALERT)
                .title("Sample Test Alert")
                .message("This is a test notification from the notification service")
                .sentVia(SentVia.WEBSOCKET)
                .build();
        
        Notification notification = notificationService.sendNotification(sampleRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sample notification sent successfully");
        response.put("notification", notification);
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/websocket-info")
    public ResponseEntity<Map<String, Object>> getWebSocketInfo() {
        Map<String, Object> info = new HashMap<>();
        
        List<Long> onlineUsers = webSocketService.getOnlineUsers();
        int onlineCount = webSocketService.getOnlineUserCount();
        
        info.put("onlineUsers", onlineUsers);
        info.put("onlineCount", onlineCount);
        info.put("websocketEndpoint", "/ws-alerts");
        info.put("userQueue", "/user/{userId}/queue/notifications");
        info.put("broadcastTopic", "/topic/alerts");
        info.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(info);
    }
    
    @PostMapping("/create-sample-data")
    public ResponseEntity<Map<String, Object>> createSampleData() {
        try {
            notificationService.createSampleNotifications();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Sample data created successfully");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create sample data", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failed to create sample data: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "notification-service");
        health.put("timestamp", java.time.LocalDateTime.now());
        health.put("database", "Connected"); // In real implementation, check actual DB connection
        health.put("websocket", "Active");
        
        return ResponseEntity.ok(health);
    }
}

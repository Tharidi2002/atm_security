package com.atm.notification.controller;

import com.atm.notification.dto.NotificationRequest;
import com.atm.notification.entity.Notification;
import com.atm.notification.service.NotificationService;
import com.atm.notification.service.WebSocketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "APIs for managing notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final WebSocketService webSocketService;
    
    @PostMapping("/send")
    @Operation(summary = "Send notification to a specific user")
    public ResponseEntity<Notification> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        
        log.info("Sending notification to user: {}", request.getUserId());
        Notification notification = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications for a specific user")
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread notification count for a user")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        long count = notificationService.getUnreadCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Notification> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable Long id,
            @Parameter(description = "User ID for authorization") @RequestParam Long userId) {
        
        Notification notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(notification);
    }
    
    @PostMapping("/broadcast")
    @Operation(summary = "Broadcast notification to all online users")
    public ResponseEntity<Map<String, Object>> broadcastNotification(
            @Valid @RequestBody NotificationRequest request) {
        
        log.info("Broadcasting notification to all online users");
        notificationService.sendToAllOnlineUsers(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notification broadcasted successfully");
        response.put("onlineUsers", webSocketService.getOnlineUserCount());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/online-users")
    @Operation(summary = "Get information about online users")
    public ResponseEntity<Map<String, Object>> getOnlineUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("onlineCount", webSocketService.getOnlineUserCount());
        response.put("userIds", webSocketService.getOnlineUsers());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "Get unread notifications for a user")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(notifications.getContent());
    }
}

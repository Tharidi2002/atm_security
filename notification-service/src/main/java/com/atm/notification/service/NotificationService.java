package com.atm.notification.service;

import com.atm.notification.dto.NotificationMessage;
import com.atm.notification.dto.NotificationRequest;
import com.atm.notification.entity.Notification;
import com.atm.notification.enums.SentVia;
import com.atm.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final WebSocketService webSocketService;
    private final EmailService emailService;
    
    @Transactional
    public Notification sendNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .alertId(request.getAlertId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .sentVia(request.getSentVia())
                .metadata(request.getMetadata())
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        log.info("Saved notification: {} for user: {}", notification.getId(), notification.getUserId());
        
        // Send via WebSocket if user is online
        if (request.getSentVia() == SentVia.WEBSOCKET || request.getSentVia() == SentVia.BOTH) {
            if (webSocketService.isUserOnline(notification.getUserId())) {
                NotificationMessage message = NotificationMessage.builder()
                        .notificationId(notification.getId())
                        .type(notification.getType())
                        .title(notification.getTitle())
                        .message(notification.getMessage())
                        .timestamp(notification.getCreatedAt())
                        .alertId(notification.getAlertId())
                        .metadata(notification.getMetadata())
                        .build();
                
                webSocketService.sendToUser(notification.getUserId(), message);
                log.info("Sent WebSocket notification to user: {}", notification.getUserId());
            }
        }
        
        // Send via Email if requested
        if (request.getSentVia() == SentVia.EMAIL || request.getSentVia() == SentVia.BOTH) {
            // For now, we'll use a mock email. In real implementation, 
            // you'd fetch user email from user service
            String userEmail = "user" + notification.getUserId() + "@example.com";
            String subject = "ATM Security Alert: " + notification.getTitle();
            String body = notification.getMessage();
            
            emailService.sendSimpleEmail(userEmail, subject, body);
            log.info("Sent email notification to user: {}", notification.getUserId());
        }
        
        return notification;
    }
    
    @Transactional
    public void sendToAllOnlineUsers(NotificationRequest request) {
        List<Long> onlineUsers = webSocketService.getOnlineUsers();
        
        for (Long userId : onlineUsers) {
            NotificationRequest userRequest = NotificationRequest.builder()
                    .userId(userId)
                    .alertId(request.getAlertId())
                    .type(request.getType())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .sentVia(SentVia.WEBSOCKET) // Always WebSocket for broadcast
                    .metadata(request.getMetadata())
                    .build();
            
            sendNotification(userRequest);
        }
        
        log.info("Broadcast notification sent to {} online users", onlineUsers.size());
    }
    
    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        // Verify user owns this notification
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("User not authorized to mark this notification as read");
        }
        
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }
    
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    public Page<Notification> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable);
    }
    
    public List<Notification> getRecentUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    @Transactional
    public void createSampleNotifications() {
        long count = notificationRepository.count();
        if (count == 0) {
            log.info("Creating sample notifications for user ID 1");
            
            NotificationRequest sample1 = NotificationRequest.builder()
                    .userId(1L)
                    .type(com.atm.notification.enums.NotificationType.ALERT)
                    .title("Critical Alert")
                    .message("Fire detected at ATM-001")
                    .sentVia(SentVia.BOTH)
                    .build();
            
            NotificationRequest sample2 = NotificationRequest.builder()
                    .userId(1L)
                    .type(com.atm.notification.enums.NotificationType.ASSIGNMENT)
                    .title("New Assignment")
                    .message("You have been assigned to investigate alert #123")
                    .sentVia(SentVia.WEBSOCKET)
                    .build();
            
            NotificationRequest sample3 = NotificationRequest.builder()
                    .userId(1L)
                    .type(com.atm.notification.enums.NotificationType.ESCALATION)
                    .title("Alert Escalated")
                    .message("Alert #456 has been escalated to your team")
                    .sentVia(SentVia.BOTH)
                    .build();
            
            sendNotification(sample1);
            sendNotification(sample2);
            sendNotification(sample3);
            
            log.info("Created 3 sample notifications");
        }
    }
}

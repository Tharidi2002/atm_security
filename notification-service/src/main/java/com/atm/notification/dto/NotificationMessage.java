package com.atm.notification.dto;

import com.atm.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    
    private Long notificationId;
    private NotificationType type;
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private Long alertId;
    private String metadata;
}

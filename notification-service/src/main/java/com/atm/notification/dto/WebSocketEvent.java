package com.atm.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent {
    
    public enum EventType {
        NOTIFICATION,
        USER_ONLINE,
        USER_OFFLINE,
        ALERT_UPDATE
    }
    
    private EventType eventType;
    private Object payload;
    private Long userId;
    private String timestamp;
}

package com.atm.notification.event;

import com.atm.notification.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    
    private final WebSocketService webSocketService;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Get userId from session attributes (set by interceptor)
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        
        if (userId != null) {
            webSocketService.registerSession(sessionId, userId);
            log.info("WebSocket connected: SessionId={}, UserId={}", sessionId, userId);
        } else {
            log.warn("WebSocket connected without userId: SessionId={}", sessionId);
        }
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        webSocketService.unregisterSession(sessionId);
        log.info("WebSocket disconnected: SessionId={}", sessionId);
    }
}

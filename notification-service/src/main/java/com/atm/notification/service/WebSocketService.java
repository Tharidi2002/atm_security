package com.atm.notification.service;

import com.atm.notification.dto.WebSocketEvent;
import com.atm.notification.entity.WebSocketSession;
import com.atm.notification.repository.WebSocketSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    
    private final WebSocketSessionRepository webSocketSessionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    public void registerSession(String sessionId, Long userId) {
        // Remove existing sessions for this user (limit to max sessions per user)
        List<WebSocketSession> existingSessions = webSocketSessionRepository.findByUserId(userId);
        if (existingSessions.size() >= 3) {
            // Remove oldest session
            WebSocketSession oldestSession = existingSessions.get(0);
            webSocketSessionRepository.delete(oldestSession);
            log.info("Removed oldest session {} for user {}", oldestSession.getSessionId(), userId);
        }
        
        WebSocketSession webSocketSession = WebSocketSession.builder()
                .userId(userId)
                .sessionId(sessionId)
                .connectedAt(LocalDateTime.now())
                .build();
        
        webSocketSessionRepository.save(webSocketSession);
        log.info("Registered WebSocket session {} for user {}", sessionId, userId);
        
        // Notify other users that this user is online
        WebSocketEvent event = WebSocketEvent.builder()
                .eventType(WebSocketEvent.EventType.USER_ONLINE)
                .payload(userId)
                .userId(userId)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
        
        sendToAll(event);
    }
    
    public void unregisterSession(String sessionId) {
        WebSocketSession session = webSocketSessionRepository.findBySessionId(sessionId).orElse(null);
        if (session != null) {
            Long userId = session.getUserId();
            webSocketSessionRepository.delete(session);
            log.info("Unregistered WebSocket session {} for user {}", sessionId, userId);
            
            // Check if user has any remaining sessions
            List<WebSocketSession> remainingSessions = webSocketSessionRepository.findByUserId(userId);
            if (remainingSessions.isEmpty()) {
                // Notify other users that this user is offline
                WebSocketEvent event = WebSocketEvent.builder()
                        .eventType(WebSocketEvent.EventType.USER_OFFLINE)
                        .payload(userId)
                        .userId(userId)
                        .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .build();
                
                sendToAll(event);
            }
        }
    }
    
    public void sendToUser(Long userId, Object message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(), 
                    "/queue/notifications", 
                    message
            );
            log.debug("Sent message to user {}: {}", userId, message);
        } catch (Exception e) {
            log.error("Failed to send message to user {}: {}", userId, message, e);
        }
    }
    
    public void sendToAll(Object message) {
        try {
            messagingTemplate.convertAndSend("/topic/alerts", message);
            log.debug("Sent broadcast message: {}", message);
        } catch (Exception e) {
            log.error("Failed to send broadcast message: {}", message, e);
        }
    }
    
    public List<Long> getOnlineUsers() {
        return webSocketSessionRepository.findDistinctUserIds();
    }
    
    public int getOnlineUserCount() {
        return (int) webSocketSessionRepository.findDistinctUserIds().size();
    }
    
    public boolean isUserOnline(Long userId) {
        List<WebSocketSession> sessions = webSocketSessionRepository.findByUserId(userId);
        return !sessions.isEmpty();
    }
}

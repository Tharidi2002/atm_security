package com.atmsecurity.notification.handler;

import com.atmsecurity.common.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertWebSocketHandler extends TextWebSocketHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        String token = null;

        if (query != null && query.contains("token=")) {
            token = query.split("token=")[1].split("&")[0];
        }

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String role = jwtTokenProvider.getRole(token);
            Long bankId = jwtTokenProvider.getBankId(token);
            Long userId = jwtTokenProvider.getUserId(token);

            session.getAttributes().put("userId", userId);
            session.getAttributes().put("role", role);
            session.getAttributes().put("bankId", bankId);
            
            sessions.add(session);
            log.info("WebSocket connection established. User ID: {}, Role: {}, Bank ID: {}", userId, role, bankId);
        } else {
            log.warn("Invalid token in WebSocket handshake. Closing connection.");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket connection closed for session: {}", session.getId());
    }

    public void broadcastAlert(Map<String, Object> alertMap) {
        String alertJson;
        try {
            alertJson = objectMapper.writeValueAsString(alertMap);
        } catch (Exception e) {
            log.error("Failed to serialize alert map: {}", e.getMessage());
            return;
        }

        Long alertBankId = null;
        if (alertMap.get("bankId") != null) {
            alertBankId = Long.valueOf(alertMap.get("bankId").toString());
        }

        TextMessage message = new TextMessage(alertJson);
        int dispatchedCount = 0;

        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                sessions.remove(session);
                continue;
            }

            try {
                String role = (String) session.getAttributes().get("role");
                Long sessionBankId = (Long) session.getAttributes().get("bankId");

                // RBAC + Bank Isolation
                if ("ADMIN".equals(role)) {
                    // System admins get all alerts
                    session.sendMessage(message);
                    dispatchedCount++;
                } else if (alertBankId != null && alertBankId.equals(sessionBankId)) {
                    // Bank scoped users only get their bank's alerts
                    session.sendMessage(message);
                    dispatchedCount++;
                }
            } catch (IOException e) {
                log.error("Failed to send message to session: {}", session.getId(), e);
            }
        }
        log.info("Broadcasted alert {} to {} WebSocket clients", alertMap.get("id"), dispatchedCount);
    }
}

package com.atmsecurity.notification.controller;

import com.atmsecurity.notification.entity.NotificationLog;
import com.atmsecurity.notification.handler.AlertWebSocketHandler;
import com.atmsecurity.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final AlertWebSocketHandler webSocketHandler;
    private final NotificationLogRepository logRepository;

    @PostMapping("/dispatch")
    public ResponseEntity<Void> dispatchAlertNotification(@RequestBody Map<String, Object> alertMap) {
        log.info("Received dispatch request for alert: {}", alertMap);

        // Broadcast to WebSocket subscribers
        webSocketHandler.broadcastAlert(alertMap);

        // Save notification log
        try {
            Long alertId = alertMap.get("id") != null ? Long.valueOf(alertMap.get("id").toString()) : null;
            Long bankId = alertMap.get("bankId") != null ? Long.valueOf(alertMap.get("bankId").toString()) : null;
            String alertType = alertMap.get("alertType") != null ? alertMap.get("alertType").toString() : "GENERAL";
            String title = alertMap.get("title") != null ? alertMap.get("title").toString() : "Alert";
            String message = alertMap.get("message") != null ? alertMap.get("message").toString() : "";
            String severity = alertMap.get("severity") != null ? alertMap.get("severity").toString() : "INFO";

            NotificationLog nlog = NotificationLog.builder()
                    .alertId(alertId)
                    .bankId(bankId)
                    .channel("WEBSOCKET")
                    .title(title)
                    .body(message)
                    .severity(severity)
                    .delivered(true)
                    .deliveredAt(LocalDateTime.now())
                    .build();
            logRepository.save(nlog);
        } catch (Exception e) {
            log.error("Failed to write notification log: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}

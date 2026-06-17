package com.atmsecurity.alert.service;

import com.atmsecurity.common.crypto.AesEncryptionService;
import com.atmsecurity.alert.dto.AiAnalysisRequest;
import com.atmsecurity.alert.dto.AiAnalysisResponse;
import com.atmsecurity.alert.dto.SmsWebhookPayload;
import com.atmsecurity.alert.entity.*;
import com.atmsecurity.alert.repository.AlertAcknowledgementRepository;
import com.atmsecurity.alert.repository.SecurityAlertRepository;
import com.atmsecurity.alert.repository.StationRefRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final SecurityAlertRepository alertRepository;
    private final StationRefRepository stationRefRepository;
    private final AlertAcknowledgementRepository acknowledgementRepository;
    private final AesEncryptionService encryptionService;
    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public List<SecurityAlert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public List<SecurityAlert> getAlertsByBank(Long bankId) {
        return alertRepository.findByBankId(bankId);
    }

    public Optional<SecurityAlert> getAlertById(Long id) {
        return alertRepository.findById(id);
    }

    @Transactional
    public SecurityAlert processSmsWebhook(SmsWebhookPayload payload) {
        String sender = payload.getSender();
        String message = payload.getMessage();
        String messageId = payload.getMessageId();

        String phoneHash = encryptionService.hashSha256(sender);
        
        // Find station ref
        StationRef station = resolveStationRef(phoneHash);
        Long bankId = (station != null) ? station.getBankId() : 1L; // Fallback to Bank ID 1

        // Parse message for alert type, severity, and title
        ParsedAlert parsed = parseMessage(message);

        // Build alert entity
        SecurityAlert alert = SecurityAlert.builder()
                .externalMessageId(messageId)
                .stationId(station != null ? station.getId() : null)
                .bankId(bankId)
                .alertType(parsed.alertType)
                .severity(parsed.severity)
                .title(parsed.title)
                .message(message)
                .rawSms("Sender: " + sender + " | Msg: " + message)
                .senderPhoneHash(phoneHash)
                .zone(parsed.zone)
                .acknowledged(false)
                .receivedAt(LocalDateTime.now())
                .source("SMS")
                .build();

        // Call AI Anomaly Service
        try {
            AiAnalysisRequest aiRequest = AiAnalysisRequest.builder()
                    .stationCode(station != null ? station.getStationCode() : "UNKNOWN")
                    .alertType(parsed.alertType)
                    .message(message)
                    .timestamp(LocalDateTime.now().toString())
                    .build();

            AiAnalysisResponse aiResponse = restTemplate.postForObject(
                    aiServiceUrl + "/api/ai/analyze",
                    aiRequest,
                    AiAnalysisResponse.class
            );

            if (aiResponse != null) {
                alert.setAnomalyScore(aiResponse.getAnomalyScore());
                alert.setAnomaly(aiResponse.isAnomaly());
            }
        } catch (Exception e) {
            log.warn("AI service call failed: {}. Executing local fallback anomaly analysis.", e.getMessage());
            
            double score = 0.0;
            boolean isAnom = false;

            if (station == null) {
                score = 0.95;
                isAnom = true;
            }

            int hour = LocalDateTime.now().getHour();
            boolean isNight = hour < 6 || hour >= 22;

            if (isNight) {
                if ("DOOR_OPEN".equals(parsed.alertType) || "PHYSICAL_TAMPERING".equals(parsed.alertType)) {
                    score = Math.max(score, 0.90);
                    isAnom = true;
                } else if ("POWER_FAILURE".equals(parsed.alertType)) {
                    score = Math.max(score, 0.70);
                    isAnom = true;
                }
            } else {
                if ("PHYSICAL_TAMPERING".equals(parsed.alertType)) {
                    score = Math.max(score, 0.80);
                    isAnom = true;
                } else if ("DOOR_OPEN".equals(parsed.alertType)) {
                    score = Math.max(score, 0.30);
                } else if ("FIRE_ALARM".equals(parsed.alertType)) {
                    score = Math.max(score, 0.85);
                    isAnom = true;
                }
            }

            // Keyword threat matching
            String lowerMsg = message.toLowerCase();
            String[] keywords = {"unauthorized", "failed", "breach", "smoke", "tamper", "alarm"};
            int kwCount = 0;
            for (String kw : keywords) {
                if (lowerMsg.contains(kw)) {
                    kwCount++;
                }
            }

            if (kwCount >= 2) {
                score = Math.max(score, 0.75);
                isAnom = true;
            }

            if (score >= 0.70) {
                isAnom = true;
            }

            alert.setAnomalyScore(BigDecimal.valueOf(score));
            alert.setAnomaly(isAnom);
        }

        SecurityAlert saved = alertRepository.save(alert);

        // Dispatch WebSocket notification via Notification Service
        try {
            restTemplate.postForObject("http://notification-service/api/notifications/dispatch", saved, Void.class);
        } catch (Exception e) {
            log.error("Failed to dispatch real-time notification to notification-service: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional
    public SecurityAlert acknowledgeAlert(Long id, Long userId, String username, String notes) {
        SecurityAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with ID: " + id));

        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(userId);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setResponseNotes(notes);

        SecurityAlert saved = alertRepository.save(alert);

        AlertAcknowledgement ack = AlertAcknowledgement.builder()
                .alertId(id)
                .userId(userId)
                .username(username)
                .notes(notes)
                .build();
        acknowledgementRepository.save(ack);

        return saved;
    }

    private StationRef resolveStationRef(String phoneHash) {
        Optional<StationRef> localRef = stationRefRepository.findByPhoneNumberHash(phoneHash);
        if (localRef.isPresent()) {
            return localRef.get();
        }

        // Try syncing from station-service
        try {
            StationRef remoteRef = restTemplate.getForObject(
                    "http://station-service/api/stations/phone-hash/" + phoneHash,
                    StationRef.class
            );
            if (remoteRef != null) {
                // Save locally
                return stationRefRepository.save(remoteRef);
            }
        } catch (Exception e) {
            log.warn("Failed to resolve station from station-service: {}", e.getMessage());
        }
        return null;
    }

    private static class ParsedAlert {
        String alertType;
        Severity severity;
        String title;
        String zone;
    }

    private ParsedAlert parseMessage(String msg) {
        ParsedAlert parsed = new ParsedAlert();
        String lower = msg.toLowerCase();

        if (lower.contains("door") || lower.contains("open") || lower.contains("access")) {
            parsed.alertType = "DOOR_OPEN";
            parsed.severity = Severity.CRITICAL;
            parsed.title = "Unauthorized Door Access";
            parsed.zone = "cash counter";
        } else if (lower.contains("fire") || lower.contains("smoke") || lower.contains("temp")) {
            parsed.alertType = "FIRE_ALARM";
            parsed.severity = Severity.CRITICAL;
            parsed.title = "Fire Alarm Triggered";
            parsed.zone = "general";
        } else if (lower.contains("power") || lower.contains("ups") || lower.contains("battery")) {
            parsed.alertType = "POWER_FAILURE";
            parsed.severity = Severity.WARNING;
            parsed.title = "Power Supply Interrupted";
            parsed.zone = "pawning area";
        } else if (lower.contains("tamper") || lower.contains("vibration") || lower.contains("shake")) {
            parsed.alertType = "PHYSICAL_TAMPERING";
            parsed.severity = Severity.CRITICAL;
            parsed.title = "Physical Tampering Detected";
            parsed.zone = "cash counter";
        } else {
            parsed.alertType = "GENERAL";
            parsed.severity = Severity.INFO;
            parsed.title = "General Security Alert";
            parsed.zone = "general";
        }
        return parsed;
    }
}

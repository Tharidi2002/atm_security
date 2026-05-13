package com.atm.alert.service;

import com.atm.alert.client.AtmServiceClient;
import com.atm.alert.client.NotificationServiceClient;
import com.atm.alert.dto.SmsAlertDto;
import com.atm.alert.entity.Alert;
import com.atm.alert.enums.AlertCategory;
import com.atm.alert.enums.AlertSeverity;
import com.atm.alert.enums.AlertStatus;
import com.atm.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertProcessingService {
    
    private final AlertRepository alertRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationServiceClient notificationServiceClient;
    private final AtmServiceClient atmServiceClient;
    
    private static final Pattern ATM_ID_PATTERN = Pattern.compile("(?i)ATM[-\\s]?([0-9]+)");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?[0-9]{10,15}");
    
    // Critical keywords for severity classification
    private static final List<String> CRITICAL_KEYWORDS = List.of(
        "fire", "tamper", "unauthorized", "attack", "breach", "intrusion", "theft", "robbery"
    );
    
    private static final List<String> WARNING_KEYWORDS = List.of(
        "door open", "power fail", "low battery", "connection lost", "offline", "error"
    );
    
    @Transactional
    public Alert processSmsAlert(SmsAlertDto smsAlert) {
        log.info("Processing SMS alert from: {}", smsAlert.getFromNumber());
        
        // Check for deduplication
        String deduplicationKey = generateDeduplicationKey(smsAlert);
        if (isDuplicateAlert(deduplicationKey)) {
            log.info("Duplicate alert detected, skipping: {}", deduplicationKey);
            return null;
        }
        
        // Parse ATM ID from message
        Long atmId = extractAtmId(smsAlert.getMessage(), smsAlert.getFromNumber());
        if (atmId == null) {
            log.warn("Could not extract ATM ID from SMS: {}", smsAlert.getMessage());
            return null;
        }
        
        // Verify ATM exists
        if (!atmServiceClient.isAtmExists(atmId)) {
            log.warn("ATM not found for ID: {}", atmId);
            return null;
        }
        
        // Classify alert severity and category
        AlertSeverity severity = classifySeverity(smsAlert.getMessage());
        AlertCategory category = classifyCategory(smsAlert.getMessage());
        
        // Create alert
        Alert alert = Alert.builder()
                .atmId(atmId)
                .severity(severity)
                .category(category)
                .message(smsAlert.getMessage())
                .rawSms(smsAlert.getRawSms())
                .status(AlertStatus.NEW)
                .source("SMS")
                .externalId(smsAlert.getSmsId())
                .metadata(buildMetadata(smsAlert))
                .createdAt(LocalDateTime.now())
                .build();
        
        Alert savedAlert = alertRepository.save(alert);
        
        // Mark as processed for deduplication
        markAlertProcessed(deduplicationKey);
        
        // Send notification
        sendNotification(savedAlert);
        
        log.info("Alert processed successfully: ID={}, ATM={}, Severity={}", 
                savedAlert.getId(), atmId, severity);
        
        return savedAlert;
    }
    
    private String generateDeduplicationKey(SmsAlertDto smsAlert) {
        Long atmId = extractAtmId(smsAlert.getMessage(), smsAlert.getFromNumber());
        AlertCategory category = classifyCategory(smsAlert.getMessage());
        
        if (atmId != null && category != null) {
            return String.format("alert:%d:%s:%d", atmId, category.name(), 
                    System.currentTimeMillis() / (5 * 60 * 1000)); // 5-minute window
        }
        
        return String.format("sms:%s:%d", smsAlert.getFromNumber(), 
                System.currentTimeMillis() / (5 * 60 * 1000));
    }
    
    private boolean isDuplicateAlert(String deduplicationKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(deduplicationKey));
    }
    
    private void markAlertProcessed(String deduplicationKey) {
        redisTemplate.opsForValue().set(deduplicationKey, "processed", 5, TimeUnit.MINUTES);
    }
    
    private Long extractAtmId(String message, String fromNumber) {
        // Try to extract ATM ID from message first
        Matcher atmMatcher = ATM_ID_PATTERN.matcher(message);
        if (atmMatcher.find()) {
            try {
                return Long.parseLong(atmMatcher.group(1));
            } catch (NumberFormatException e) {
                log.debug("Could not parse ATM ID from message: {}", message);
            }
        }
        
        // If not found in message, try to find ATM by phone number
        return atmServiceClient.findAtmIdByPhoneNumber(fromNumber);
    }
    
    private AlertSeverity classifySeverity(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Check for critical keywords
        for (String keyword : CRITICAL_KEYWORDS) {
            if (lowerMessage.contains(keyword)) {
                return AlertSeverity.CRITICAL;
            }
        }
        
        // Check for warning keywords
        for (String keyword : WARNING_KEYWORDS) {
            if (lowerMessage.contains(keyword)) {
                return AlertSeverity.WARNING;
            }
        }
        
        return AlertSeverity.INFO;
    }
    
    private AlertCategory classifyCategory(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("unauthorized") || lowerMessage.contains("access")) {
            return AlertCategory.UNAUTHORIZED_ACCESS;
        } else if (lowerMessage.contains("fire")) {
            return AlertCategory.FIRE;
        } else if (lowerMessage.contains("tamper")) {
            return AlertCategory.TAMPERING;
        } else if (lowerMessage.contains("door") && lowerMessage.contains("open")) {
            return AlertCategory.DOOR_OPEN;
        } else if (lowerMessage.contains("power") || lowerMessage.contains("mains")) {
            return AlertCategory.POWER_FAILURE;
        } else if (lowerMessage.contains("arm") || lowerMessage.contains("disarm")) {
            return AlertCategory.ARMING_DISARMING;
        } else if (lowerMessage.contains("network") || lowerMessage.contains("connection")) {
            return AlertCategory.NETWORK_ISSUE;
        } else if (lowerMessage.contains("battery")) {
            return AlertCategory.LOW_BATTERY;
        } else if (lowerMessage.contains("maintenance")) {
            return AlertCategory.MAINTENANCE;
        } else if (lowerMessage.contains("error") || lowerMessage.contains("fault")) {
            return AlertCategory.SYSTEM_ERROR;
        }
        
        return AlertCategory.UNKNOWN;
    }
    
    private String buildMetadata(SmsAlertDto smsAlert) {
        return String.format("{\"fromNumber\":\"%s\",\"receivedAt\":\"%s\",\"smsId\":\"%s\"}", 
                smsAlert.getFromNumber(), smsAlert.getReceivedAt(), smsAlert.getSmsId());
    }
    
    private void sendNotification(Alert alert) {
        try {
            notificationServiceClient.sendAlertNotification(alert);
        } catch (Exception e) {
            log.error("Failed to send notification for alert ID: {}", alert.getId(), e);
        }
    }
    
    @Transactional
    public Alert acknowledgeAlert(Long alertId, Long userId) {
        Alert alert = getAlertById(alertId);
        
        if (alert.getStatus() != AlertStatus.NEW) {
            throw new IllegalStateException("Alert cannot be acknowledged in current status: " + alert.getStatus());
        }
        
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(userId);
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert acknowledged: ID={}, User={}", alertId, userId);
        
        return savedAlert;
    }
    
    @Transactional
    public Alert startInvestigation(Long alertId, Long userId) {
        Alert alert = getAlertById(alertId);
        
        if (alert.getStatus() != AlertStatus.ACKNOWLEDGED) {
            throw new IllegalStateException("Alert cannot be investigated in current status: " + alert.getStatus());
        }
        
        alert.setStatus(AlertStatus.INVESTIGATING);
        alert.setInvestigationStartedAt(LocalDateTime.now());
        alert.setInvestigationStartedBy(userId);
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert investigation started: ID={}, User={}", alertId, userId);
        
        return savedAlert;
    }
    
    @Transactional
    public Alert resolveAlert(Long alertId, Long userId, String resolutionNotes) {
        Alert alert = getAlertById(alertId);
        
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(userId);
        alert.setResolutionNotes(resolutionNotes);
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert resolved: ID={}, User={}", alertId, userId);
        
        return savedAlert;
    }
    
    @Transactional
    public Alert markAsFalseAlarm(Long alertId, Long userId, String reason) {
        Alert alert = getAlertById(alertId);
        
        alert.setStatus(AlertStatus.FALSE_ALARM);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(userId);
        alert.setFalseAlarm(Boolean.TRUE);
        alert.setFalseAlarmReason(reason);
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert marked as false alarm: ID={}, User={}", alertId, userId);
        
        return savedAlert;
    }
    
    @Transactional
    public Alert escalateAlert(Long alertId, Long escalateToUserId, String reason) {
        Alert alert = getAlertById(alertId);
        
        alert.setStatus(AlertStatus.ESCALATED);
        alert.setEscalatedAt(LocalDateTime.now());
        alert.setEscalatedTo(escalateToUserId);
        alert.setEscalationReason(reason);
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert escalated: ID={}, ToUser={}, Reason={}", alertId, escalateToUserId, reason);
        
        // Send escalation notification
        sendEscalationNotification(savedAlert);
        
        return savedAlert;
    }
    
    private void sendEscalationNotification(Alert alert) {
        try {
            notificationServiceClient.sendEscalationNotification(alert);
        } catch (Exception e) {
            log.error("Failed to send escalation notification for alert ID: {}", alert.getId(), e);
        }
    }
    
    public Alert getAlertById(Long alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with ID: " + alertId));
    }
    
    public List<Alert> getAlertsByAtmId(Long atmId) {
        return alertRepository.findByAtmId(atmId);
    }
    
    public List<Alert> getActiveAlertsForUser(Long userId) {
        return alertRepository.findActiveAlertsForUser(userId, AlertStatus.RESOLVED);
    }
    
    public List<Alert> getOverdueAlerts() {
        return alertRepository.findOverdueAlerts(LocalDateTime.now(), 
                List.of(AlertStatus.NEW, AlertStatus.ACKNOWLEDGED));
    }
    
    public List<Alert> getAlertsNeedingEscalation() {
        return alertRepository.findAlertsNeedingEscalation(LocalDateTime.now(), 
                List.of(AlertStatus.NEW, AlertStatus.ACKNOWLEDGED, AlertStatus.INVESTIGATING));
    }
    
    @Transactional
    public void processAutomaticEscalations() {
        List<Alert> alertsToEscalate = getAlertsNeedingEscalation();
        
        for (Alert alert : alertsToEscalate) {
            // Escalate to super admin or bank admin based on severity
            Long escalateTo = determineEscalationTarget(alert);
            String reason = String.format("Automatic escalation due to SLA breach. Alert was not resolved within deadline: %s", 
                    alert.getSlaDeadline());
            
            escalateAlert(alert.getId(), escalateTo, reason);
        }
        
        if (!alertsToEscalate.isEmpty()) {
            log.info("Automatically escalated {} alerts", alertsToEscalate.size());
        }
    }
    
    private Long determineEscalationTarget(Alert alert) {
        // In a real implementation, this would determine the appropriate escalation target
        // For now, escalate to user ID 1 (super admin)
        return 1L;
    }
}

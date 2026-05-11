package com.atm.alert.service;

import com.atm.alert.entity.Alert;
import com.atm.alert.entity.AlertSeverity;
import com.atm.alert.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public Optional<Alert> getAlertById(Long id) {
        return alertRepository.findById(id);
    }

    public List<Alert> getAlertsByPhoneNumber(String phoneNumber) {
        return alertRepository.findByPhoneNumber(phoneNumber);
    }

    public List<Alert> getAlertsBySeverity(AlertSeverity severity) {
        return alertRepository.findBySeverity(severity);
    }

    public List<Alert> getAlertsByBankName(String bankName) {
        return alertRepository.findByBankName(bankName);
    }

    public List<Alert> getUnacknowledgedAlerts() {
        return alertRepository.findUnacknowledgedAlertsOrderByCreatedAtDesc();
    }

    public List<Alert> getCriticalUnacknowledgedAlerts() {
        return alertRepository.findCriticalUnacknowledgedAlerts();
    }

    public List<Alert> getAlertsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return alertRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public List<Alert> searchAlerts(String keyword) {
        return alertRepository.searchAlerts(keyword);
    }

    public Alert createAlert(Alert alert) {
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());
        Alert savedAlert = alertRepository.save(alert);
        
        // TODO: Publish to Kafka for real-time processing (temporarily disabled)
        // kafkaAlertProducer.publishAlert(savedAlert);
        
        // Send real-time WebSocket notification
        webSocketNotificationService.broadcastAlertUpdate(savedAlert, "ALERT_CREATED");
        
        // TODO: Log audit trail (temporarily disabled)
        // String username = getCurrentUsername();
        // Map<String, Object> details = new HashMap<>();
        // details.put("alertId", savedAlert.getId());
        // details.put("message", savedAlert.getMessage());
        // details.put("severity", savedAlert.getSeverity());
        // kafkaAlertProducer.publishAuditLog("CREATE_ALERT", "Alert", savedAlert.getId().toString(), username, details);
        
        return savedAlert;
    }

    public Alert createAlertFromSms(String message, String phoneNumber, String bankName, String location) {
        Alert alert = new Alert(message, phoneNumber);
        alert.setBankName(bankName);
        alert.setLocation(location);
        return createAlert(alert);
    }

    public Alert acknowledgeAlert(Long id, String acknowledgedBy, String incidentDetails) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + id));

        alert.setIsAcknowledged(true);
        alert.setAcknowledgedBy(acknowledgedBy);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setIncidentDetails(incidentDetails);
        alert.setUpdatedAt(LocalDateTime.now());

        return alertRepository.save(alert);
    }

    public void deleteAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + id));
        alertRepository.delete(alert);
    }

    public Long getAlertCountBySeverityAndDateRange(AlertSeverity severity, LocalDateTime startDate, LocalDateTime endDate) {
        return alertRepository.countBySeverityAndCreatedAtBetween(severity, startDate, endDate);
    }

    public List<Alert> getBankUnacknowledgedAlerts(String bankName) {
        return alertRepository.findByBankNameAndIsAcknowledgedOrderByCreatedAtDesc(bankName, false);
    }
    
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}

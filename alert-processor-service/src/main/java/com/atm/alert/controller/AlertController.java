package com.atm.alert.controller;

import com.atm.alert.entity.Alert;
import com.atm.alert.enums.AlertSeverity;
import com.atm.alert.enums.AlertStatus;
import com.atm.alert.service.AlertProcessingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {
    
    private final AlertProcessingService alertProcessingService;
    
    @PostMapping("/acknowledge/{id}")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long id, HttpServletRequest request) {
        log.info("Acknowledge alert request for ID: {} from IP: {}", id, getClientIp(request));
        
        // In real implementation, extract user ID from JWT token
        Long userId = 1L; // Placeholder
        
        Alert alert = alertProcessingService.acknowledgeAlert(id, userId);
        return ResponseEntity.ok(alert);
    }
    
    @PostMapping("/investigate/{id}")
    public ResponseEntity<Alert> startInvestigation(@PathVariable Long id, HttpServletRequest request) {
        log.info("Start investigation request for alert ID: {} from IP: {}", id, getClientIp(request));
        
        // In real implementation, extract user ID from JWT token
        Long userId = 1L; // Placeholder
        
        Alert alert = alertProcessingService.startInvestigation(id, userId);
        return ResponseEntity.ok(alert);
    }
    
    @PostMapping("/resolve/{id}")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id, 
                                            @RequestBody Map<String, String> request,
                                            HttpServletRequest httpRequest) {
        log.info("Resolve alert request for ID: {} from IP: {}", id, getClientIp(httpRequest));
        
        String resolutionNotes = request.get("resolutionNotes");
        if (resolutionNotes == null || resolutionNotes.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Resolution notes are required");
            return ResponseEntity.badRequest().build();
        }
        
        // In real implementation, extract user ID from JWT token
        Long userId = 1L; // Placeholder
        
        Alert alert = alertProcessingService.resolveAlert(id, userId, resolutionNotes);
        return ResponseEntity.ok(alert);
    }
    
    @PostMapping("/false-alarm/{id}")
    public ResponseEntity<Alert> markAsFalseAlarm(@PathVariable Long id,
                                                 @RequestBody Map<String, String> request,
                                                 HttpServletRequest httpRequest) {
        log.info("Mark as false alarm request for alert ID: {} from IP: {}", id, getClientIp(httpRequest));
        
        String reason = request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Reason is required");
            return ResponseEntity.badRequest().build();
        }
        
        // In real implementation, extract user ID from JWT token
        Long userId = 1L; // Placeholder
        
        Alert alert = alertProcessingService.markAsFalseAlarm(id, userId, reason);
        return ResponseEntity.ok(alert);
    }
    
    @PostMapping("/escalate/{id}")
    public ResponseEntity<Alert> escalateAlert(@PathVariable Long id,
                                             @RequestBody Map<String, String> request,
                                             HttpServletRequest httpRequest) {
        log.info("Escalate alert request for ID: {} from IP: {}", id, getClientIp(httpRequest));
        
        String escalateToUserIdStr = request.get("escalateToUserId");
        String reason = request.get("reason");
        
        if (escalateToUserIdStr == null || escalateToUserIdStr.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Escalate to user ID is required");
            return ResponseEntity.badRequest().build();
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Escalation reason is required");
            return ResponseEntity.badRequest().build();
        }
        
        Long escalateToUserId = Long.parseLong(escalateToUserIdStr);
        
        // In real implementation, extract user ID from JWT token
        Long userId = 1L; // Placeholder
        
        Alert alert = alertProcessingService.escalateAlert(id, escalateToUserId, reason);
        return ResponseEntity.ok(alert);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        Alert alert = alertProcessingService.getAlertById(id);
        return ResponseEntity.ok(alert);
    }
    
    @GetMapping("/atm/{atmId}")
    public ResponseEntity<List<Alert>> getAlertsByAtmId(@PathVariable Long atmId) {
        List<Alert> alerts = alertProcessingService.getAlertsByAtmId(atmId);
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Alert>> getActiveAlertsForUser(@PathVariable Long userId) {
        List<Alert> alerts = alertProcessingService.getActiveAlertsForUser(userId);
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<Alert>> getOverdueAlerts() {
        List<Alert> alerts = alertProcessingService.getOverdueAlerts();
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/needs-escalation")
    public ResponseEntity<List<Alert>> getAlertsNeedingEscalation() {
        List<Alert> alerts = alertProcessingService.getAlertsNeedingEscalation();
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping
    public ResponseEntity<Page<Alert>> getAllAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // This would need to be implemented in AlertProcessingService
        // For now, return empty page
        Page<Alert> alerts = Page.empty(pageable);
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAlertStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        
        // These would need to be implemented in AlertProcessingService
        statistics.put("totalAlerts", 0);
        statistics.put("criticalAlerts", 0);
        statistics.put("warningAlerts", 0);
        statistics.put("infoAlerts", 0);
        statistics.put("resolvedAlerts", 0);
        statistics.put("overdueAlerts", alertProcessingService.getOverdueAlerts().size());
        statistics.put("averageResponseTime", 0.0);
        statistics.put("averageResolutionTime", 0.0);
        
        return ResponseEntity.ok(statistics);
    }
    
    @PostMapping("/simulate-sms")
    public ResponseEntity<Map<String, String>> simulateSmsAlert(@RequestBody Map<String, String> request) {
        String fromNumber = request.get("fromNumber");
        String message = request.get("message");
        
        if (fromNumber == null || message == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "fromNumber and message are required");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Create SMS alert DTO
        com.atm.alert.dto.SmsAlertDto smsAlert = com.atm.alert.dto.SmsAlertDto.builder()
                .fromNumber(fromNumber)
                .message(message)
                .rawSms(message)
                .smsId("SIM-" + System.currentTimeMillis())
                .receivedAt(LocalDateTime.now())
                .serviceProvider("SIMULATOR")
                .build();
        
        // Process the alert
        Alert alert = alertProcessingService.processSmsAlert(smsAlert);
        
        Map<String, String> response = new HashMap<>();
        if (alert != null) {
            response.put("message", "SMS alert processed successfully");
            response.put("alertId", alert.getId().toString());
            response.put("severity", alert.getSeverity().name());
            response.put("category", alert.getCategory().name());
        } else {
            response.put("message", "SMS alert was duplicate or could not be processed");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "alert-processor-service");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

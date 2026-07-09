package com.security.alarm.controller;

import com.security.alarm.entity.AlertLog;
import com.security.alarm.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/sms-simulate")
    public ResponseEntity<AlertLog> simulateSMS(@RequestBody Map<String, String> smsData) {
        String simNumber = smsData.get("simNumber");
        String message = smsData.get("message");
        AlertLog savedLog = alertService.processIncomingSMS(simNumber, message);
        return ResponseEntity.ok(savedLog);
    }

    @GetMapping
    public ResponseEntity<List<AlertLog>> getAllAlerts(@RequestParam(required = false) String username) {
        return ResponseEntity.ok(alertService.getAllAlerts(username));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveAlert(
            @PathVariable Long id,
            @RequestParam String resolvedBy,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {
        
        try {
            String clientIp = request.getRemoteAddr();
            if (clientIp == null || clientIp.isEmpty() || "0:0:0:0:0:0:0:1".equals(clientIp)) {
                clientIp = "127.0.0.1";
            }
            
            AlertLog resolvedAlert = alertService.resolveAlert(id, resolvedBy, clientIp, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Alert resolved successfully");
            response.put("alert", resolvedAlert);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getAlertDetails(@PathVariable Long id) {
        AlertLog alert = alertService.getAlertWithDetails(id);
        if (alert == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(alert);
    }

    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Object>> getPendingCount() {
        Map<String, Object> response = new HashMap<>();
        response.put("pending", alertService.getPendingCount());
        response.put("resolved", alertService.getResolvedCount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AlertLog>> getPendingAlerts() {
        return ResponseEntity.ok(alertService.getPendingAlerts());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AlertLog>> getAlertsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(alertService.getAlertsByStatus(status));
    }
}
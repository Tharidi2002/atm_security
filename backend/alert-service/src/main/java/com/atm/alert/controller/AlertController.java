package com.atm.alert.controller;

import com.atm.alert.entity.Alert;
import com.atm.alert.entity.AlertSeverity;
import com.atm.alert.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        List<Alert> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        Optional<Alert> alert = alertService.getAlertById(id);
        return alert.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<List<Alert>> getAlertsByPhoneNumber(@PathVariable String phoneNumber) {
        List<Alert> alerts = alertService.getAlertsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Alert>> getAlertsBySeverity(@PathVariable AlertSeverity severity) {
        List<Alert> alerts = alertService.getAlertsBySeverity(severity);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/bank/{bankName}")
    public ResponseEntity<List<Alert>> getAlertsByBank(@PathVariable String bankName) {
        List<Alert> alerts = alertService.getAlertsByBankName(bankName);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/unacknowledged")
    public ResponseEntity<List<Alert>> getUnacknowledgedAlerts() {
        List<Alert> alerts = alertService.getUnacknowledgedAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/critical-unacknowledged")
    public ResponseEntity<List<Alert>> getCriticalUnacknowledgedAlerts() {
        List<Alert> alerts = alertService.getCriticalUnacknowledgedAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/bank/{bankName}/unacknowledged")
    public ResponseEntity<List<Alert>> getBankUnacknowledgedAlerts(@PathVariable String bankName) {
        List<Alert> alerts = alertService.getBankUnacknowledgedAlerts(bankName);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Alert>> getAlertsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Alert> alerts = alertService.getAlertsByDateRange(startDate, endDate);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Alert>> searchAlerts(@RequestParam String keyword) {
        List<Alert> alerts = alertService.searchAlerts(keyword);
        return ResponseEntity.ok(alerts);
    }

    @PostMapping
    public ResponseEntity<Alert> createAlert(@Valid @RequestBody Alert alert) {
        Alert newAlert = alertService.createAlert(alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAlert);
    }

    @PostMapping("/sms")
    public ResponseEntity<Alert> receiveSmsAlert(@RequestBody Map<String, String> smsData) {
        String message = smsData.get("message");
        String phoneNumber = smsData.get("phoneNumber");
        String bankName = smsData.get("bankName");
        String location = smsData.get("location");

        if (message == null || phoneNumber == null) {
            return ResponseEntity.badRequest().build();
        }

        Alert alert = alertService.createAlertFromSms(message, phoneNumber, bankName, location);
        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long id, @RequestBody Map<String, String> acknowledgeData) {
        try {
            String acknowledgedBy = acknowledgeData.get("acknowledgedBy");
            String incidentDetails = acknowledgeData.get("incidentDetails");

            if (acknowledgedBy == null) {
                return ResponseEntity.badRequest().build();
            }

            Alert alert = alertService.acknowledgeAlert(id, acknowledgedBy, incidentDetails);
            return ResponseEntity.ok(alert);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        try {
            alertService.deleteAlert(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistics/severity-count")
    public ResponseEntity<Map<String, Long>> getAlertStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Long> statistics = Map.of(
                "critical", alertService.getAlertCountBySeverityAndDateRange(AlertSeverity.CRITICAL, startDate, endDate),
                "warning", alertService.getAlertCountBySeverityAndDateRange(AlertSeverity.WARNING, startDate, endDate),
                "info", alertService.getAlertCountBySeverityAndDateRange(AlertSeverity.INFO, startDate, endDate)
        );
        return ResponseEntity.ok(statistics);
    }
}

package com.atmsecurity.alert.controller;

import com.atmsecurity.common.dto.ApiResponse;
import com.atmsecurity.common.security.UserPrincipal;
import com.atmsecurity.alert.entity.SecurityAlert;
import com.atmsecurity.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SecurityAlert>>> getAlerts(@AuthenticationPrincipal UserPrincipal principal) {
        if ("ADMIN".equals(principal.getRole())) {
            return ResponseEntity.ok(ApiResponse.ok(alertService.getAllAlerts()));
        } else {
            Long bankId = principal.getBankId();
            if (bankId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User does not have a bank assigned"));
            }
            return ResponseEntity.ok(ApiResponse.ok(alertService.getAlertsByBank(bankId)));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SecurityAlert>> getAlertById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        SecurityAlert alert = alertService.getAlertById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        if (!"ADMIN".equals(principal.getRole()) && !alert.getBankId().equals(principal.getBankId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access Denied: Alert belongs to another bank"));
        }

        return ResponseEntity.ok(ApiResponse.ok(alert));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<SecurityAlert>> acknowledgeAlert(
            @PathVariable Long id,
            @RequestBody String notes,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        SecurityAlert alert = alertService.getAlertById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        if (!"ADMIN".equals(principal.getRole()) && !alert.getBankId().equals(principal.getBankId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access Denied: Alert belongs to another bank"));
        }

        // Clean quotes from raw request body if passed as plain string
        String cleanNotes = notes != null ? notes.replaceAll("(^\"|\"$)", "") : "";

        SecurityAlert updated = alertService.acknowledgeAlert(id, principal.getId(), principal.getUsername(), cleanNotes);
        return ResponseEntity.ok(ApiResponse.ok("Alert acknowledged successfully", updated));
    }
}

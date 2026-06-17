package com.atmsecurity.alert.controller;

import com.atmsecurity.common.dto.ApiResponse;
import com.atmsecurity.alert.dto.SmsWebhookPayload;
import com.atmsecurity.alert.entity.SecurityAlert;
import com.atmsecurity.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final AlertService alertService;

    @PostMapping("/sms")
    public ResponseEntity<ApiResponse<SecurityAlert>> receiveSms(@RequestBody SmsWebhookPayload payload) {
        if (payload.getMessageId() == null || payload.getMessageId().isBlank()) {
            payload.setMessageId(UUID.randomUUID().toString());
        }

        if (payload.getSender() == null || payload.getSender().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Sender phone number is required"));
        }

        if (payload.getMessage() == null || payload.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Message content is required"));
        }

        SecurityAlert alert = alertService.processSmsWebhook(payload);
        return ResponseEntity.ok(ApiResponse.ok("Alert processed successfully", alert));
    }
}

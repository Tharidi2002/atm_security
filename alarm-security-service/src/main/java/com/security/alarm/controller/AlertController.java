package com.security.alarm.controller;

import com.security.alarm.entity.AlertLog;
import com.security.alarm.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    // 1. Test Lap එකේ Postman එකෙන් SMS එකක් ආවා වගේ simulate කරලා My Lap DB එක update කරන API එක
    @PostMapping("/sms-simulate")
    public ResponseEntity<AlertLog> simulateSMS(@RequestBody Map<String, String> smsData) {
        String simNumber = smsData.get("simNumber");
        String message = smsData.get("message");

        AlertLog savedLog = alertService.processIncomingSMS(simNumber, message);
        return ResponseEntity.ok(savedLog);
    }

    // 2. දැනට තියෙන ඔක්කොම Alerts ලැයිස්තුව බලන API එක
    @GetMapping
    public ResponseEntity<List<AlertLog>> getAllAlerts(@RequestParam(required = false) String username) {
        return ResponseEntity.ok(alertService.getAllAlerts(username));
    }
}

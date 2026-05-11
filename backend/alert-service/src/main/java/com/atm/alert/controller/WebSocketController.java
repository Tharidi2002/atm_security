package com.atm.alert.controller;

import com.atm.alert.entity.Alert;
import com.atm.alert.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/alert")
    @SendTo("/topic/alerts")
    public Alert broadcastAlert(Alert alert) {
        Alert savedAlert = alertService.createAlert(alert);
        return savedAlert;
    }

    public void sendRealTimeAlert(Alert alert) {
        messagingTemplate.convertAndSend("/topic/alerts", alert);
        
        // Send critical alerts to a separate topic for immediate attention
        if (alert.getSeverity().name().equals("CRITICAL")) {
            messagingTemplate.convertAndSend("/topic/critical-alerts", alert);
        }
    }

    public void sendBankSpecificAlert(String bankName, Alert alert) {
        messagingTemplate.convertAndSend("/topic/bank/" + bankName, alert);
    }
}

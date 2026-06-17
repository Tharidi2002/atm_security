package com.atmsecurity.alert.dto;

import lombok.Data;

@Data
public class SmsWebhookPayload {
    private String sender;
    private String message;
    private String messageId;
}

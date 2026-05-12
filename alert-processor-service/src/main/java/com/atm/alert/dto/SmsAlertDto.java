package com.atm.alert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsAlertDto {
    private String fromNumber;
    private String message;
    private String rawSms;
    private String smsId;
    private LocalDateTime receivedAt;
    private String serviceProvider;
}

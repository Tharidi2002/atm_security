package com.atmsecurity.alert.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiAnalysisRequest {
    private String stationCode;
    private String alertType;
    private String message;
    private String timestamp;
}

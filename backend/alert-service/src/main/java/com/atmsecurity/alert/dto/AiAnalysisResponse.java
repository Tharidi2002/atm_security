package com.atmsecurity.alert.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiAnalysisResponse {
    private BigDecimal anomalyScore;
    private boolean isAnomaly;
}

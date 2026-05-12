package com.atm.alert.scheduled;

import com.atm.alert.service.AlertProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEscalationScheduler {
    
    private final AlertProcessingService alertProcessingService;
    
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processAutomaticEscalations() {
        try {
            alertProcessingService.processAutomaticEscalations();
        } catch (Exception e) {
            log.error("Error in automatic escalation processing", e);
        }
    }
}

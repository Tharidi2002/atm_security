package com.atm.alert.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtmServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.atm-management-service}")
    private String atmServiceUrl;
    
    public boolean isAtmExists(Long atmId) {
        try {
            return webClient.get()
                    .uri(atmServiceUrl + "/api/atm/" + atmId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> true)
                    .onErrorReturn(false)
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to check ATM existence for ID: {}", atmId, e);
            return false;
        }
    }
    
    public Long findAtmIdByPhoneNumber(String phoneNumber) {
        try {
            return webClient.get()
                    .uri(atmServiceUrl + "/api/atm/phone/" + phoneNumber)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        // Extract ATM ID from response
                        Object idObj = response.get("id");
                        if (idObj instanceof Integer) {
                            return ((Integer) idObj).longValue();
                        } else if (idObj instanceof Long) {
                            return (Long) idObj;
                        } else if (idObj instanceof String) {
                            return Long.parseLong((String) idObj);
                        }
                        return null;
                    })
                    .onErrorReturn(null)
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to find ATM by phone number: {}", phoneNumber, e);
            return null;
        }
    }
}

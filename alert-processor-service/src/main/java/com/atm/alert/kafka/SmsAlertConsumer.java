package com.atm.alert.kafka;

import com.atm.alert.dto.SmsAlertDto;
import com.atm.alert.service.AlertProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsAlertConsumer {
    
    private final AlertProcessingService alertProcessingService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "${spring.kafka.consumer.properties.topic.sms-alerts:sms-alerts}",
        groupId = "${spring.kafka.consumer.group-id:alert-processor-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSmsAlert(@Payload String message,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                               @Header(KafkaHeaders.OFFSET) long offset,
                               Acknowledgment acknowledgment) {
        
        log.info("Received SMS alert from topic: {}, partition: {}, offset: {}", topic, partition, offset);
        
        try {
            SmsAlertDto smsAlert = parseSmsAlert(message);
            
            if (smsAlert == null) {
                log.error("Failed to parse SMS alert message: {}", message);
                acknowledgment.acknowledge();
                return;
            }
            
            // Process the alert
            alertProcessingService.processSmsAlert(smsAlert);
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
            log.info("Successfully processed SMS alert from: {}", smsAlert.getFromNumber());
            
        } catch (Exception e) {
            log.error("Error processing SMS alert: {}", message, e);
            
            // In case of error, you might want to:
            // 1. Acknowledge to avoid reprocessing (if the error is permanent)
            // 2. Send to dead letter queue
            // 3. Retry after some delay
            
            // For now, we acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }
    
    private SmsAlertDto parseSmsAlert(String message) {
        try {
            return objectMapper.readValue(message, SmsAlertDto.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse SMS alert JSON: {}", message, e);
            return null;
        }
    }
}

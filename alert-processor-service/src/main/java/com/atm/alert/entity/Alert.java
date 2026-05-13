package com.atm.alert.entity;

import com.atm.alert.enums.AlertCategory;
import com.atm.alert.enums.AlertSeverity;
import com.atm.alert.enums.AlertStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_atm", columnList = "atm_id"),
    @Index(name = "idx_alert_severity", columnList = "severity"),
    @Index(name = "idx_alert_status", columnList = "status"),
    @Index(name = "idx_alert_category", columnList = "category"),
    @Index(name = "idx_alert_created", columnList = "created_at"),
    @Index(name = "idx_alert_assigned", columnList = "assigned_to")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "atm_id", nullable = false)
    private Long atmId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private AlertSeverity severity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private AlertCategory category;
    
    @Column(name = "message", nullable = false, length = 1000)
    private String message;
    
    @Column(name = "raw_sms", columnDefinition = "TEXT")
    private String rawSms;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AlertStatus status;
    
    @Column(name = "assigned_to")
    private Long assignedTo;
    
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
    
    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;
    
    @Column(name = "investigation_started_at")
    private LocalDateTime investigationStartedAt;
    
    @Column(name = "investigation_started_by")
    private Long investigationStartedBy;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "resolved_by")
    private Long resolvedBy;
    
    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;
    
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;
    
    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;
    
    @Column(name = "escalated_to")
    private Long escalatedTo;
    
    @Column(name = "escalation_reason", length = 500)
    private String escalationReason;
    
    @Column(name = "is_false_alarm")
    @Builder.Default
    private Boolean falseAlarm = false;
    
    @Column(name = "false_alarm_reason", length = 500)
    private String falseAlarmReason;
    
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
    
    @Column(name = "source", length = 50)
    private String source;
    
    @Column(name = "external_id", length = 100)
    private String externalId;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AlertStatus.NEW;
        }
        if (source == null) {
            source = "SMS";
        }
        
        // Set SLA deadline based on severity
        if (slaDeadline == null) {
            if (severity == AlertSeverity.CRITICAL) {
                slaDeadline = createdAt.plusMinutes(10); // 10 minutes for critical
            } else if (severity == AlertSeverity.WARNING) {
                slaDeadline = createdAt.plusHours(1); // 1 hour for warning
            } else {
                slaDeadline = createdAt.plusHours(24); // 24 hours for info
            }
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isOverdue() {
        return slaDeadline != null && LocalDateTime.now().isAfter(slaDeadline) && 
               (status == AlertStatus.NEW || status == AlertStatus.ACKNOWLEDGED);
    }
    
    public boolean needsEscalation() {
        if (status == AlertStatus.RESOLVED || status == AlertStatus.FALSE_ALARM) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = slaDeadline != null ? slaDeadline : createdAt.plusHours(1);
        
        return now.isAfter(deadline);
    }
    
    public long getResponseTimeMinutes() {
        if (acknowledgedAt == null) {
            return -1; // Not acknowledged yet
        }
        return java.time.Duration.between(createdAt, acknowledgedAt).toMinutes();
    }
    
    public long getResolutionTimeMinutes() {
        if (resolvedAt == null) {
            return -1; // Not resolved yet
        }
        return java.time.Duration.between(createdAt, resolvedAt).toMinutes();
    }
}

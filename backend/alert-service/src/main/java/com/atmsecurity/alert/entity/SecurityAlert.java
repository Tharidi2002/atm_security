package com.atmsecurity.alert.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_message_id", unique = true, length = 100)
    private String externalMessageId;

    @Column(name = "station_id")
    private Long stationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "station_id", insertable = false, updatable = false)
    private StationRef station;

    @Column(name = "bank_id", nullable = false)
    private Long bankId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bank_id", insertable = false, updatable = false)
    private BankRef bank;

    @Column(name = "alert_type", nullable = false, length = 80)
    private String alertType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity = Severity.INFO;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "raw_sms", columnDefinition = "TEXT")
    private String rawSms;

    @Column(name = "sender_phone_hash", length = 64)
    private String senderPhoneHash;

    @Column(length = 100)
    private String zone;

    @Builder.Default
    @Column(nullable = false)
    private boolean acknowledged = false;

    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "response_notes", columnDefinition = "TEXT")
    private String responseNotes;

    @Column(name = "anomaly_score", precision = 5, scale = 4)
    private BigDecimal anomalyScore;

    @Builder.Default
    @Column(name = "is_anomaly", nullable = false)
    private boolean isAnomaly = false;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String source = "SMS";

    @Builder.Default
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }
}

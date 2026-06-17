package com.atmsecurity.station.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "atm_stations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtmStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code", nullable = false, unique = true, length = 50)
    private String stationCode;

    @Column(name = "phone_number_enc", nullable = false, length = 512)
    private String phoneNumberEnc;

    @Column(name = "phone_number_hash", nullable = false, length = 64)
    private String phoneNumberHash;

    @Column(name = "bank_id", nullable = false)
    private Long bankId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bank_id", insertable = false, updatable = false)
    private BankRef bank;

    @Column(name = "location_name", nullable = false, length = 255)
    private String locationName;

    @Column(name = "location_address", length = 500)
    private String locationAddress;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_alert_at")
    private LocalDateTime lastAlertAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

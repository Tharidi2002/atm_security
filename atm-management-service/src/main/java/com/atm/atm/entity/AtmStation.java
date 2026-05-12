package com.atm.atm.entity;

import com.atm.atm.enums.AtmStatus;
import com.atm.atm.enums.ZoneType;
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
@Table(name = "atm_stations", indexes = {
    @Index(name = "idx_atm_phone", columnList = "phone_number"),
    @Index(name = "idx_atm_bank", columnList = "bank_id"),
    @Index(name = "idx_atm_status", columnList = "status"),
    @Index(name = "idx_atm_heartbeat", columnList = "last_heartbeat")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AtmStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "phone_number", unique = true, nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(name = "bank_id", nullable = false)
    private Long bankId;
    
    @Column(name = "location_name", nullable = false, length = 200)
    private String locationName;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "address", length = 300)
    private String address;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "district", length = 100)
    private String district;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type", nullable = false)
    private ZoneType zoneType;
    
    @Column(name = "firmware_version", length = 20)
    private String firmwareVersion;
    
    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AtmStatus status;
    
    @Column(name = "installation_date")
    private LocalDateTime installationDate;
    
    @Column(name = "last_maintenance")
    private LocalDateTime lastMaintenance;
    
    @Column(name = "next_maintenance_due")
    private LocalDateTime nextMaintenanceDue;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "qr_code", length = 100)
    private String qrCode;
    
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
            status = AtmStatus.ACTIVE;
        }
        if (zoneType == null) {
            zoneType = ZoneType.GENERAL;
        }
        if (firmwareVersion == null) {
            firmwareVersion = "1.0.0";
        }
        if (qrCode == null) {
            qrCode = generateQrCode();
        }
        if (installationDate == null) {
            installationDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generateQrCode() {
        return "ATM-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    public boolean isOnline() {
        if (lastHeartbeat == null) {
            return false;
        }
        return lastHeartbeat.isAfter(LocalDateTime.now().minusMinutes(5));
    }
    
    public boolean needsMaintenance() {
        if (nextMaintenanceDue == null) {
            return false;
        }
        return nextMaintenanceDue.isBefore(LocalDateTime.now().plusDays(7));
    }
    
    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
        if (this.status == AtmStatus.OFFLINE) {
            this.status = AtmStatus.ACTIVE;
        }
    }
}

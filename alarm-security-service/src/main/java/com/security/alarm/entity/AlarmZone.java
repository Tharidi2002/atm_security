package com.security.alarm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alarm_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== FIX: Add cascade delete =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id", nullable = false)
    private AlarmSystem alarmSystem;

    @Column(name = "zone_number", nullable = false)
    private Integer zoneNumber;

    @Column(name = "zone_name", nullable = false, length = 100)
    private String zoneName;

    @Column(name = "zone_type", nullable = false)
    private Integer zoneType = 1;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
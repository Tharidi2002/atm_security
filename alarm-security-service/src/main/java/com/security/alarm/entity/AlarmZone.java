package com.security.alarm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alarm_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "system_id", nullable = false)
    private AlarmSystem alarmSystem;

    @Column(name = "zone_number", nullable = false)
    private Integer zoneNumber;

    @Column(name = "zone_name", nullable = false, length = 100)
    private String zoneName;

    // ===== NEW FIELDS =====
    @Column(name = "zone_type", nullable = false)
    private Integer zoneType = 1; // 0=OFF, 1=PERIMETER, 2=DELAY, 3=AWAY, 4=24HR, 5=MUTE, 6=EXIT, 7=BELL, 8=SOS

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt = java.time.LocalDateTime.now();
}
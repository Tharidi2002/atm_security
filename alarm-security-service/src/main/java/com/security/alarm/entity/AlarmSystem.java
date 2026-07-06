package com.security.alarm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alarm_systems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmSystem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "system_code", unique = true, nullable = false)
    private String systemCode;

    @Column(nullable = false)
    private String location;

    @Column(name = "sim_number", nullable = false)
    private String simNumber;

    private String status = "ACTIVE";

    @Column(name = "last_status_changed_at")
    private java.time.LocalDateTime lastStatusChangedAt = java.time.LocalDateTime.now();
}

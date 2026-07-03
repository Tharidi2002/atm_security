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

    @Column(name = "zone_name", nullable = false)
    private String zoneName;
}

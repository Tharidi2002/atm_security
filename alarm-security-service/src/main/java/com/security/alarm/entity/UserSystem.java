package com.security.alarm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_systems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "system_id", nullable = false)
    private Long systemId;
}

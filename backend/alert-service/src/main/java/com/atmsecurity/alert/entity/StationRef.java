package com.atmsecurity.alert.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "station_refs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationRef {
    @Id
    private Long id;

    @Column(name = "station_code", nullable = false, length = 50)
    private String stationCode;

    @Column(name = "bank_id", nullable = false)
    private Long bankId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bank_id", insertable = false, updatable = false)
    private BankRef bank;

    @Column(name = "location_name", nullable = false, length = 255)
    private String locationName;

    @Column(name = "phone_number_hash", length = 64)
    private String phoneNumberHash;

    @Column(nullable = false)
    private boolean active = true;
}

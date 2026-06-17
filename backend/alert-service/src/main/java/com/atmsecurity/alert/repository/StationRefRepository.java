package com.atmsecurity.alert.repository;

import com.atmsecurity.alert.entity.StationRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StationRefRepository extends JpaRepository<StationRef, Long> {
    Optional<StationRef> findByPhoneNumberHash(String phoneNumberHash);
    Optional<StationRef> findByStationCode(String stationCode);
}

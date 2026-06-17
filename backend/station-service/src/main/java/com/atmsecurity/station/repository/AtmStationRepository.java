package com.atmsecurity.station.repository;

import com.atmsecurity.station.entity.AtmStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtmStationRepository extends JpaRepository<AtmStation, Long> {
    Optional<AtmStation> findByStationCode(String stationCode);
    Optional<AtmStation> findByPhoneNumberHash(String phoneNumberHash);
    List<AtmStation> findByBankId(Long bankId);
}

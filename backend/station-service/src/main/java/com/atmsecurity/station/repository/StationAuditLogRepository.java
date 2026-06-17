package com.atmsecurity.station.repository;

import com.atmsecurity.station.entity.StationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationAuditLogRepository extends JpaRepository<StationAuditLog, Long> {
    List<StationAuditLog> findByStationId(Long stationId);
}

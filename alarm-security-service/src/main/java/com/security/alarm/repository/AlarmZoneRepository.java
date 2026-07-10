package com.security.alarm.repository;

import com.security.alarm.entity.AlarmZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface AlarmZoneRepository extends JpaRepository<AlarmZone, Long> {
    
    List<AlarmZone> findByAlarmSystemIdOrderByZoneNumberAsc(Long systemId);
    
    Optional<AlarmZone> findByAlarmSystemIdAndZoneNumber(Long systemId, Integer zoneNumber);
    
    // ===== FIX: Delete all zones by system ID =====
    @Modifying
    @Transactional
    @Query("DELETE FROM AlarmZone z WHERE z.alarmSystem.id = :systemId")
    void deleteBySystemId(Long systemId);
    
    long countByAlarmSystemId(Long systemId);
}
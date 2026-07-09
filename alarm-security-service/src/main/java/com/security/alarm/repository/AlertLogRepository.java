package com.security.alarm.repository;

import com.security.alarm.entity.AlertLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {
    List<AlertLog> findAllByOrderByReceivedAtDesc();
    List<AlertLog> findAllByAlarmSystemIdInOrderByReceivedAtDesc(List<Long> systemIds);
    
    long countByStatus(String status);
    
    @Query("SELECT COUNT(a) FROM AlertLog a WHERE a.status = 'RESOLVED'")
    long countResolved();
    
    // ===== NEW: Get alert with system details =====
    @Query("SELECT a FROM AlertLog a LEFT JOIN FETCH a.alarmSystem WHERE a.id = :id")
    AlertLog findByIdWithSystem(Long id);
}
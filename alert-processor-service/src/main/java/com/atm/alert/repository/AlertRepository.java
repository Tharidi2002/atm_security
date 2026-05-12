package com.atm.alert.repository;

import com.atm.alert.entity.Alert;
import com.atm.alert.enums.AlertCategory;
import com.atm.alert.enums.AlertSeverity;
import com.atm.alert.enums.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    List<Alert> findByAtmId(Long atmId);
    
    Page<Alert> findByAtmId(Long atmId, Pageable pageable);
    
    List<Alert> findBySeverity(AlertSeverity severity);
    
    Page<Alert> findBySeverity(AlertSeverity severity, Pageable pageable);
    
    List<Alert> findByStatus(AlertStatus status);
    
    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);
    
    List<Alert> findByCategory(AlertCategory category);
    
    List<Alert> findByAtmIdAndStatus(Long atmId, AlertStatus status);
    
    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<Alert> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Alert a WHERE a.slaDeadline < :now AND a.status IN :statuses")
    List<Alert> findOverdueAlerts(@Param("now") LocalDateTime now, 
                                 @Param("statuses") List<AlertStatus> statuses);
    
    @Query("SELECT a FROM Alert a WHERE a.assignedTo = :userId AND a.status != :resolvedStatus")
    List<Alert> findActiveAlertsForUser(@Param("userId") Long userId, 
                                       @Param("resolvedStatus") AlertStatus resolvedStatus);
    
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.atmId = :atmId AND a.status != :resolvedStatus")
    long countActiveAlertsForAtm(@Param("atmId") Long atmId, 
                               @Param("resolvedStatus") AlertStatus resolvedStatus);
    
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.severity = :severity AND a.status != :resolvedStatus")
    long countActiveAlertsBySeverity(@Param("severity") AlertSeverity severity, 
                                   @Param("resolvedStatus") AlertStatus resolvedStatus);
    
    @Query("SELECT a FROM Alert a WHERE a.message LIKE %:search% OR a.rawSms LIKE %:search%")
    List<Alert> searchAlerts(@Param("search") String search);
    
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.createdAt >= :since")
    long countAlertsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.createdAt >= :since AND a.severity = :severity")
    long countAlertsSinceBySeverity(@Param("since") LocalDateTime since, 
                                   @Param("severity") AlertSeverity severity);
    
    @Query("SELECT a FROM Alert a WHERE a.externalId = :externalId AND a.source = :source")
    Optional<Alert> findByExternalIdAndSource(@Param("externalId") String externalId, 
                                            @Param("source") String source);
    
    @Query("SELECT a FROM Alert a WHERE a.slaDeadline < :now AND a.status IN :statuses ORDER BY a.slaDeadline ASC")
    List<Alert> findAlertsNeedingEscalation(@Param("now") LocalDateTime now, 
                                           @Param("statuses") List<AlertStatus> statuses);
    
    @Query("SELECT a FROM Alert a WHERE a.isFalseAlarm = true AND a.createdAt BETWEEN :startDate AND :endDate")
    List<Alert> findFalseAlarmsInPeriod(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (a.acknowledgedAt - a.createdAt))/60) FROM Alert a WHERE a.acknowledgedAt IS NOT NULL AND a.createdAt >= :since")
    Double getAverageResponseTimeSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (a.resolvedAt - a.createdAt))/60) FROM Alert a WHERE a.resolvedAt IS NOT NULL AND a.createdAt >= :since")
    Double getAverageResolutionTimeSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT a.severity, COUNT(a) FROM Alert a WHERE a.createdAt >= :since GROUP BY a.severity")
    List<Object[]> getAlertStatisticsBySeveritySince(@Param("since") LocalDateTime since);
    
    @Query("SELECT a.category, COUNT(a) FROM Alert a WHERE a.createdAt >= :since GROUP BY a.category")
    List<Object[]> getAlertStatisticsByCategorySince(@Param("since") LocalDateTime since);
}

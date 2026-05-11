package com.atm.alert.repository;

import com.atm.alert.entity.Alert;
import com.atm.alert.entity.AlertSeverity;
import com.atm.alert.entity.AlertCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    List<Alert> findByPhoneNumber(String phoneNumber);
    List<Alert> findBySeverity(AlertSeverity severity);
    List<Alert> findByCategory(AlertCategory category);
    List<Alert> findByBankName(String bankName);
    List<Alert> findByIsAcknowledged(Boolean isAcknowledged);
    
    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<Alert> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Alert a WHERE a.severity = :severity AND a.createdAt BETWEEN :startDate AND :endDate")
    List<Alert> findBySeverityAndCreatedAtBetween(@Param("severity") AlertSeverity severity,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Alert a WHERE a.bankName = :bankName AND a.isAcknowledged = :isAcknowledged ORDER BY a.createdAt DESC")
    List<Alert> findByBankNameAndIsAcknowledgedOrderByCreatedAtDesc(@Param("bankName") String bankName,
                                                                   @Param("isAcknowledged") Boolean isAcknowledged);
    
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.severity = :severity AND a.createdAt BETWEEN :startDate AND :endDate")
    Long countBySeverityAndCreatedAtBetween(@Param("severity") AlertSeverity severity,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Alert a WHERE a.message LIKE %:keyword% OR a.bankName LIKE %:keyword% OR a.location LIKE %:keyword%")
    List<Alert> searchAlerts(@Param("keyword") String keyword);
    
    @Query("SELECT a FROM Alert a WHERE a.isAcknowledged = false ORDER BY a.createdAt DESC")
    List<Alert> findUnacknowledgedAlertsOrderByCreatedAtDesc();
    
    @Query("SELECT a FROM Alert a WHERE a.severity = 'CRITICAL' AND a.isAcknowledged = false ORDER BY a.createdAt DESC")
    List<Alert> findCriticalUnacknowledgedAlerts();
}

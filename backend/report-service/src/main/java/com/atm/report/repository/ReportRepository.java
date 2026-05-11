package com.atm.report.repository;

import com.atm.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    List<Report> findByReportType(String reportType);
    
    List<Report> findByGeneratedBy(String generatedBy);
    
    List<Report> findByStatus(String status);
    
    List<Report> findByScheduled(Boolean scheduled);
    
    Optional<Report> findByFileName(String fileName);
    
    @Query("SELECT r FROM Report r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<Report> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT r FROM Report r WHERE r.reportType = :reportType AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<Report> findByReportTypeAndDateRange(
        @Param("reportType") String reportType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportType = :reportType")
    Long countByReportType(@Param("reportType") String reportType);
    
    @Query("SELECT r FROM Report r WHERE r.scheduled = true AND r.status = 'PENDING'")
    List<Report> findPendingScheduledReports();
}

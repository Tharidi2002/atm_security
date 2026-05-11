package com.atm.report.service;

import com.atm.report.entity.Report;
import com.atm.report.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportService {
    
    @Autowired
    private ReportRepository reportRepository;
    
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }
    
    public Page<Report> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }
    
    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }
    
    public Report createReport(Report report) {
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }
    
    public Report updateReport(Long id, Report reportDetails) {
        Optional<Report> optionalReport = reportRepository.findById(id);
        if (optionalReport.isPresent()) {
            Report report = optionalReport.get();
            report.setReportType(reportDetails.getReportType());
            report.setGeneratedBy(reportDetails.getGeneratedBy());
            report.setParameters(reportDetails.getParameters());
            report.setFilePath(reportDetails.getFilePath());
            report.setFileName(reportDetails.getFileName());
            report.setFileSize(reportDetails.getFileSize());
            report.setFileFormat(reportDetails.getFileFormat());
            report.setScheduled(reportDetails.getScheduled());
            report.setScheduledFrequency(reportDetails.getScheduledFrequency());
            report.setRecipients(reportDetails.getRecipients());
            report.setStatus(reportDetails.getStatus());
            report.setErrorMessage(reportDetails.getErrorMessage());
            report.setUpdatedAt(LocalDateTime.now());
            report.setGeneratedAt(reportDetails.getGeneratedAt());
            return reportRepository.save(report);
        }
        throw new RuntimeException("Report not found with id: " + id);
    }
    
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
    
    public List<Report> getReportsByType(String reportType) {
        return reportRepository.findByReportType(reportType);
    }
    
    public List<Report> getReportsByGeneratedBy(String generatedBy) {
        return reportRepository.findByGeneratedBy(generatedBy);
    }
    
    public List<Report> getReportsByStatus(String status) {
        return reportRepository.findByStatus(status);
    }
    
    public List<Report> getReportsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return reportRepository.findByDateRange(startDate, endDate);
    }
    
    public List<Report> getPendingScheduledReports() {
        return reportRepository.findPendingScheduledReports();
    }
    
    public Map<String, Object> generateSummaryReport(Map<String, Object> parameters) {
        Map<String, Object> report = new HashMap<>();
        
        // In a real implementation, this would fetch data from other services
        // For now, returning sample data
        report.put("totalAlerts", 156);
        report.put("criticalAlerts", 12);
        report.put("warningAlerts", 45);
        report.put("infoAlerts", 99);
        report.put("acknowledgedAlerts", 134);
        report.put("unacknowledgedAlerts", 22);
        report.put("activeStations", 45);
        report.put("inactiveStations", 3);
        
        if (parameters.containsKey("bankName")) {
            report.put("bankName", parameters.get("bankName"));
        }
        
        report.put("generatedAt", LocalDateTime.now());
        report.put("reportType", "SUMMARY");
        
        return report;
    }
    
    public Map<String, Object> generateAlertsBySeverityReport(Map<String, Object> parameters) {
        Map<String, Object> report = new HashMap<>();
        
        Map<String, Long> severityCounts = new HashMap<>();
        severityCounts.put("CRITICAL", 12L);
        severityCounts.put("WARNING", 45L);
        severityCounts.put("INFO", 99L);
        
        report.put("severityCounts", severityCounts);
        report.put("totalAlerts", 156L);
        report.put("dateRange", parameters);
        report.put("generatedAt", LocalDateTime.now());
        report.put("reportType", "ALERTS_BY_SEVERITY");
        
        return report;
    }
    
    public Map<String, Object> generateAlertsByBankReport(Map<String, Object> parameters) {
        Map<String, Object> report = new HashMap<>();
        
        Map<String, Long> bankCounts = new HashMap<>();
        bankCounts.put("Bank of America", 45L);
        bankCounts.put("Chase Bank", 38L);
        bankCounts.put("Wells Fargo", 32L);
        bankCounts.put("Citibank", 28L);
        bankCounts.put("Others", 13L);
        
        report.put("bankCounts", bankCounts);
        report.put("totalAlerts", 156L);
        report.put("dateRange", parameters);
        report.put("generatedAt", LocalDateTime.now());
        report.put("reportType", "ALERTS_BY_BANK");
        
        return report;
    }
    
    public Map<String, Object> generateStationStatusReport(Map<String, Object> parameters) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("totalStations", 48);
        report.put("activeStations", 45);
        report.put("inactiveStations", 3);
        report.put("stationsNeedingMaintenance", 7);
        
        Map<String, Long> bankStationCounts = new HashMap<>();
        bankStationCounts.put("Bank of America", 12L);
        bankStationCounts.put("Chase Bank", 10L);
        bankStationCounts.put("Wells Fargo", 9L);
        bankStationCounts.put("Citibank", 8L);
        bankStationCounts.put("Others", 9L);
        
        report.put("stationsByBank", bankStationCounts);
        report.put("generatedAt", LocalDateTime.now());
        report.put("reportType", "STATION_STATUS");
        
        return report;
    }
    
    public Report markReportAsGenerated(Long id, String filePath, String fileName, Long fileSize) {
        Optional<Report> optionalReport = reportRepository.findById(id);
        if (optionalReport.isPresent()) {
            Report report = optionalReport.get();
            report.setStatus("COMPLETED");
            report.setFilePath(filePath);
            report.setFileName(fileName);
            report.setFileSize(fileSize);
            report.setGeneratedAt(LocalDateTime.now());
            report.setUpdatedAt(LocalDateTime.now());
            return reportRepository.save(report);
        }
        throw new RuntimeException("Report not found with id: " + id);
    }
    
    public Report markReportAsFailed(Long id, String errorMessage) {
        Optional<Report> optionalReport = reportRepository.findById(id);
        if (optionalReport.isPresent()) {
            Report report = optionalReport.get();
            report.setStatus("FAILED");
            report.setErrorMessage(errorMessage);
            report.setUpdatedAt(LocalDateTime.now());
            return reportRepository.save(report);
        }
        throw new RuntimeException("Report not found with id: " + id);
    }
    
    public Map<String, Object> getReportStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalReports", reportRepository.count());
        stats.put("completedReports", reportRepository.countByStatus("COMPLETED"));
        stats.put("pendingReports", reportRepository.countByStatus("PENDING"));
        stats.put("failedReports", reportRepository.countByStatus("FAILED"));
        stats.put("scheduledReports", reportRepository.findByScheduled(true).size());
        
        // Report type statistics
        List<Report> allReports = reportRepository.findAll();
        Map<String, Long> reportTypeStats = allReports.stream()
            .collect(Collectors.groupingBy(Report::getReportType, Collectors.counting()));
        stats.put("reportsByType", reportTypeStats);
        
        return stats;
    }
}

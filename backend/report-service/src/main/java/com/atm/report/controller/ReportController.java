package com.atm.report.controller;

import com.atm.report.entity.Report;
import com.atm.report.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        List<Report> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        Optional<Report> report = reportService.getReportById(id);
        return report.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{reportType}")
    public ResponseEntity<List<Report>> getReportsByType(@PathVariable String reportType) {
        List<Report> reports = reportService.getReportsByType(reportType);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/generated-by/{generatedBy}")
    public ResponseEntity<List<Report>> getReportsByGeneratedBy(@PathVariable String generatedBy) {
        List<Report> reports = reportService.getReportsByGeneratedBy(generatedBy);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Report>> getReportsByStatus(@PathVariable String status) {
        List<Report> reports = reportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Report>> getReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Report> reports = reportService.getReportsByDateRange(startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    @PostMapping
    public ResponseEntity<Report> createReport(@Valid @RequestBody Report report) {
        Report newReport = reportService.createReport(report);
        return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Report> updateReport(@PathVariable Long id, @Valid @RequestBody Report reportDetails) {
        try {
            Report updatedReport = reportService.updateReport(id, reportDetails);
            return ResponseEntity.ok(updatedReport);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummaryReport(
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Map<String, Object> parameters = new HashMap<>();
        if (bankName != null) parameters.put("bankName", bankName);
        if (startDate != null) parameters.put("startDate", startDate);
        if (endDate != null) parameters.put("endDate", endDate);
        
        Map<String, Object> report = reportService.generateSummaryReport(parameters);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/alerts-by-severity")
    public ResponseEntity<Map<String, Object>> getAlertsBySeverityReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        Map<String, Object> parameters = Map.of("start", startDate, "end", endDate);
        Map<String, Object> report = reportService.generateAlertsBySeverityReport(parameters);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/alerts-by-bank")
    public ResponseEntity<Map<String, Object>> getAlertsByBankReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        Map<String, Object> parameters = Map.of("start", startDate, "end", endDate);
        Map<String, Object> report = reportService.generateAlertsByBankReport(parameters);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/station-status")
    public ResponseEntity<Map<String, Object>> getStationStatusReport() {
        Map<String, Object> parameters = new HashMap<>();
        Map<String, Object> report = reportService.generateStationStatusReport(parameters);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReportStatistics() {
        Map<String, Object> statistics = reportService.getReportStatistics();
        return ResponseEntity.ok(statistics);
    }

    @PutMapping("/{id}/mark-generated")
    public ResponseEntity<Report> markReportAsGenerated(@PathVariable Long id, @RequestBody Map<String, Object> updateData) {
        try {
            String filePath = (String) updateData.get("filePath");
            String fileName = (String) updateData.get("fileName");
            Long fileSize = updateData.get("fileSize") != null ? 
                Long.valueOf(updateData.get("fileSize").toString()) : null;
            
            Report updatedReport = reportService.markReportAsGenerated(id, filePath, fileName, fileSize);
            return ResponseEntity.ok(updatedReport);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/mark-failed")
    public ResponseEntity<Report> markReportAsFailed(@PathVariable Long id, @RequestBody Map<String, String> errorData) {
        try {
            String errorMessage = errorData.get("errorMessage");
            Report updatedReport = reportService.markReportAsFailed(id, errorMessage);
            return ResponseEntity.ok(updatedReport);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

package com.security.alarm.controller;

import com.security.alarm.entity.AlertLog;
import com.security.alarm.entity.User;
import com.security.alarm.entity.UserSystem;
import com.security.alarm.repository.AlertLogRepository;
import com.security.alarm.repository.AlarmSystemRepository;
import com.security.alarm.repository.UserRepository;
import com.security.alarm.repository.UserSystemRepository;
import com.security.alarm.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "false")

public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final UserSystemRepository userSystemRepository;
    private final AlarmSystemRepository alarmSystemRepository;
    private final AlertLogRepository alertLogRepository;

    public ReportController(ReportService reportService,
                            UserRepository userRepository,
                            UserSystemRepository userSystemRepository,
                            AlarmSystemRepository alarmSystemRepository,
                            AlertLogRepository alertLogRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.userSystemRepository = userSystemRepository;
        this.alarmSystemRepository = alarmSystemRepository;
        this.alertLogRepository = alertLogRepository;
    }

    // ===== GET SUMMARY DATA =====
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String username) {
        
        LocalDateTime fromDate = parseDate(from);
        LocalDateTime toDate = parseDate(to);
        String role = "ADMIN";
        
        List<AlertLog> alerts;
        
        if (username != null && !username.trim().isEmpty()) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                role = userOpt.get().getRole();
                if ("USER".equalsIgnoreCase(role)) {
                    List<UserSystem> userSystems = userSystemRepository.findAllByUserId(userOpt.get().getId());
                    List<Long> systemIds = userSystems.stream().map(UserSystem::getSystemId).toList();
                    if (!systemIds.isEmpty()) {
                        alerts = alertLogRepository.findByAlarmSystemIdInAndReceivedAtBetween(systemIds, fromDate, toDate);
                    } else {
                        alerts = List.of();
                    }
                } else {
                    alerts = alertLogRepository.findByReceivedAtBetween(fromDate, toDate);
                }
            } else {
                alerts = alertLogRepository.findByReceivedAtBetween(fromDate, toDate);
            }
        } else {
            alerts = alertLogRepository.findByReceivedAtBetween(fromDate, toDate);
        }
        
        Map<String, Object> summary = reportService.generateSummary(alerts, username != null ? username : "System", role);
        return ResponseEntity.ok(summary);
    }

    // ===== EXPORT PROFESSIONAL PDF =====
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPDF(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String username) {
        
        LocalDateTime fromDate = parseDate(from);
        LocalDateTime toDate = parseDate(to);
        
        List<AlertLog> alerts;
        String systemName = "All Systems";
        String role = "ADMIN";
        String userName = "System";
        
        if (username != null && !username.trim().isEmpty()) {
            userName = username;
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                role = userOpt.get().getRole();
                if ("USER".equalsIgnoreCase(role)) {
                    List<UserSystem> userSystems = userSystemRepository.findAllByUserId(userOpt.get().getId());
                    List<Long> systemIds = userSystems.stream().map(UserSystem::getSystemId).toList();
                    if (!systemIds.isEmpty()) {
                        alerts = alertLogRepository.findByAlarmSystemIdInAndReceivedAtBetween(systemIds, fromDate, toDate);
                        List<com.security.alarm.entity.AlarmSystem> systems = alarmSystemRepository.findAllById(systemIds);
                        systemName = systems.stream()
                            .map(com.security.alarm.entity.AlarmSystem::getSystemCode)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("All Systems");
                    } else {
                        alerts = List.of();
                    }
                } else {
                    alerts = alertLogRepository.findByReceivedAtBetween(fromDate, toDate);
                }
            } else {
                alerts = alertLogRepository.findByReceivedAtBetween(fromDate, toDate);
            }
        } else {
            alerts = alertLogRepository.findByReceivedAtBetween(fromDate, toDate);
        }
        
        Map<String, Object> summary = reportService.generateSummary(alerts, userName, role);
        byte[] pdfBytes = reportService.generateProfessionalPDF(summary, fromDate, toDate, systemName, userName, role);
        
        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.status(500).build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "Alarm_Professional_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    // ===== EXPORT PROFESSIONAL EXCEL =====
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String username) {
        
        LocalDateTime fromDate = parseDate(from);
        LocalDateTime toDate = parseDate(to);
        
        List<AlertLog> alerts;
        String role = "ADMIN";
        String userName = "System";
        
        if (username != null && !username.trim().isEmpty()) {
            userName = username;
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                role = userOpt.get().getRole();
                if ("USER".equalsIgnoreCase(role)) {
                    List<UserSystem> userSystems = userSystemRepository.findAllByUserId(userOpt.get().getId());
                    List<Long> systemIds = userSystems.stream().map(UserSystem::getSystemId).toList();
                    if (!systemIds.isEmpty()) {
                        alerts = alertLogRepository.findByAlarmSystemIdInAndReceivedAtBetween(systemIds, fromDate, toDate);
                    } else {
                        alerts = List.of();
                    }
                } else {
                    alerts = alertLogRepository.findByReceivedAtBetween(fromDate, toDate);
                }
            } else {
                alerts = alertLogRepository.findByReceivedAtBetween(fromDate, toDate);
            }
        } else {
            alerts = alertLogRepository.findByReceivedAtBetween(fromDate, toDate);
        }
        
        Map<String, Object> summary = reportService.generateSummary(alerts, userName, role);
        byte[] excelBytes = reportService.generateProfessionalExcel(summary, fromDate, toDate, userName, role);
        
        if (excelBytes == null || excelBytes.length == 0) {
            return ResponseEntity.status(500).build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String filename = "Alarm_Professional_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        
        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }

    // ===== SYSTEM HEALTH =====
    @GetMapping("/system-health")
    public ResponseEntity<?> getSystemHealth(@RequestParam(required = false) String username) {
        List<com.security.alarm.entity.AlarmSystem> systems;
        
        if (username != null && !username.trim().isEmpty()) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent() && "USER".equalsIgnoreCase(userOpt.get().getRole())) {
                List<UserSystem> userSystems = userSystemRepository.findAllByUserId(userOpt.get().getId());
                List<Long> systemIds = userSystems.stream().map(UserSystem::getSystemId).toList();
                systems = alarmSystemRepository.findAllById(systemIds);
            } else {
                systems = alarmSystemRepository.findAll();
            }
        } else {
            systems = alarmSystemRepository.findAll();
        }
        
        Map<String, Object> health = reportService.generateSystemHealth(systems);
        return ResponseEntity.ok(health);
    }

    // ===== USER PERFORMANCE =====
    @GetMapping("/user-performance")
    public ResponseEntity<?> getUserPerformance(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        
        LocalDateTime fromDate = parseDate(from);
        LocalDateTime toDate = parseDate(to);
        
        List<Map<String, Object>> performance = reportService.generateUserPerformance(fromDate, toDate);
        return ResponseEntity.ok(performance);
    }

    // ===== HELPER: Parse date =====
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.now().minusDays(30);
        }
        try {
            return LocalDateTime.parse(dateStr + "T00:00:00");
        } catch (Exception e) {
            return LocalDateTime.now().minusDays(30);
        }
    }
}
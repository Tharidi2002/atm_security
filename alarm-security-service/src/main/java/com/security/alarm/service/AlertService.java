package com.security.alarm.service;

import com.security.alarm.entity.AlertLog;
import com.security.alarm.entity.AlarmSystem;
import com.security.alarm.entity.AlarmZone;
import com.security.alarm.entity.User;
import com.security.alarm.entity.UserSystem;
import com.security.alarm.repository.AlertLogRepository;
import com.security.alarm.repository.AlarmSystemRepository;
import com.security.alarm.repository.AlarmZoneRepository;
import com.security.alarm.repository.UserRepository;
import com.security.alarm.repository.UserSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final AlertLogRepository alertLogRepository;
    private final AlarmSystemRepository alarmSystemRepository;
    private final AlarmZoneRepository alarmZoneRepository;
    private final UserRepository userRepository;
    private final UserSystemRepository userSystemRepository;

    public AlertService(AlertLogRepository alertLogRepository, 
                        AlarmSystemRepository alarmSystemRepository,
                        AlarmZoneRepository alarmZoneRepository,
                        UserRepository userRepository,
                        UserSystemRepository userSystemRepository) {
        this.alertLogRepository = alertLogRepository;
        this.alarmSystemRepository = alarmSystemRepository;
        this.alarmZoneRepository = alarmZoneRepository;
        this.userRepository = userRepository;
        this.userSystemRepository = userSystemRepository;
    }

    // Process incoming SMS
    // Accept optional atmCode — prefer to find system by atmCode first, fallback to SIM number
    public AlertLog processIncomingSMS(String fromSimNumber, String smsContent, String atmCode) {
        AlertLog alertLog = new AlertLog();
        alertLog.setReceivedAt(LocalDateTime.now());
        
        String cleanMessage = smsContent;
        if (fromSimNumber != null && !fromSimNumber.isEmpty()) {
            cleanMessage = cleanMessage.replace(fromSimNumber, "").trim();
        }

        // Check for CALL or ARMED status
        if (cleanMessage != null && cleanMessage.toLowerCase().contains("call incoming")) {
            alertLog.setStatus("CALL");
        } else if (cleanMessage != null && cleanMessage.toUpperCase().contains("ARMED")) {
            alertLog.setStatus("ARMED");
        } else {
            alertLog.setStatus("PENDING");
        }

        // Find system by ATM code if provided, otherwise by SIM number
        Optional<AlarmSystem> machineOpt = Optional.empty();
        if (atmCode != null && !atmCode.trim().isEmpty()) {
            machineOpt = alarmSystemRepository.findBySystemCode(atmCode.trim());
        }
        if (machineOpt.isEmpty() && fromSimNumber != null && !fromSimNumber.isEmpty()) {
            String rawSim = fromSimNumber.trim();
            // Try exact
            machineOpt = alarmSystemRepository.findBySimNumber(rawSim);

            // Try digits-only
            if (machineOpt.isEmpty()) {
                String digits = rawSim.replaceAll("\\D+", "");
                if (!digits.isEmpty()) {
                    machineOpt = alarmSystemRepository.findBySimNumber(digits);
                }
            }

            // Try converting +94... to 0... (common Sri Lanka format)
            if (machineOpt.isEmpty()) {
                String digits = rawSim.replaceAll("\\D+", "");
                if (digits.startsWith("94") && digits.length() > 2) {
                    String local = "0" + digits.substring(2);
                    machineOpt = alarmSystemRepository.findBySimNumber(local);
                }
            }
        }

        if (atmCode != null && !atmCode.trim().isEmpty() && machineOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid ATM Code: " + atmCode);
        }

        if (machineOpt.isPresent()) {
            alertLog.setAlarmSystem(machineOpt.get());
        } else {
            alertLog.setAlarmSystem(null);
        }

        // Extract zone numbers
        String zoneNumbers = extractZoneNumbers(smsContent);
        
        if (!zoneNumbers.isEmpty()) {
            alertLog.setZoneNumbers(zoneNumbers);
            String firstZone = zoneNumbers.split(",")[0].trim();
            try {
                alertLog.setZoneNumber(Integer.parseInt(firstZone));
            } catch (NumberFormatException e) {
                alertLog.setZoneNumber(0);
            }
            
            // ===== NEW: Get zone names from DB =====
            if (machineOpt.isPresent()) {
                String zoneNames = getZoneNames(machineOpt.get().getId(), zoneNumbers);
                alertLog.setZoneNames(zoneNames);
            }
        } else {
            alertLog.setZoneNumber(0);
            alertLog.setZoneNumbers("00");
            alertLog.setZoneNames("No Zone");
        }

        alertLog.setAlertType(cleanMessage);
        alertLog.setRawMessage(smsContent);
        
        return alertLogRepository.save(alertLog);
    }

    // ===== NEW: Get zone names from zone numbers =====
    private String getZoneNames(Long systemId, String zoneNumbers) {
        if (zoneNumbers == null || zoneNumbers.isEmpty() || zoneNumbers.equals("00")) {
            return "No Zone";
        }
        
        String[] zoneArray = zoneNumbers.split(",");
        List<String> zoneNames = new ArrayList<>();
        
        for (String zoneStr : zoneArray) {
            try {
                int zoneNum = Integer.parseInt(zoneStr.trim());
                Optional<AlarmZone> zoneOpt = alarmZoneRepository.findByAlarmSystemIdAndZoneNumber(systemId, zoneNum);
                if (zoneOpt.isPresent()) {
                    zoneNames.add(zoneOpt.get().getZoneName());
                } else {
                    zoneNames.add("Zone " + zoneStr.trim());
                }
            } catch (NumberFormatException e) {
                zoneNames.add("Zone " + zoneStr.trim());
            }
        }
        
        return String.join(", ", zoneNames);
    }

    // Extract zone numbers from SMS
    private String extractZoneNumbers(String smsContent) {
        List<String> zones = new ArrayList<>();
        
        if (smsContent == null || smsContent.isEmpty()) {
            return "";
        }
        
        Pattern pattern1 = Pattern.compile("Zone:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher1 = pattern1.matcher(smsContent);
        while (matcher1.find()) {
            zones.add(matcher1.group(1));
        }
        
        Pattern pattern2 = Pattern.compile("ZONE\\s*(\\d+)\\s+ALARM!", Pattern.CASE_INSENSITIVE);
        Matcher matcher2 = pattern2.matcher(smsContent);
        while (matcher2.find()) {
            zones.add(matcher2.group(1));
        }
        
        Pattern pattern3 = Pattern.compile("ZONE\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher3 = pattern3.matcher(smsContent);
        while (matcher3.find()) {
            String zone = matcher3.group(1);
            if (!zones.contains(zone)) {
                zones.add(zone);
            }
        }
        
        List<String> uniqueZones = zones.stream().distinct().collect(Collectors.toList());
        return uniqueZones.isEmpty() ? "" : String.join(",", uniqueZones);
    }

    // Get all alerts (filtered by user) - WITH ZONE NAMES
    public List<AlertLog> getAllAlerts(String username) {
        List<AlertLog> alerts;
        
        if (username != null && !username.trim().isEmpty()) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent() && "USER".equalsIgnoreCase(userOpt.get().getRole())) {
                List<UserSystem> userSystems = userSystemRepository.findAllByUserId(userOpt.get().getId());
                List<Long> systemIds = userSystems.stream().map(UserSystem::getSystemId).collect(Collectors.toList());
                if (systemIds.isEmpty()) {
                    return new ArrayList<>();
                }
                alerts = alertLogRepository.findAllByAlarmSystemIdInOrderByReceivedAtDesc(systemIds);
            } else {
                alerts = alertLogRepository.findAllByOrderByReceivedAtDesc();
            }
        } else {
            alerts = alertLogRepository.findAllByOrderByReceivedAtDesc();
        }
        
        // ===== NEW: Populate zone names for each alert =====
        for (AlertLog alert : alerts) {
            if (alert.getAlarmSystem() != null && alert.getZoneNumbers() != null && !alert.getZoneNumbers().isEmpty()) {
                String zoneNames = getZoneNames(alert.getAlarmSystem().getId(), alert.getZoneNumbers());
                alert.setZoneNames(zoneNames);
            } else {
                alert.setZoneNames("No Zone");
            }
        }
        
        return alerts;
    }

    // Resolve an alert
    @Transactional
    public AlertLog resolveAlert(Long alertId, String resolvedBy, String clientIp, String description) {
        Optional<AlertLog> alertOpt = alertLogRepository.findById(alertId);
        if (alertOpt.isEmpty()) {
            throw new RuntimeException("Alert not found with ID: " + alertId);
        }

        AlertLog alert = alertOpt.get();
        
        if (!"PENDING".equals(alert.getStatus())) {
            throw new RuntimeException("Only PENDING alerts can be resolved. Current status: " + alert.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(alert.getReceivedAt(), now);
        
        alert.setStatus("RESOLVED");
        alert.setResolvedAt(now);
        alert.setResolvedBy(resolvedBy);
        alert.setPendingDurationSeconds(duration.getSeconds());
        alert.setResolvedFromIp(clientIp);
        
        if (description != null && !description.trim().isEmpty()) {
            alert.setResolutionDescription(description.trim());
        }

        AlertLog savedAlert = alertLogRepository.save(alert);
        
        // Populate zone names
        if (savedAlert.getAlarmSystem() != null && savedAlert.getZoneNumbers() != null) {
            savedAlert.setZoneNames(getZoneNames(savedAlert.getAlarmSystem().getId(), savedAlert.getZoneNumbers()));
        }
        
        return savedAlert;
    }

    // Get alert with details
    public AlertLog getAlertWithDetails(Long alertId) {
        AlertLog alert = alertLogRepository.findByIdWithSystem(alertId);
        if (alert != null && alert.getAlarmSystem() != null && alert.getZoneNumbers() != null) {
            alert.setZoneNames(getZoneNames(alert.getAlarmSystem().getId(), alert.getZoneNumbers()));
        }
        return alert;
    }

    // Register heartbeat from device (by atmCode or sim number)
    public void registerHeartbeat(String atmCode, String simNumber) {
        Optional<AlarmSystem> machineOpt = Optional.empty();
        if (atmCode != null && !atmCode.trim().isEmpty()) {
            machineOpt = alarmSystemRepository.findBySystemCode(atmCode.trim());
        }
        if (machineOpt.isEmpty() && simNumber != null && !simNumber.trim().isEmpty()) {
            String rawSim = simNumber.trim();
            machineOpt = alarmSystemRepository.findBySimNumber(rawSim);
            if (machineOpt.isEmpty()) {
                String digits = rawSim.replaceAll("\\D+", "");
                if (!digits.isEmpty()) machineOpt = alarmSystemRepository.findBySimNumber(digits);
                if (machineOpt.isEmpty() && digits.startsWith("94") && digits.length() > 2) {
                    String local = "0" + digits.substring(2);
                    machineOpt = alarmSystemRepository.findBySimNumber(local);
                }
            }
        }

        if (machineOpt.isPresent()) {
            AlarmSystem sys = machineOpt.get();
            sys.setLastStatusChangedAt(java.time.LocalDateTime.now());
            alarmSystemRepository.save(sys);
        } else {
            // if atmCode provided but no match, throw
            if (atmCode != null && !atmCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid ATM Code: " + atmCode);
            }
        }
    }

    public long getPendingCount() {
        return alertLogRepository.countByStatus("PENDING");
    }

    public long getResolvedCount() {
        return alertLogRepository.countResolved();
    }

    public List<AlertLog> getPendingAlerts() {
        List<AlertLog> alerts = alertLogRepository.findAllByOrderByReceivedAtDesc()
            .stream()
            .filter(a -> "PENDING".equals(a.getStatus()))
            .collect(Collectors.toList());
        
        // Populate zone names
        for (AlertLog alert : alerts) {
            if (alert.getAlarmSystem() != null && alert.getZoneNumbers() != null) {
                alert.setZoneNames(getZoneNames(alert.getAlarmSystem().getId(), alert.getZoneNumbers()));
            }
        }
        
        return alerts;
    }

    public List<AlertLog> getAlertsByStatus(String status) {
        List<AlertLog> alerts = alertLogRepository.findAllByOrderByReceivedAtDesc()
            .stream()
            .filter(a -> status.equalsIgnoreCase(a.getStatus()))
            .collect(Collectors.toList());
        
        // Populate zone names
        for (AlertLog alert : alerts) {
            if (alert.getAlarmSystem() != null && alert.getZoneNumbers() != null) {
                alert.setZoneNames(getZoneNames(alert.getAlarmSystem().getId(), alert.getZoneNumbers()));
            }
        }
        
        return alerts;
    }
}
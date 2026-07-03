package com.security.alarm.service;

import com.security.alarm.entity.AlertLog;
import com.security.alarm.entity.AlarmSystem;
import com.security.alarm.repository.AlertLogRepository;
import com.security.alarm.repository.AlarmSystemRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
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

    public AlertService(AlertLogRepository alertLogRepository, AlarmSystemRepository alarmSystemRepository) {
        this.alertLogRepository = alertLogRepository;
        this.alarmSystemRepository = alarmSystemRepository;
    }

    // 1. මැෂින් එකෙන් එන SMS එක Process කරලා Save කරන ක්‍රියාවලිය
    public AlertLog processIncomingSMS(String fromSimNumber, String smsContent) {
        AlertLog alertLog = new AlertLog();
        alertLog.setReceivedAt(LocalDateTime.now());
        alertLog.setStatus("PENDING");

        // SMS එකෙන් SIM number එක remove කරලා clean message එක ගන්න
        String cleanMessage = smsContent;
        
        // SIM number එක message එකේ තියෙනවනම් ඒක remove කරන්න
        if (fromSimNumber != null && !fromSimNumber.isEmpty()) {
            cleanMessage = cleanMessage.replace(fromSimNumber, "").trim();
        }

        // SIM නම්බර් එකෙන් අදාළ Alarm System එක හොයනවා
        Optional<AlarmSystem> machineOpt = alarmSystemRepository.findBySimNumber(fromSimNumber);
        
        if (machineOpt.isPresent()) {
            alertLog.setAlarmSystem(machineOpt.get());
        } else {
            // සිම් එක සිස්ටම් එකේ නැත්නම් තාවකාලිකව null තබයි
            alertLog.setAlarmSystem(null);
        }

        // ZONE numbers ඔක්කොම extract කරන්න
        String zoneNumbers = extractZoneNumbers(smsContent);
        
        if (!zoneNumbers.isEmpty()) {
            // Multiple zones තියෙනවනම් comma separated විදියට ගන්න
            alertLog.setZoneNumbers(zoneNumbers);
            // පළවෙනි zone එක primary zone එක විදියට set කරන්න
            String firstZone = zoneNumbers.split(",")[0].trim();
            try {
                alertLog.setZoneNumber(Integer.parseInt(firstZone));
            } catch (NumberFormatException e) {
                alertLog.setZoneNumber(0);
            }
        } else {
            alertLog.setZoneNumber(0);
            alertLog.setZoneNumbers("00");
        }

        // Clean message එක alert type එකට save කරන්න
        alertLog.setAlertType(cleanMessage);
        // Original SMS එකත් save කරන්න (payload එක විදියට)
        alertLog.setRawMessage(smsContent);
        
        return alertLogRepository.save(alertLog);
    }

    // SMS එකෙන් සියලුම zone numbers extract කරන method එක
    private String extractZoneNumbers(String smsContent) {
        List<String> zones = new ArrayList<>();
        
        if (smsContent == null || smsContent.isEmpty()) {
            return "";
        }
        
        // Pattern 1: "Zone: XX" format එක (Z8B manual එකට අනුව)
        Pattern pattern1 = Pattern.compile("Zone:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher1 = pattern1.matcher(smsContent);
        while (matcher1.find()) {
            zones.add(matcher1.group(1));
        }
        
        // Pattern 2: "ZONE XX ALARM!" format එක
        Pattern pattern2 = Pattern.compile("ZONE\\s*(\\d+)\\s+ALARM!", Pattern.CASE_INSENSITIVE);
        Matcher matcher2 = pattern2.matcher(smsContent);
        while (matcher2.find()) {
            zones.add(matcher2.group(1));
        }
        
        // Pattern 3: "ZONE XX" format එක (ALARM! නැතිව)
        Pattern pattern3 = Pattern.compile("ZONE\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher3 = pattern3.matcher(smsContent);
        while (matcher3.find()) {
            String zone = matcher3.group(1);
            // Duplicate නැතිව add කරන්න
            if (!zones.contains(zone)) {
                zones.add(zone);
            }
        }
        
        // Unique zones extract කරන්න
        List<String> uniqueZones = zones.stream().distinct().collect(Collectors.toList());
        
        // Zone numbers join කරන්න (උදා: "01,08,03")
        return uniqueZones.isEmpty() ? "" : String.join(",", uniqueZones);
    }

    // 2. සියලුම අනතුරු ඇඟවීම් ලැයිස්තුව ලබාගැනීම (Dashboard එක සඳහා)
    public List<AlertLog> getAllAlerts() {
        return alertLogRepository.findAllByOrderByReceivedAtDesc();
    }
}

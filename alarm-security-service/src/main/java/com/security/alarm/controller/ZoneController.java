package com.security.alarm.controller;

import com.security.alarm.entity.AlarmSystem;
import com.security.alarm.entity.AlarmZone;
import com.security.alarm.repository.AlarmSystemRepository;
import com.security.alarm.repository.AlarmZoneRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/zones")
@CrossOrigin(origins = "*")
public class ZoneController {

    private final AlarmZoneRepository alarmZoneRepository;
    private final AlarmSystemRepository alarmSystemRepository;

    public ZoneController(AlarmZoneRepository alarmZoneRepository,
                          AlarmSystemRepository alarmSystemRepository) {
        this.alarmZoneRepository = alarmZoneRepository;
        this.alarmSystemRepository = alarmSystemRepository;
    }

    // ===== GET ALL ZONES FOR A SYSTEM =====
    @GetMapping("/system/{systemId}")
    public ResponseEntity<?> getZonesBySystem(@PathVariable Long systemId) {
        Optional<AlarmSystem> systemOpt = alarmSystemRepository.findById(systemId);
        if (systemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<AlarmZone> zones = alarmZoneRepository.findByAlarmSystemIdOrderByZoneNumberAsc(systemId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("systemId", systemId);
        response.put("systemCode", systemOpt.get().getSystemCode());
        response.put("zones", zones);
        
        return ResponseEntity.ok(response);
    }

    // ===== UPDATE ZONE =====
    @PutMapping("/{zoneId}")
    public ResponseEntity<?> updateZone(@PathVariable Long zoneId, @RequestBody Map<String, Object> payload) {
        Optional<AlarmZone> zoneOpt = alarmZoneRepository.findById(zoneId);
        if (zoneOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AlarmZone zone = zoneOpt.get();
        
        // Update zone name
        if (payload.containsKey("zoneName")) {
            String newName = (String) payload.get("zoneName");
            if (newName != null && !newName.trim().isEmpty()) {
                zone.setZoneName(newName.trim());
            }
        }
        
        // Update zone type
        if (payload.containsKey("zoneType")) {
            Integer newType = (Integer) payload.get("zoneType");
            if (newType != null && newType >= 0 && newType <= 8) {
                zone.setZoneType(newType);
            }
        }
        
        // Update active status
        if (payload.containsKey("isActive")) {
            Boolean isActive = (Boolean) payload.get("isActive");
            if (isActive != null) {
                zone.setIsActive(isActive);
            }
        }
        
        // Update description
        if (payload.containsKey("description")) {
            String description = (String) payload.get("description");
            zone.setDescription(description);
        }
        
        zone.setUpdatedAt(LocalDateTime.now());
        
        AlarmZone updated = alarmZoneRepository.save(zone);
        return ResponseEntity.ok(updated);
    }

    // ===== BULK UPDATE ZONES =====
    @PutMapping("/system/{systemId}/bulk")
    public ResponseEntity<?> bulkUpdateZones(@PathVariable Long systemId, 
                                              @RequestBody List<Map<String, Object>> zoneUpdates) {
        Optional<AlarmSystem> systemOpt = alarmSystemRepository.findById(systemId);
        if (systemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<AlarmZone> existingZones = alarmZoneRepository.findByAlarmSystemIdOrderByZoneNumberAsc(systemId);
        
        for (Map<String, Object> update : zoneUpdates) {
            Integer zoneNumber = (Integer) update.get("zoneNumber");
            String zoneName = (String) update.get("zoneName");
            Integer zoneType = (Integer) update.get("zoneType");
            Boolean isActive = (Boolean) update.get("isActive");
            
            if (zoneNumber != null) {
                Optional<AlarmZone> zoneOpt = existingZones.stream()
                    .filter(z -> z.getZoneNumber().equals(zoneNumber))
                    .findFirst();
                
                if (zoneOpt.isPresent()) {
                    AlarmZone zone = zoneOpt.get();
                    if (zoneName != null && !zoneName.trim().isEmpty()) {
                        zone.setZoneName(zoneName.trim());
                    }
                    if (zoneType != null && zoneType >= 0 && zoneType <= 8) {
                        zone.setZoneType(zoneType);
                    }
                    if (isActive != null) {
                        zone.setIsActive(isActive);
                    }
                    zone.setUpdatedAt(LocalDateTime.now());
                }
            }
        }
        
        alarmZoneRepository.saveAll(existingZones);
        return ResponseEntity.ok(existingZones);
    }

    // ===== RESET ZONES TO DEFAULT =====
    @PostMapping("/system/{systemId}/reset")
    public ResponseEntity<?> resetZonesToDefault(@PathVariable Long systemId) {
        Optional<AlarmSystem> systemOpt = alarmSystemRepository.findById(systemId);
        if (systemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Delete existing zones
        alarmZoneRepository.deleteBySystemId(systemId);
        
        // Create default zones (1-24)
        String[] defaultZoneNames = {
            "Main Entrance", "Cash Counter", "Lobby", "Server Room",
            "Back Office", "Vault Room", "Emergency Exit", "Parking Area",
            "Store Room", "Rest Room", "Corridor 1", "Corridor 2",
            "Main Hall", "Conference Room", "Security Room", "Generator Room",
            "Wired Zone 1", "Wired Zone 2", "Wired Zone 3", "Wired Zone 4",
            "Wired Zone 5", "Wired Zone 6", "Wired Zone 7", "Wired Zone 8"
        };

        for (int i = 0; i < 24; i++) {
            AlarmZone zone = new AlarmZone();
            zone.setAlarmSystem(systemOpt.get());
            zone.setZoneNumber(i + 1);
            zone.setZoneName(defaultZoneNames[i]);
            zone.setZoneType(1); // PERIMETER
            zone.setIsActive(true);
            zone.setDescription("Default zone " + (i + 1));
            alarmZoneRepository.save(zone);
        }

        List<AlarmZone> zones = alarmZoneRepository.findByAlarmSystemIdOrderByZoneNumberAsc(systemId);
        return ResponseEntity.ok(zones);
    }

    // ===== GET ZONE TYPES =====
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, Object>>> getZoneTypes() {
        List<Map<String, Object>> types = List.of(
            Map.of("value", 0, "label", "OFF", "description", "Zone disabled"),
            Map.of("value", 1, "label", "PERIMETER", "description", "Perimeter zone"),
            Map.of("value", 2, "label", "DELAY", "description", "Delay zone"),
            Map.of("value", 3, "label", "AWAY", "description", "Away/Part alarm"),
            Map.of("value", 4, "label", "24HR", "description", "24 hours zone"),
            Map.of("value", 5, "label", "MUTE", "description", "Mute zone"),
            Map.of("value", 6, "label", "EXIT", "description", "Exit button zone"),
            Map.of("value", 7, "label", "BELL", "description", "Door bell zone"),
            Map.of("value", 8, "label", "SOS", "description", "SOS zone")
        );
        return ResponseEntity.ok(types);
    }
}
package com.atm.atm.controller;

import com.atm.atm.dto.AtmCreateRequest;
import com.atm.atm.dto.AtmUpdateRequest;
import com.atm.atm.entity.AtmStation;
import com.atm.atm.enums.AtmStatus;
import com.atm.atm.service.AtmManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/atm")
@RequiredArgsConstructor
public class AtmManagementController {
    
    private final AtmManagementService atmManagementService;
    
    @PostMapping("/create")
    public ResponseEntity<AtmStation> createAtm(@Valid @RequestBody AtmCreateRequest request,
                                              HttpServletRequest httpRequest) {
        log.info("Create ATM request for phone: {} from IP: {}", request.getPhoneNumber(), getClientIp(httpRequest));
        
        // In a real implementation, extract user ID from JWT token
        Long createdBy = 1L; // Placeholder
        
        AtmStation created = atmManagementService.createAtm(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AtmStation> updateAtm(@PathVariable Long id,
                                                @Valid @RequestBody AtmUpdateRequest request,
                                                HttpServletRequest httpRequest) {
        log.info("Update ATM request for ID: {} from IP: {}", id, getClientIp(httpRequest));
        
        // In a real implementation, extract user ID from JWT token
        Long updatedBy = 1L; // Placeholder
        
        AtmStation updated = atmManagementService.updateAtm(id, request, updatedBy);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAtm(@PathVariable Long id) {
        log.info("Delete ATM request for ID: {}", id);
        
        atmManagementService.deleteAtm(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "ATM deleted successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AtmStation> getAtmById(@PathVariable Long id) {
        AtmStation atm = atmManagementService.getAtmById(id);
        return ResponseEntity.ok(atm);
    }
    
    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<AtmStation> getAtmByPhoneNumber(@PathVariable String phoneNumber) {
        Optional<AtmStation> atm = atmManagementService.getAtmByPhoneNumber(phoneNumber);
        return atm.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<AtmStation> getAtmByQrCode(@PathVariable String qrCode) {
        Optional<AtmStation> atm = atmManagementService.getAtmByQrCode(qrCode);
        return atm.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<AtmStation>> getAllAtms() {
        List<AtmStation> atms = atmManagementService.getAllAtms();
        return ResponseEntity.ok(atms);
    }
    
    @GetMapping
    public ResponseEntity<Page<AtmStation>> getAllAtmsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AtmStation> atms = atmManagementService.getAllAtms(pageable);
        return ResponseEntity.ok(atms);
    }
    
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<AtmStation>> getAtmsByBank(@PathVariable Long bankId) {
        List<AtmStation> atms = atmManagementService.getAtmsByBank(bankId);
        return ResponseEntity.ok(atms);
    }
    
    @GetMapping("/bank/{bankId}/paginated")
    public ResponseEntity<Page<AtmStation>> getAtmsByBankPaginated(
            @PathVariable Long bankId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AtmStation> atms = atmManagementService.getAtmsByBank(bankId, pageable);
        return ResponseEntity.ok(atms);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AtmStation>> getAtmsByStatus(@PathVariable AtmStatus status) {
        List<AtmStation> atms = atmManagementService.getAtmsByStatus(status);
        return ResponseEntity.ok(atms);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<AtmStation>> getActiveAtms() {
        List<AtmStation> atms = atmManagementService.getActiveAtms();
        return ResponseEntity.ok(atms);
    }
    
    @GetMapping("/bank/{bankId}/active")
    public ResponseEntity<List<AtmStation>> getActiveAtmsByBank(@PathVariable Long bankId) {
        List<AtmStation> atms = atmManagementService.getActiveAtmsByBank(bankId);
        return ResponseEntity.ok(atms);
    }
    
    @GetMapping("/offline")
    public ResponseEntity<List<AtmStation>> getOfflineAtms() {
        List<AtmStation> atms = atmManagementService.getOfflineAtms();
        return ResponseEntity.ok(atms);
    }
    
    @GetMapping("/maintenance-needed")
    public ResponseEntity<List<AtmStation>> getAtmsNeedingMaintenance() {
        List<AtmStation> atms = atmManagementService.getAtmsNeedingMaintenance();
        return ResponseEntity.ok(atms);
    }
    
    @PostMapping("/heartbeat")
    public ResponseEntity<AtmStation> updateHeartbeat(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Phone number is required");
            return ResponseEntity.badRequest().build();
        }
        
        AtmStation atm = atmManagementService.updateHeartbeat(phoneNumber);
        return ResponseEntity.ok(atm);
    }
    
    @PostMapping("/heartbeat/{id}")
    public ResponseEntity<AtmStation> updateHeartbeatById(@PathVariable Long id) {
        AtmStation atm = atmManagementService.updateHeartbeatById(id);
        return ResponseEntity.ok(atm);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<AtmStation>> searchAtms(@RequestParam String term) {
        List<AtmStation> atms = atmManagementService.searchAtms(term);
        return ResponseEntity.ok(atms);
    }
    
    @GetMapping("/within-bounds")
    public ResponseEntity<List<AtmStation>> getAtmsWithinBounds(
            @RequestParam Double minLat,
            @RequestParam Double maxLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLng) {
        
        List<AtmStation> atms = atmManagementService.getAtmsWithinBounds(minLat, maxLat, minLng, maxLng);
        return ResponseEntity.ok(atms);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAtmStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("totalActiveAtms", atmManagementService.countActiveAtms());
        statistics.put("totalOnlineAtms", atmManagementService.countOnlineAtms());
        statistics.put("totalOfflineAtms", atmManagementService.countAtmsByStatus(AtmStatus.OFFLINE));
        statistics.put("totalMaintenanceAtms", atmManagementService.countAtmsByStatus(AtmStatus.MAINTENANCE));
        statistics.put("atmsNeedingMaintenance", atmManagementService.getAtmsNeedingMaintenance().size());
        
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/bank/{bankId}/statistics")
    public ResponseEntity<Map<String, Object>> getAtmStatisticsByBank(@PathVariable Long bankId) {
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("totalAtms", atmManagementService.countAtmsByBank(bankId));
        statistics.put("activeAtms", atmManagementService.getActiveAtmsByBank(bankId).size());
        
        return ResponseEntity.ok(statistics);
    }
    
    @PostMapping("/schedule-maintenance/{id}")
    public ResponseEntity<AtmStation> scheduleMaintenance(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String dateString = request.get("maintenanceDate");
        LocalDateTime maintenanceDate = LocalDateTime.parse(dateString);
        
        AtmStation atm = atmManagementService.scheduleMaintenance(id, maintenanceDate);
        return ResponseEntity.ok(atm);
    }
    
    @PostMapping("/complete-maintenance/{id}")
    public ResponseEntity<AtmStation> completeMaintenance(@PathVariable Long id) {
        AtmStation atm = atmManagementService.completeMaintenance(id);
        return ResponseEntity.ok(atm);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "atm-management-service");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

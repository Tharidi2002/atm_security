package com.atmsecurity.station.controller;

import com.atmsecurity.common.dto.ApiResponse;
import com.atmsecurity.common.security.UserPrincipal;
import com.atmsecurity.station.entity.AtmStation;
import com.atmsecurity.station.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AtmStation>>> getStations(@AuthenticationPrincipal UserPrincipal principal) {
        if ("ADMIN".equals(principal.getRole())) {
            return ResponseEntity.ok(ApiResponse.ok(stationService.getAllStations()));
        } else {
            Long bankId = principal.getBankId();
            if (bankId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User does not have a bank assigned"));
            }
            return ResponseEntity.ok(ApiResponse.ok(stationService.getStationsByBank(bankId)));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AtmStation>> getStationById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        AtmStation station = stationService.getStationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));

        if (!"ADMIN".equals(principal.getRole()) && !station.getBankId().equals(principal.getBankId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access Denied: Station belongs to another bank"));
        }

        return ResponseEntity.ok(ApiResponse.ok(station));
    }

    @GetMapping("/phone-hash/{phoneHash}")
    public ResponseEntity<AtmStation> getStationByPhoneHash(@PathVariable String phoneHash) {
        // Public/internal endpoint for alert correlation
        return stationService.getStationByPhoneHash(phoneHash)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AtmStation>> createStation(@RequestBody AtmStation station, @AuthenticationPrincipal UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getRole()) && !"BANK_MANAGER".equals(principal.getRole())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access Denied: Insufficient permissions"));
        }

        if (!"ADMIN".equals(principal.getRole())) {
            station.setBankId(principal.getBankId());
        }

        AtmStation created = stationService.createStation(station, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Station created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AtmStation>> updateStation(@PathVariable Long id, @RequestBody AtmStation station, @AuthenticationPrincipal UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getRole()) && !"BANK_MANAGER".equals(principal.getRole())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access Denied: Insufficient permissions"));
        }

        AtmStation existing = stationService.getStationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));

        if (!"ADMIN".equals(principal.getRole()) && !existing.getBankId().equals(principal.getBankId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access Denied: Station belongs to another bank"));
        }

        if (!"ADMIN".equals(principal.getRole())) {
            station.setBankId(principal.getBankId());
        }

        AtmStation updated = stationService.updateStation(id, station, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Station updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStation(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getRole()) && !"BANK_MANAGER".equals(principal.getRole())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access Denied: Insufficient permissions"));
        }

        AtmStation existing = stationService.getStationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));

        if (!"ADMIN".equals(principal.getRole()) && !existing.getBankId().equals(principal.getBankId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access Denied: Station belongs to another bank"));
        }

        stationService.deleteStation(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Station deleted successfully", null));
    }
}

package com.atmsecurity.station.service;

import com.atmsecurity.common.crypto.AesEncryptionService;
import com.atmsecurity.station.entity.AtmStation;
import com.atmsecurity.station.entity.StationAuditLog;
import com.atmsecurity.station.repository.AtmStationRepository;
import com.atmsecurity.station.repository.StationAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StationService {

    private final AtmStationRepository stationRepository;
    private final StationAuditLogRepository auditLogRepository;
    private final AesEncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    public List<AtmStation> getAllStations() {
        List<AtmStation> stations = stationRepository.findAll();
        // Decrypt phone numbers for display in dashboard
        stations.forEach(this::decryptStationPhone);
        return stations;
    }

    public List<AtmStation> getStationsByBank(Long bankId) {
        List<AtmStation> stations = stationRepository.findByBankId(bankId);
        stations.forEach(this::decryptStationPhone);
        return stations;
    }

    public Optional<AtmStation> getStationById(Long id) {
        return stationRepository.findById(id).map(this::decryptStationPhone);
    }

    public Optional<AtmStation> getStationByPhoneHash(String phoneHash) {
        return stationRepository.findByPhoneNumberHash(phoneHash).map(this::decryptStationPhone);
    }

    @Transactional
    public AtmStation createStation(AtmStation station, Long userId) {
        String plainPhone = station.getPhoneNumberEnc();
        station.setPhoneNumberEnc(encryptionService.encrypt(plainPhone));
        station.setPhoneNumberHash(encryptionService.hashSha256(plainPhone));
        station.setCreatedBy(userId);

        AtmStation saved = stationRepository.save(station);

        logAudit(saved.getId(), "CREATE", userId, null, toJson(saved));
        
        // Return decrypted phone for client response
        saved.setPhoneNumberEnc(plainPhone);
        return saved;
    }

    @Transactional
    public AtmStation updateStation(Long id, AtmStation updated, Long userId) {
        AtmStation existing = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found with ID: " + id));

        String oldStateJson = toJson(existing);

        existing.setStationCode(updated.getStationCode());
        existing.setLocationName(updated.getLocationName());
        existing.setLocationAddress(updated.getLocationAddress());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setBankId(updated.getBankId());
        existing.setActive(updated.isActive());

        String plainPhone = updated.getPhoneNumberEnc();
        if (plainPhone != null && !plainPhone.isBlank()) {
            existing.setPhoneNumberEnc(encryptionService.encrypt(plainPhone));
            existing.setPhoneNumberHash(encryptionService.hashSha256(plainPhone));
        }

        AtmStation saved = stationRepository.save(existing);
        logAudit(saved.getId(), "UPDATE", userId, oldStateJson, toJson(saved));

        decryptStationPhone(saved);
        return saved;
    }

    @Transactional
    public void deleteStation(Long id, Long userId) {
        AtmStation existing = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found with ID: " + id));

        String oldStateJson = toJson(existing);
        stationRepository.delete(existing);

        logAudit(id, "DELETE", userId, oldStateJson, null);
    }

    private AtmStation decryptStationPhone(AtmStation station) {
        if (station != null && station.getPhoneNumberEnc() != null) {
            try {
                station.setPhoneNumberEnc(encryptionService.decrypt(station.getPhoneNumberEnc()));
            } catch (Exception e) {
                // Keep encrypted if decryption fails (fallback)
            }
        }
        return station;
    }

    private void logAudit(Long stationId, String action, Long userId, String oldValues, String newValues) {
        StationAuditLog log = StationAuditLog.builder()
                .stationId(stationId)
                .action(action)
                .performedBy(userId)
                .oldValues(oldValues)
                .newValues(newValues)
                .build();
        auditLogRepository.save(log);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}

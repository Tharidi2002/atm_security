package com.atm.atm.service;

import com.atm.atm.dto.AtmCreateRequest;
import com.atm.atm.dto.AtmUpdateRequest;
import com.atm.atm.entity.AtmStation;
import com.atm.atm.enums.AtmStatus;
import com.atm.atm.enums.ZoneType;
import com.atm.atm.repository.AtmStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtmManagementService {
    
    private final AtmStationRepository atmStationRepository;
    
    @Transactional
    public AtmStation createAtm(AtmCreateRequest request, Long createdBy) {
        // Check if phone number already exists
        if (atmStationRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("ATM with phone number " + request.getPhoneNumber() + " already exists");
        }
        
        AtmStation atm = AtmStation.builder()
                .phoneNumber(request.getPhoneNumber())
                .bankId(request.getBankId())
                .locationName(request.getLocationName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .city(request.getCity())
                .district(request.getDistrict())
                .zoneType(request.getZoneType() != null ? request.getZoneType() : ZoneType.GENERAL)
                .firmwareVersion(request.getFirmwareVersion())
                .status(request.getStatus() != null ? request.getStatus() : AtmStatus.ACTIVE)
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();
        
        AtmStation savedAtm = atmStationRepository.save(atm);
        log.info("ATM created successfully: {} with ID: {}", savedAtm.getPhoneNumber(), savedAtm.getId());
        
        return savedAtm;
    }
    
    @Transactional
    public AtmStation updateAtm(Long id, AtmUpdateRequest request, Long updatedBy) {
        AtmStation existingAtm = getAtmById(id);
        
        // Check if phone number is being changed and if it already exists
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(existingAtm.getPhoneNumber())) {
            if (atmStationRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("ATM with phone number " + request.getPhoneNumber() + " already exists");
            }
            existingAtm.setPhoneNumber(request.getPhoneNumber());
        }
        
        // Update fields if provided
        if (request.getBankId() != null) {
            existingAtm.setBankId(request.getBankId());
        }
        if (request.getLocationName() != null) {
            existingAtm.setLocationName(request.getLocationName());
        }
        if (request.getLatitude() != null) {
            existingAtm.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            existingAtm.setLongitude(request.getLongitude());
        }
        if (request.getAddress() != null) {
            existingAtm.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            existingAtm.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            existingAtm.setDistrict(request.getDistrict());
        }
        if (request.getZoneType() != null) {
            existingAtm.setZoneType(request.getZoneType());
        }
        if (request.getFirmwareVersion() != null) {
            existingAtm.setFirmwareVersion(request.getFirmwareVersion());
        }
        if (request.getStatus() != null) {
            existingAtm.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            existingAtm.setNotes(request.getNotes());
        }
        
        existingAtm.setUpdatedBy(updatedBy);
        
        AtmStation savedAtm = atmStationRepository.save(existingAtm);
        log.info("ATM updated successfully: {} with ID: {}", savedAtm.getPhoneNumber(), savedAtm.getId());
        
        return savedAtm;
    }
    
    @Transactional
    public void deleteAtm(Long id) {
        AtmStation atm = getAtmById(id);
        atm.setIsActive(false);
        atm.setStatus(AtmStatus.INACTIVE);
        
        atmStationRepository.save(atm);
        log.info("ATM deleted (soft delete) successfully: {} with ID: {}", atm.getPhoneNumber(), atm.getId());
    }
    
    public AtmStation getAtmById(Long id) {
        return atmStationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ATM not found with ID: " + id));
    }
    
    public Optional<AtmStation> getAtmByPhoneNumber(String phoneNumber) {
        return atmStationRepository.findByPhoneNumber(phoneNumber);
    }
    
    public Optional<AtmStation> getAtmByQrCode(String qrCode) {
        return atmStationRepository.findByQrCode(qrCode);
    }
    
    public List<AtmStation> getAllAtms() {
        return atmStationRepository.findAll();
    }
    
    public Page<AtmStation> getAllAtms(Pageable pageable) {
        return atmStationRepository.findAll(pageable);
    }
    
    public List<AtmStation> getAtmsByBank(Long bankId) {
        return atmStationRepository.findByBankId(bankId);
    }
    
    public Page<AtmStation> getAtmsByBank(Long bankId, Pageable pageable) {
        return atmStationRepository.findByBankId(bankId, pageable);
    }
    
    public List<AtmStation> getAtmsByStatus(AtmStatus status) {
        return atmStationRepository.findByStatus(status);
    }
    
    public List<AtmStation> getActiveAtms() {
        return atmStationRepository.findActiveAtms();
    }
    
    public List<AtmStation> getActiveAtmsByBank(Long bankId) {
        return atmStationRepository.findActiveAtmsByBank(bankId);
    }
    
    public List<AtmStation> getOfflineAtms() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        return atmStationRepository.findOfflineAtms(threshold);
    }
    
    public List<AtmStation> getAtmsNeedingMaintenance() {
        LocalDateTime threshold = LocalDateTime.now().plusDays(7);
        return atmStationRepository.findAtmsNeedingMaintenance(threshold);
    }
    
    @Transactional
    public AtmStation updateHeartbeat(String phoneNumber) {
        AtmStation atm = getAtmByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("ATM not found with phone number: " + phoneNumber));
        
        atm.updateHeartbeat();
        AtmStation savedAtm = atmStationRepository.save(atm);
        
        log.debug("Heartbeat updated for ATM: {}", phoneNumber);
        return savedAtm;
    }
    
    @Transactional
    public AtmStation updateHeartbeatById(Long id) {
        AtmStation atm = getAtmById(id);
        atm.updateHeartbeat();
        
        AtmStation savedAtm = atmStationRepository.save(atm);
        log.debug("Heartbeat updated for ATM ID: {}", id);
        
        return savedAtm;
    }
    
    public List<AtmStation> searchAtms(String searchTerm) {
        return atmStationRepository.searchAtms(searchTerm);
    }
    
    public List<AtmStation> getAtmsWithinBounds(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        return atmStationRepository.findAtmsWithinBounds(minLat, maxLat, minLng, maxLng);
    }
    
    public long countActiveAtms() {
        return atmStationRepository.countActiveAtms();
    }
    
    public long countOnlineAtms() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        return atmStationRepository.countOnlineAtms(threshold);
    }
    
    public long countAtmsByBank(Long bankId) {
        return atmStationRepository.countActiveAtmsByBank(bankId);
    }
    
    public long countAtmsByStatus(AtmStatus status) {
        return atmStationRepository.countByStatus(status);
    }
    
    @Transactional
    public AtmStation scheduleMaintenance(Long id, LocalDateTime maintenanceDate) {
        AtmStation atm = getAtmById(id);
        atm.setNextMaintenanceDue(maintenanceDate);
        
        AtmStation savedAtm = atmStationRepository.save(atm);
        log.info("Maintenance scheduled for ATM: {} on {}", atm.getPhoneNumber(), maintenanceDate);
        
        return savedAtm;
    }
    
    @Transactional
    public AtmStation completeMaintenance(Long id) {
        AtmStation atm = getAtmById(id);
        atm.setLastMaintenance(LocalDateTime.now());
        
        // Schedule next maintenance in 30 days
        atm.setNextMaintenanceDue(LocalDateTime.now().plusDays(30));
        
        AtmStation savedAtm = atmStationRepository.save(atm);
        log.info("Maintenance completed for ATM: {}", atm.getPhoneNumber());
        
        return savedAtm;
    }
}

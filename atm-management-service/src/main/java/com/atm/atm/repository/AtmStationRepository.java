package com.atm.atm.repository;

import com.atm.atm.entity.AtmStation;
import com.atm.atm.enums.AtmStatus;
import com.atm.atm.enums.ZoneType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AtmStationRepository extends JpaRepository<AtmStation, Long> {
    
    Optional<AtmStation> findByPhoneNumber(String phoneNumber);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    List<AtmStation> findByBankId(Long bankId);
    
    Page<AtmStation> findByBankId(Long bankId, Pageable pageable);
    
    List<AtmStation> findByStatus(AtmStatus status);
    
    Page<AtmStation> findByStatus(AtmStatus status, Pageable pageable);
    
    List<AtmStation> findByZoneType(ZoneType zoneType);
    
    List<AtmStation> findByCity(String city);
    
    List<AtmStation> findByDistrict(String district);
    
    @Query("SELECT a FROM AtmStation a WHERE a.isActive = true")
    List<AtmStation> findActiveAtms();
    
    @Query("SELECT a FROM AtmStation a WHERE a.isActive = true AND a.bankId = :bankId")
    List<AtmStation> findActiveAtmsByBank(@Param("bankId") Long bankId);
    
    @Query("SELECT a FROM AtmStation a WHERE a.lastHeartbeat < :threshold")
    List<AtmStation> findOfflineAtms(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT a FROM AtmStation a WHERE a.nextMaintenanceDue < :threshold")
    List<AtmStation> findAtmsNeedingMaintenance(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT COUNT(a) FROM AtmStation a WHERE a.bankId = :bankId AND a.isActive = true")
    long countActiveAtmsByBank(@Param("bankId") Long bankId);
    
    @Query("SELECT COUNT(a) FROM AtmStation a WHERE a.status = :status")
    long countByStatus(@Param("status") AtmStatus status);
    
    @Query("SELECT a FROM AtmStation a WHERE a.locationName LIKE %:search% OR a.address LIKE %:search% OR a.city LIKE %:search%")
    List<AtmStation> searchAtms(@Param("search") String search);
    
    @Query("SELECT a FROM AtmStation a WHERE a.latitude BETWEEN :minLat AND :maxLat AND a.longitude BETWEEN :minLng AND :maxLng")
    List<AtmStation> findAtmsWithinBounds(@Param("minLat") Double minLat, 
                                         @Param("maxLat") Double maxLat,
                                         @Param("minLng") Double minLng, 
                                         @Param("maxLng") Double maxLng);
    
    @Query("SELECT a FROM AtmStation a WHERE a.qrCode = :qrCode")
    Optional<AtmStation> findByQrCode(@Param("qrCode") String qrCode);
    
    @Query("SELECT COUNT(a) FROM AtmStation a WHERE a.isActive = true")
    long countActiveAtms();
    
    @Query("SELECT COUNT(a) FROM AtmStation a WHERE a.lastHeartbeat > :threshold")
    long countOnlineAtms(@Param("threshold") LocalDateTime threshold);
}

package com.atm.station.repository;

import com.atm.station.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByPhoneNumber(String phoneNumber);
    Boolean existsByPhoneNumber(String phoneNumber);
    
    List<Station> findByBankName(String bankName);
    List<Station> findByCity(String city);
    List<Station> findByIsActive(Boolean isActive);
    
    @Query("SELECT s FROM Station s WHERE s.bankName LIKE %:bankName% OR s.location LIKE %:location% OR s.city LIKE %:city%")
    List<Station> findByBankNameOrLocationOrCityContaining(@Param("bankName") String bankName, 
                                                           @Param("location") String location, 
                                                           @Param("city") String city);
    
    @Query("SELECT s FROM Station s WHERE s.phoneNumber = :phoneNumber AND s.isActive = true")
    Optional<Station> findActiveStationByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    List<Station> findByBankNameAndIsActive(String bankName, Boolean isActive);
}

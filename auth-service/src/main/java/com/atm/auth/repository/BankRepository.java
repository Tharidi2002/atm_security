package com.atm.auth.repository;

import com.atm.auth.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    
    Optional<Bank> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT b FROM Bank b WHERE b.isActive = true")
    List<Bank> findActiveBanks();
    
    @Query("SELECT b FROM Bank b WHERE b.isActive = true AND b.subscriptionTier = :tier")
    List<Bank> findActiveBanksBySubscriptionTier(@Param("tier") String tier);
    
    @Query("SELECT COUNT(b) FROM Bank b WHERE b.isActive = true")
    long countActiveBanks();
}

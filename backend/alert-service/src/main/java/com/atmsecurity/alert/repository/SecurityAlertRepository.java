package com.atmsecurity.alert.repository;

import com.atmsecurity.alert.entity.SecurityAlert;
import com.atmsecurity.alert.entity.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {
    List<SecurityAlert> findByBankId(Long bankId);
    List<SecurityAlert> findByBankIdAndSeverity(Long bankId, Severity severity);
    List<SecurityAlert> findBySeverity(Severity severity);
}

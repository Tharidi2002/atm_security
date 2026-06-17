package com.atmsecurity.alert.repository;

import com.atmsecurity.alert.entity.AlertAcknowledgement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertAcknowledgementRepository extends JpaRepository<AlertAcknowledgement, Long> {
    List<AlertAcknowledgement> findByAlertId(Long alertId);
}

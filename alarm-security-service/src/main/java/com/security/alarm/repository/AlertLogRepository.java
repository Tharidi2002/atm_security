package com.security.alarm.repository;

import com.security.alarm.entity.AlertLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {
    List<AlertLog> findAllByOrderByReceivedAtDesc();
    List<AlertLog> findAllByAlarmSystemIdInOrderByReceivedAtDesc(List<Long> systemIds);
}

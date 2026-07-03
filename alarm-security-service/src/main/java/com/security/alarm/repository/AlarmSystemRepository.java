package com.security.alarm.repository;

import com.security.alarm.entity.AlarmSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AlarmSystemRepository extends JpaRepository<AlarmSystem, Long> {
    Optional<AlarmSystem> findBySimNumber(String simNumber);
}

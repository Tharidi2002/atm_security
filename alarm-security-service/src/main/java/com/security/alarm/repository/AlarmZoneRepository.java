package com.security.alarm.repository;

import com.security.alarm.entity.AlarmZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmZoneRepository extends JpaRepository<AlarmZone, Long> {
}

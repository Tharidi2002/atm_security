-- Database creation if not exists
CREATE DATABASE IF NOT EXISTS alarm_security_db;
USE alarm_security_db;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS alarm_systems (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  system_code VARCHAR(255) NOT NULL UNIQUE,
  location VARCHAR(255) NOT NULL,
  description VARCHAR(1024),
  sim_number VARCHAR(255) NOT NULL,
  status VARCHAR(255) DEFAULT 'ACTIVE',
  last_status_changed_at DATETIME,
  panel_sim_number VARCHAR(255),
  panel_password VARCHAR(255) DEFAULT '8888',
  disarm_command VARCHAR(255) DEFAULT '8888#2A',
  arm_command VARCHAR(255) DEFAULT '8888#1A',
  siren_status VARCHAR(255) DEFAULT 'OFF'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS alarm_zones (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  system_id BIGINT NOT NULL,
  zone_number INT NOT NULL,
  zone_name VARCHAR(100) NOT NULL,
  zone_type INT NOT NULL,
  is_active TINYINT(1) DEFAULT 1,
  description VARCHAR(255),
  zone_category VARCHAR(20),
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT fk_alarm_zones_system FOREIGN KEY (system_id)
    REFERENCES alarm_systems(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS alert_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  system_id BIGINT,
  zone_number INT,
  zone_numbers VARCHAR(255),
  alert_type VARCHAR(255) NOT NULL,
  raw_message TEXT,
  received_at DATETIME,
  status VARCHAR(20) DEFAULT 'PENDING',
  resolved_at DATETIME,
  resolved_by VARCHAR(255),
  pending_duration_seconds BIGINT,
  resolution_description TEXT,
  resolved_from_ip VARCHAR(255),
  CONSTRAINT fk_alert_logs_system FOREIGN KEY (system_id)
    REFERENCES alarm_systems(id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_systems (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  system_id BIGINT NOT NULL,
  CONSTRAINT fk_user_systems_user FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_user_systems_system FOREIGN KEY (system_id)
    REFERENCES alarm_systems(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

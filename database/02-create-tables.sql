-- ATM Security System - Create Tables
-- This script creates all necessary tables for the system
-- Note: Hibernate will auto-create these tables with ddl-auto=update
-- This script is for manual setup and reference

USE atm_security_system;

-- Banks table
CREATE TABLE IF NOT EXISTS banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    timezone VARCHAR(50) DEFAULT 'Asia/Colombo',
    subscription_tier VARCHAR(50) DEFAULT 'STANDARD',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_banks_active (is_active),
    INDEX idx_banks_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('SUPER_ADMIN', 'BANK_ADMIN', 'SECURITY_OFFICER') NOT NULL,
    bank_id BIGINT,
    language ENUM('EN', 'SI', 'TA') DEFAULT 'EN',
    mfa_secret VARCHAR(32),
    login_attempts INT DEFAULT 0,
    account_locked_until TIMESTAMP NULL,
    last_login TIMESTAMP NULL,
    password_changed_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_bank_id (bank_id),
    INDEX idx_users_active (is_active),
    INDEX idx_users_locked (account_locked_until),
    FOREIGN KEY (bank_id) REFERENCES banks(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ATM Stations table
CREATE TABLE IF NOT EXISTS atm_stations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    bank_id BIGINT NOT NULL,
    location_name VARCHAR(200) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    address VARCHAR(300),
    city VARCHAR(100),
    district VARCHAR(100),
    zone_type ENUM('CASH_COUNTER', 'PAWNING_AREA', 'GENERAL', 'HIGH_SECURITY', 'LOBBY', 'DRIVE_THROUGH') DEFAULT 'GENERAL',
    firmware_version VARCHAR(20) DEFAULT '1.0.0',
    last_heartbeat TIMESTAMP NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'OFFLINE', 'ERROR') DEFAULT 'ACTIVE',
    installation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_maintenance TIMESTAMP NULL,
    next_maintenance_due TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    notes VARCHAR(500),
    qr_code VARCHAR(100),
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_atm_phone (phone_number),
    INDEX idx_atm_bank (bank_id),
    INDEX idx_atm_status (status),
    INDEX idx_atm_heartbeat (last_heartbeat),
    INDEX idx_atm_active (is_active),
    INDEX idx_atm_location (location_name),
    FOREIGN KEY (bank_id) REFERENCES banks(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Alerts table
CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    atm_id BIGINT NOT NULL,
    severity ENUM('CRITICAL', 'WARNING', 'INFO') NOT NULL,
    category ENUM('UNAUTHORIZED_ACCESS', 'FIRE', 'TAMPERING', 'DOOR_OPEN', 'POWER_FAILURE', 'ARMING_DISARMING', 'NETWORK_ISSUE', 'LOW_BATTERY', 'MAINTENANCE', 'SYSTEM_ERROR', 'UNKNOWN') DEFAULT 'UNKNOWN',
    message VARCHAR(1000) NOT NULL,
    raw_sms TEXT,
    status ENUM('NEW', 'ACKNOWLEDGED', 'INVESTIGATING', 'RESOLVED', 'FALSE_ALARM', 'ESCALATED') DEFAULT 'NEW',
    assigned_to BIGINT,
    acknowledged_at TIMESTAMP NULL,
    acknowledged_by BIGINT,
    investigation_started_at TIMESTAMP NULL,
    investigation_started_by BIGINT,
    resolved_at TIMESTAMP NULL,
    resolved_by BIGINT,
    resolution_notes VARCHAR(1000),
    sla_deadline TIMESTAMP NULL,
    escalated_at TIMESTAMP NULL,
    escalated_to BIGINT,
    escalation_reason VARCHAR(500),
    is_false_alarm BOOLEAN DEFAULT FALSE,
    false_alarm_reason VARCHAR(500),
    metadata JSON,
    source VARCHAR(50) DEFAULT 'SMS',
    external_id VARCHAR(100),
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_alert_atm (atm_id),
    INDEX idx_alert_severity (severity),
    INDEX idx_alert_status (status),
    INDEX idx_alert_category (category),
    INDEX idx_alert_created (created_at),
    INDEX idx_alert_assigned (assigned_to),
    INDEX idx_alert_sla (sla_deadline),
    FOREIGN KEY (atm_id) REFERENCES atm_stations(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (acknowledged_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (investigation_started_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (escalated_to) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    alert_id BIGINT,
    type ENUM('ALERT', 'ASSIGNMENT', 'ESCALATION', 'ACKNOWLEDGE', 'SYSTEM') DEFAULT 'ALERT',
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_via ENUM('WEBSOCKET', 'EMAIL', 'BOTH') DEFAULT 'WEBSOCKET',
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_alert (alert_id),
    INDEX idx_notification_read (is_read),
    INDEX idx_notification_type (type),
    INDEX idx_notification_created (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- WebSocket Sessions table (for tracking active connections)
CREATE TABLE IF NOT EXISTS websocket_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    INDEX idx_ws_session_user (user_id),
    INDEX idx_ws_session_id (session_id),
    INDEX idx_ws_session_activity (last_activity),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit Logs table (for security and compliance)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_timestamp (timestamp),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Reports table
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    generated_by BIGINT NOT NULL,
    report_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'CUSTOM') NOT NULL,
    title VARCHAR(200) NOT NULL,
    parameters JSON,
    file_path VARCHAR(500),
    file_size BIGINT,
    file_format ENUM('PDF', 'EXCEL', 'CSV') DEFAULT 'PDF',
    scheduled BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_report_type (report_type),
    INDEX idx_report_generated (generated_by),
    INDEX idx_report_scheduled (scheduled),
    INDEX idx_report_created (created_at),
    FOREIGN KEY (generated_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SMS Logs table (for tracking incoming SMS messages)
CREATE TABLE IF NOT EXISTS sms_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_number VARCHAR(20) NOT NULL,
    to_number VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    raw_message TEXT,
    status ENUM('RECEIVED', 'PROCESSED', 'FAILED', 'DUPLICATE') DEFAULT 'RECEIVED',
    sms_id VARCHAR(100),
    service_provider VARCHAR(50),
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    alert_id BIGINT,
    error_message TEXT,
    INDEX idx_sms_from (from_number),
    INDEX idx_sms_status (status),
    INDEX idx_sms_received (received_at),
    INDEX idx_sms_alert (alert_id),
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Show all created tables
SHOW TABLES;

-- Display table structures
DESCRIBE banks;
DESCRIBE users;
DESCRIBE atm_stations;
DESCRIBE alerts;
DESCRIBE notifications;
DESCRIBE websocket_sessions;
DESCRIBE audit_logs;
DESCRIBE reports;
DESCRIBE sms_logs;

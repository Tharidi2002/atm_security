-- ATM Security System Complete Database Schema
-- MySQL 8.0+ Compatible

-- Create databases for each microservice
CREATE DATABASE IF NOT EXISTS atm_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS atm_stations CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS atm_alerts CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS atm_reports CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS atm_notifications CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use main database for shared tables
USE atm_auth;

-- Banks table for multi-tenant support
CREATE TABLE banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    address TEXT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    timezone VARCHAR(50) DEFAULT 'Asia/Colombo',
    subscription_tier ENUM('BASIC', 'PREMIUM', 'ENTERPRISE') DEFAULT 'BASIC',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_banks_name (name),
    INDEX idx_banks_status (status)
);

-- Users table with comprehensive authentication features
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role ENUM('SUPER_ADMIN', 'BANK_ADMIN', 'SECURITY_OFFICER') NOT NULL,
    bank_id BIGINT,
    language VARCHAR(10) DEFAULT 'en',
    mfa_secret VARCHAR(32),
    mfa_enabled BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    last_login TIMESTAMP NULL,
    login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED', 'PENDING_APPROVAL') DEFAULT 'PENDING_APPROVAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (bank_id) REFERENCES banks(id) ON DELETE SET NULL,
    INDEX idx_users_email (email),
    INDEX idx_users_username (username),
    INDEX idx_users_role (role),
    INDEX idx_users_bank_role (bank_id, role),
    INDEX idx_users_status (status)
);

-- Password reset tokens
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reset_tokens_token (token),
    INDEX idx_reset_tokens_user (user_id)
);

-- Refresh tokens for JWT
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    device_info TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_user (user_id)
);

-- User sessions for tracking concurrent logins
CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    device_info TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_sessions_user (user_id),
    INDEX idx_sessions_session (session_id)
);

-- Switch to stations database
USE atm_stations;

-- ATM stations table
CREATE TABLE atm_stations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_id VARCHAR(50) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    bank_id BIGINT NOT NULL,
    location_name VARCHAR(200) NOT NULL,
    address TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    zone_type ENUM('CASH_COUNTER', 'PAWNING_AREA', 'GENERAL_ZONE', 'VAULT') DEFAULT 'GENERAL_ZONE',
    firmware_version VARCHAR(50),
    last_heartbeat TIMESTAMP NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'OFFLINE') DEFAULT 'ACTIVE',
    qr_code_url VARCHAR(500),
    installation_date DATE,
    last_maintenance_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_atm_stations_station_id (station_id),
    INDEX idx_atm_stations_phone (phone_number),
    INDEX idx_atm_stations_bank (bank_id),
    INDEX idx_atm_stations_status (status),
    INDEX idx_atm_stations_location (latitude, longitude),
    INDEX idx_atm_stations_heartbeat (last_heartbeat)
);

-- ATM zones for detailed monitoring
CREATE TABLE atm_zones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    atm_id BIGINT NOT NULL,
    zone_name VARCHAR(100) NOT NULL,
    zone_type ENUM('ENTRANCE', 'CASH_DISPENSER', 'KEYPAD', 'CAMERA', 'MOTION_SENSOR', 'DOOR_SENSOR') NOT NULL,
    sensor_id VARCHAR(50),
    last_checked TIMESTAMP NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'MALFUNCTIONING') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (atm_id) REFERENCES atm_stations(id) ON DELETE CASCADE,
    INDEX idx_zones_atm (atm_id),
    INDEX idx_zones_type (zone_type),
    INDEX idx_zones_status (status)
);

-- ATM maintenance records
CREATE TABLE atm_maintenance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    atm_id BIGINT NOT NULL,
    maintenance_type ENUM('ROUTINE', 'EMERGENCY', 'UPGRADE', 'REPAIR') NOT NULL,
    description TEXT,
    technician_name VARCHAR(100),
    technician_contact VARCHAR(20),
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    status ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    cost DECIMAL(10, 2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (atm_id) REFERENCES atm_stations(id) ON DELETE CASCADE,
    INDEX idx_maintenance_atm (atm_id),
    INDEX idx_maintenance_status (status),
    INDEX idx_maintenance_dates (started_at, completed_at)
);

-- Switch to alerts database
USE atm_alerts;

-- Alerts table with comprehensive tracking
CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id VARCHAR(50) NOT NULL UNIQUE,
    atm_id BIGINT NOT NULL,
    severity ENUM('CRITICAL', 'WARNING', 'INFO') NOT NULL,
    category ENUM('UNAUTHORIZED_ACCESS', 'FIRE', 'TAMPERING', 'DOOR_OPEN', 'POWER_FAILURE', 'ARMING', 'DISARMING', 'MAINTENANCE', 'NETWORK_DOWN') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    raw_sms TEXT,
    status ENUM('NEW', 'ACKNOWLEDGED', 'ASSIGNED', 'INVESTIGATING', 'RESOLVED', 'FALSE_ALARM') DEFAULT 'NEW',
    priority_score INT DEFAULT 50,
    assigned_to BIGINT,
    acknowledged_by BIGINT,
    acknowledged_at TIMESTAMP NULL,
    resolved_by BIGINT,
    resolved_at TIMESTAMP NULL,
    resolution_notes TEXT,
    sla_deadline TIMESTAMP NULL,
    escalation_level INT DEFAULT 0,
    is_duplicate BOOLEAN DEFAULT FALSE,
    original_alert_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_alerts_alert_id (alert_id),
    INDEX idx_alerts_atm (atm_id),
    INDEX idx_alerts_severity (severity),
    INDEX idx_alerts_status (status),
    INDEX idx_alerts_category (category),
    INDEX idx_alerts_assigned (assigned_to),
    INDEX idx_alerts_created (created_at),
    INDEX idx_alerts_sla (sla_deadline),
    INDEX idx_alerts_duplicate (is_duplicate, original_alert_id)
);

-- Alert deduplication tracking
CREATE TABLE alert_deduplication (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dedup_key VARCHAR(255) NOT NULL UNIQUE,
    atm_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    first_alert_id BIGINT NOT NULL,
    last_alert_id BIGINT NOT NULL,
    count INT DEFAULT 1,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dedup_key (dedup_key),
    INDEX idx_dedup_atm (atm_id),
    INDEX idx_dedup_window (window_start, window_end)
);

-- Alert timeline for tracking changes
CREATE TABLE alert_timeline (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    action ENUM('CREATED', 'ACKNOWLEDGED', 'ASSIGNED', 'ESCALATED', 'RESOLVED', 'UPDATED') NOT NULL,
    performed_by BIGINT,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    notes TEXT,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE,
    INDEX idx_timeline_alert (alert_id),
    INDEX idx_timeline_action (action),
    INDEX idx_timeline_created (created_at)
);

-- Switch to notifications database
USE atm_notifications;

-- Incidents table for comprehensive incident management
CREATE TABLE incidents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id VARCHAR(50) NOT NULL UNIQUE,
    alert_id BIGINT NOT NULL,
    assigned_team VARCHAR(100),
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    lifecycle_status ENUM('NEW', 'ACKNOWLEDGED', 'INVESTIGATING', 'RESOLVED', 'CLOSED') DEFAULT 'NEW',
    title VARCHAR(200) NOT NULL,
    description TEXT,
    resolution_notes TEXT,
    assigned_to BIGINT,
    resolved_by BIGINT,
    time_to_respond INT, -- in minutes
    time_to_resolve INT, -- in minutes
    auto_resolve_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE,
    INDEX idx_incidents_incident_id (incident_id),
    INDEX idx_incidents_alert (alert_id),
    INDEX idx_incidents_status (lifecycle_status),
    INDEX idx_incidents_priority (priority),
    INDEX idx_incidents_assigned (assigned_to),
    INDEX idx_incidents_created (created_at)
);

-- Incident notes for collaboration
CREATE TABLE incident_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    note TEXT NOT NULL,
    mentions JSON, -- @mentioned users
    attachments JSON, -- file paths or URLs
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notes_incident (incident_id),
    INDEX idx_notes_user (user_id),
    INDEX idx_notes_created (created_at)
);

-- Notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    alert_id BIGINT,
    incident_id BIGINT,
    type ENUM('ALERT', 'INCIDENT', 'SYSTEM', 'REMINDER') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_via ENUM('WEBSOCKET', 'EMAIL', 'SMS', 'PUSH') NOT NULL,
    sent_at TIMESTAMP NULL,
    read_at TIMESTAMP NULL,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE,
    FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_read (is_read),
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_created (created_at)
);

-- SMS logs for tracking SMS communications
CREATE TABLE sms_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_number VARCHAR(20) NOT NULL,
    to_number VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    direction ENUM('INCOMING', 'OUTGOING') NOT NULL,
    status ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCE') DEFAULT 'PENDING',
    twilio_sid VARCHAR(100),
    error_message TEXT,
    cost DECIMAL(10, 4),
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    INDEX idx_sms_from_to (from_number, to_number),
    INDEX idx_sms_status (status),
    INDEX idx_sms_direction (direction),
    INDEX idx_sms_received (received_at)
);

-- Switch to reports database
USE atm_reports;

-- Reports table
CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id VARCHAR(50) NOT NULL UNIQUE,
    generated_by BIGINT NOT NULL,
    report_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'CUSTOM', 'COMPLIANCE', 'AUDIT') NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    parameters JSON,
    file_path VARCHAR(500),
    file_format ENUM('PDF', 'EXCEL', 'CSV', 'JSON') NOT NULL,
    file_size BIGINT,
    scheduled BOOLEAN DEFAULT FALSE,
    schedule_cron VARCHAR(100),
    recipients JSON, -- email addresses
    status ENUM('GENERATING', 'COMPLETED', 'FAILED', 'EXPIRED') DEFAULT 'GENERATING',
    generated_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    download_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reports_report_id (report_id),
    INDEX idx_reports_type (report_type),
    INDEX idx_reports_generated_by (generated_by),
    INDEX idx_reports_status (status),
    INDEX idx_reports_created (created_at)
);

-- Report templates
CREATE TABLE report_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    template_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'CUSTOM') NOT NULL,
    parameters JSON, -- template parameters schema
    layout JSON, -- report layout configuration
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_templates_type (template_type),
    INDEX idx_templates_active (is_active)
);

-- Back to auth database for audit logs
USE atm_auth;

-- Comprehensive audit logs table
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_value JSON,
    new_value JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(255),
    success BOOLEAN DEFAULT TRUE,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_created (created_at),
    INDEX idx_audit_success (success)
);

-- System configuration table
CREATE TABLE system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    config_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_config_key (config_key)
);

-- Insert default system configuration
INSERT INTO system_config (config_key, config_value, config_type, description) VALUES
('max_login_attempts', '5', 'NUMBER', 'Maximum login attempts before account lockout'),
('lockout_duration_minutes', '15', 'NUMBER', 'Account lockout duration in minutes'),
('jwt_expiration_minutes', '15', 'NUMBER', 'JWT token expiration in minutes'),
('refresh_token_days', '7', 'NUMBER', 'Refresh token validity in days'),
('password_min_length', '8', 'NUMBER', 'Minimum password length'),
('session_timeout_minutes', '30', 'NUMBER', 'User session timeout in minutes'),
('alert_deduplication_window_minutes', '5', 'NUMBER', 'Alert deduplication window in minutes'),
('critical_alert_sla_minutes', '2', 'NUMBER', 'SLA for critical alert acknowledgment'),
('max_concurrent_sessions', '3', 'NUMBER', 'Maximum concurrent sessions per user'),
('password_history_count', '5', 'NUMBER', 'Number of previous passwords to remember');

-- Insert default banks
INSERT INTO banks (name, address, contact_phone, contact_email, subscription_tier) VALUES
('Bank of Ceylon', 'No. 01, BOC Headquarters, Bristol Street, Colombo 01', '+94112223333', 'info@boc.lk', 'ENTERPRISE'),
('Commercial Bank', 'No. 21, Commercial Bank, Sir Chittampalam A. Gardiner Mawatha, Colombo 02', '+94112345678', 'info@combank.lk', 'ENTERPRISE'),
('Hatton National Bank', 'No. 24, HNB Towers, Union Place, Colombo 02', '+94112456789', 'info@hnb.lk', 'PREMIUM'),
('Sampath Bank', 'No. 90, Galle Road, Colombo 03', '+94112567890', 'info@sampath.lk', 'PREMIUM'),
('National Development Bank', 'No. 48, NDB Towers, Station Road, Colombo 04', '+94112678901', 'info@ndb.lk', 'BASIC');

-- Create super admin user (password: Admin@123)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, language, email_verified, status) VALUES
('admin', 'admin@atmsystem.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uO9G', 'System', 'Administrator', 'SUPER_ADMIN', 'en', TRUE, 'ACTIVE');

-- Create sample bank admins
INSERT INTO users (username, email, password_hash, first_name, last_name, role, bank_id, language, email_verified, status) VALUES
('boc_admin', 'admin@boc.lk', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uO9G', 'BOC', 'Administrator', 'BANK_ADMIN', 1, 'en', TRUE, 'ACTIVE'),
('combank_admin', 'admin@combank.lk', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uO9G', 'Commercial', 'Administrator', 'BANK_ADMIN', 2, 'en', TRUE, 'ACTIVE'),
('hnb_admin', 'admin@hnb.lk', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uO9G', 'HNB', 'Administrator', 'BANK_ADMIN', 3, 'en', TRUE, 'ACTIVE');

-- Create sample security officers
INSERT INTO users (username, email, password_hash, first_name, last_name, role, bank_id, language, email_verified, status) VALUES
('boc_officer1', 'officer1@boc.lk', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uO9G', 'Rohan', 'Perera', 'SECURITY_OFFICER', 1, 'en', TRUE, 'ACTIVE'),
('boc_officer2', 'officer2@boc.lk', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uO9G', 'Nimal', 'Silva', 'SECURITY_OFFICER', 1, 'si', TRUE, 'ACTIVE'),
('combank_officer1', 'officer1@combank.lk', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uO9G', 'Kamal', 'Fernando', 'SECURITY_OFFICER', 2, 'en', TRUE, 'ACTIVE');

-- Create sample ATM stations
USE atm_stations;
INSERT INTO atm_stations (station_id, phone_number, bank_id, location_name, address, latitude, longitude, zone_type, status) VALUES
('BOC001', '+94771234567', 1, 'BOC Head Office', 'No. 01, BOC Headquarters, Bristol Street, Colombo 01', 6.9319, 79.8478, 'CASH_COUNTER', 'ACTIVE'),
('BOC002', '+94771234568', 1, 'BOC Kollupitiya', 'No. 455, Galle Road, Kollupitiya, Colombo 03', 6.9144, 79.8496, 'GENERAL_ZONE', 'ACTIVE'),
('BOC003', '+94771234569', 1, 'BOC Nugegoda', 'No. 78, High Level Road, Nugegoda', 6.8511, 79.8898, 'PAWNING_AREA', 'ACTIVE'),
('COM001', '+94772345678', 2, 'Commercial Bank Colpetty', 'No. 234, Galle Road, Colombo 03', 6.9100, 79.8500, 'GENERAL_ZONE', 'ACTIVE'),
('COM002', '+94772345679', 2, 'Commercial Bank Dehiwala', 'No. 155, Galle Road, Dehiwala', 6.8346, 79.8637, 'CASH_COUNTER', 'ACTIVE'),
('HNB001', '+94773456789', 3, 'HNB Mount Lavinia', 'No. 89, Galle Road, Mount Lavinia', 6.8256, 79.8643, 'GENERAL_ZONE', 'ACTIVE'),
('HNB002', '+94773456790', 3, 'HNB Maharagama', 'No. 234, High Level Road, Maharagama', 6.8535, 79.8965, 'VAULT', 'ACTIVE');

-- Create sample ATM zones
INSERT INTO atm_zones (atm_id, zone_name, zone_type, sensor_id, status) VALUES
(1, 'Main Entrance', 'ENTRANCE', 'ENT001', 'ACTIVE'),
(1, 'Cash Dispenser', 'CASH_DISPENSER', 'CD001', 'ACTIVE'),
(1, 'Keypad Area', 'KEYPAD', 'KP001', 'ACTIVE'),
(2, 'Main Entrance', 'ENTRANCE', 'ENT002', 'ACTIVE'),
(2, 'Cash Dispenser', 'CASH_DISPENSER', 'CD002', 'ACTIVE'),
(3, 'Pawning Entrance', 'ENTRANCE', 'ENT003', 'ACTIVE'),
(3, 'Pawning Counter', 'MOTION_SENSOR', 'MS001', 'ACTIVE');

-- Create sample alerts
USE atm_alerts;
INSERT INTO alerts (alert_id, atm_id, severity, category, title, message, status, priority_score) VALUES
('ALT001', 1, 'CRITICAL', 'UNAUTHORIZED_ACCESS', 'Unauthorized Access Detected', 'Motion detected at BOC Head Office ATM during off-hours', 'NEW', 90),
('ALT002', 2, 'WARNING', 'DOOR_OPEN', 'ATM Door Open', 'Front door opened at BOC Kollupitiya branch', 'ACKNOWLEDGED', 60),
('ALT003', 3, 'INFO', 'ARMING', 'System Armed', 'ATM at Nugegoda branch armed successfully', 'RESOLVED', 30),
('ALT004', 4, 'CRITICAL', 'TAMPERING', 'ATM Tampering Detected', 'Vandalism detected at Commercial Bank Colpetty', 'NEW', 85),
('ALT005', 5, 'WARNING', 'POWER_FAILURE', 'Power Outage', 'Power failure detected at Commercial Bank Dehiwala', 'ASSIGNED', 65);

-- Create sample incidents
USE atm_notifications;
INSERT INTO incidents (incident_id, alert_id, priority, lifecycle_status, title, description, assigned_to) VALUES
('INC001', 1, 'CRITICAL', 'INVESTIGATING', 'Unauthorized Access Investigation', 'Security team dispatched to investigate unauthorized access at BOC Head Office', 6),
('INC002', 4, 'HIGH', 'NEW', 'ATM Vandalism Incident', 'ATM damaged at Commercial Bank Colpetty branch', 7);

-- Create sample incident notes
INSERT INTO incident_notes (incident_id, user_id, note) VALUES
(1, 6, 'Security team has been dispatched. Expected arrival in 15 minutes.'),
(1, 6, 'CCTV footage being reviewed. Suspicious individual identified.'),
(2, 8, 'Police notified. Incident report filed.');

-- Create sample notifications
INSERT INTO notifications (user_id, alert_id, incident_id, type, title, message, is_read, sent_via) VALUES
(6, 1, 1, 'ALERT', 'Critical Alert: Unauthorized Access', 'Unauthorized access detected at BOC Head Office ATM', FALSE, 'WEBSOCKET'),
(7, 1, 1, 'ALERT', 'Critical Alert: Unauthorized Access', 'Unauthorized access detected at BOC Head Office ATM', FALSE, 'EMAIL'),
(8, 4, 2, 'INCIDENT', 'New Incident Created', 'ATM vandalism incident created for Commercial Bank Colpetty', TRUE, 'WEBSOCKET');

-- Create sample report templates
USE atm_reports;
INSERT INTO report_templates (name, description, template_type, is_default) VALUES
('Daily Security Report', 'Daily summary of all security alerts and incidents', 'DAILY', TRUE),
('Weekly Alert Analysis', 'Weekly analysis of alert trends and patterns', 'WEEKLY', TRUE),
('Monthly Compliance Report', 'Monthly compliance report for banking regulations', 'MONTHLY', TRUE),
('Incident Response Report', 'Detailed incident response time analysis', 'CUSTOM', FALSE);

-- Create sample reports
INSERT INTO reports (report_id, generated_by, report_type, title, description, file_format, status, generated_at) VALUES
('RPT001', 1, 'DAILY', 'Daily Security Report - 2024-01-15', 'Daily summary of security alerts and incidents', 'PDF', 'COMPLETED', '2024-01-15 08:00:00'),
('RPT002', 1, 'WEEKLY', 'Weekly Alert Analysis - Week 2 2024', 'Weekly analysis of alert trends', 'EXCEL', 'COMPLETED', '2024-01-15 09:00:00'),
('RPT003', 2, 'CUSTOM', 'BOC ATM Security Report', 'Custom report for BOC ATM security status', 'PDF', 'COMPLETED', '2024-01-14 14:30:00');

-- Create sample audit logs
USE atm_auth;
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, ip_address, success) VALUES
(1, 'LOGIN', 'USER', 1, '127.0.0.1', TRUE),
(1, 'CREATE_ALERT', 'ALERT', 1, '127.0.0.1', TRUE),
(6, 'ACKNOWLEDGE_ALERT', 'ALERT', 1, '192.168.1.100', TRUE),
(7, 'ASSIGN_INCIDENT', 'INCIDENT', 1, '192.168.1.101', TRUE),
(8, 'CREATE_REPORT', 'REPORT', 1, '127.0.0.1', TRUE);

-- Grant necessary permissions for the services
CREATE USER IF NOT EXISTS 'atm_auth_user'@'localhost' IDENTIFIED BY 'atm_auth_2024!';
CREATE USER IF NOT EXISTS 'atm_station_user'@'localhost' IDENTIFIED BY 'atm_station_2024!';
CREATE USER IF NOT EXISTS 'atm_alert_user'@'localhost' IDENTIFIED BY 'atm_alert_2024!';
CREATE USER IF NOT EXISTS 'atm_report_user'@'localhost' IDENTIFIED BY 'atm_report_2024!';
CREATE USER IF NOT EXISTS 'atm_notification_user'@'localhost' IDENTIFIED BY 'atm_notification_2024!';

-- Grant permissions
GRANT ALL PRIVILEGES ON atm_auth.* TO 'atm_auth_user'@'localhost';
GRANT ALL PRIVILEGES ON atm_stations.* TO 'atm_station_user'@'localhost';
GRANT ALL PRIVILEGES ON atm_alerts.* TO 'atm_alert_user'@'localhost';
GRANT ALL PRIVILEGES ON atm_reports.* TO 'atm_report_user'@'localhost';
GRANT ALL PRIVILEGES ON atm_notifications.* TO 'atm_notification_user'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;

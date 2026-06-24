-- Database එක create කරන්න (නැත්නම්)
CREATE DATABASE IF NOT EXISTS atm
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;


USE atm;


-- ========================================
-- 1. BANKS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS banks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 2. ATM_MACHINES TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS atm_machines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    atm_code VARCHAR(50) UNIQUE NOT NULL,
    atm_name VARCHAR(100),
    location VARCHAR(255) NOT NULL,
    sim_number VARCHAR(20) UNIQUE NOT NULL,
    gsm_signal INT DEFAULT 0,
    battery_level INT DEFAULT 100,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_heartbeat TIMESTAMP NULL,
    firmware_version VARCHAR(20) DEFAULT '1.0.0',
    installation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    bank_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (bank_id) REFERENCES banks(id) ON DELETE SET NULL,
    INDEX idx_sim_number (sim_number),
    INDEX idx_bank_id (bank_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 3. ATM_ZONES TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS atm_zones (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    atm_id BIGINT NOT NULL,
    zone_number INT NOT NULL,
    zone_name VARCHAR(100) NOT NULL,
    zone_type VARCHAR(50) DEFAULT 'SECURITY',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (atm_id) REFERENCES atm_machines(id) ON DELETE CASCADE,
    UNIQUE KEY unique_atm_zone (atm_id, zone_number),
    INDEX idx_atm_id (atm_id),
    INDEX idx_zone_number (zone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 4. ALERT_LOGS TABLE (Main Alerts Table)
-- ========================================
CREATE TABLE IF NOT EXISTS alert_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    atm_id BIGINT,
    zone_number INT DEFAULT 0,
    zone_name VARCHAR(100),
    alert_type VARCHAR(50) NOT NULL DEFAULT 'UNKNOWN',
    raw_message TEXT,
    confidence_score DOUBLE DEFAULT 0.0,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    acknowledged_at TIMESTAMP NULL,
    resolved_at TIMESTAMP NULL,
    acknowledged_by VARCHAR(50),
    resolved_by VARCHAR(50),
    resolution_notes TEXT,
    severity INT DEFAULT 1,
    is_escalated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (atm_id) REFERENCES atm_machines(id) ON DELETE SET NULL,
    INDEX idx_atm_id (atm_id),
    INDEX idx_status (status),
    INDEX idx_received_at (received_at),
    INDEX idx_severity (severity),
    INDEX idx_alert_type (alert_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 5. USERS TABLE (For Authentication)
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'BANK_USER',
    bank_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (bank_id) REFERENCES banks(id) ON DELETE SET NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_bank_id (bank_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 6. ACTIVITY_LOGS TABLE (Audit Trail)
-- ========================================
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_performed_at (performed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 7. SMS_GATEWAY_LOGS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS sms_gateway_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    to_number VARCHAR(20) NOT NULL,
    from_number VARCHAR(20) NOT NULL,
    message TEXT,
    direction VARCHAR(10) DEFAULT 'INBOUND',
    status VARCHAR(20) DEFAULT 'RECEIVED',
    sms_provider VARCHAR(50),
    provider_message_id VARCHAR(100),
    sent_at TIMESTAMP NULL,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT,
    INDEX idx_to_number (to_number),
    INDEX idx_from_number (from_number),
    INDEX idx_status (status),
    INDEX idx_received_at (received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 8. REPORTS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_type VARCHAR(50) NOT NULL,
    report_name VARCHAR(100) NOT NULL,
    generated_by VARCHAR(50),
    file_path VARCHAR(500),
    file_format VARCHAR(20) DEFAULT 'PDF',
    parameters JSON,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_date DATE,
    end_date DATE,
    INDEX idx_report_type (report_type),
    INDEX idx_generated_at (generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;




-- ========================================


USE atm;

-- 1. Sample Banks
INSERT INTO banks (name, contact_email, contact_phone, address) VALUES 
('National Bank', 'info@nationalbank.com', '+94771111111', 'Colombo 01'),
('Commercial Bank', 'info@combank.com', '+94772222222', 'Colombo 02'),
('Sampath Bank', 'info@sampath.lk', '+94773333333', 'Colombo 03');


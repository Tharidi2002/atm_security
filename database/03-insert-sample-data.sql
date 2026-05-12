-- ATM Security System - Sample Data Insertion
-- This script creates sample data for testing the system

USE atm_security_system;

-- Insert sample banks
INSERT INTO banks (name, address, contact_phone, contact_email, timezone) VALUES
('Commercial Bank of Ceylon', 'No. 21, Bristol Street, Colombo 01', '+94112474471', 'info@combank.lk', 'Asia/Colombo'),
('Bank of Ceylon', 'No. 01, BOC Square, Colombo 01', '+94112474472', 'info@boc.lk', 'Asia/Colombo'),
('Hatton National Bank', 'No. 24, Union Place, Colombo 02', '+94112474473', 'info@hnb.lk', 'Asia/Colombo'),
('Sampath Bank', 'No. 95, Sir James Peiris Mawatha, Colombo 02', '+94112474474', 'info@sampath.lk', 'Asia/Colombo'),
('Nations Trust Bank', 'No. 501, Galle Road, Colombo 06', '+94112474475', 'info@ntb.lk', 'Asia/Colombo');

-- Insert sample users
INSERT INTO users (username, email, password_hash, role, bank_id, language, is_active, is_email_verified) VALUES
('admin', 'admin@atmsecurity.com', '$2a$12$LQv3c1yqBWVHxkd0LHAO4a.2rDjGd1hG3fA8d5m8vK8xH9f2eU6sG', 'SUPER_ADMIN', NULL, 'EN', TRUE, TRUE),
('bankadmin1', 'admin@combank.lk', '$2a$12$LQv3c1yqBWVHxkd0LHAO4a.2rDjGd1hG3fA8d5m8vK8xH9f2eU6sG', 'BANK_ADMIN', 1, 'EN', TRUE, TRUE),
('security1', 'security@combank.lk', '$2a$12$LQv3c1yqBWVHxkd0LHAO4a.2rDjGd1hG3fA8d5m8vK8xH9f2eU6sG', 'SECURITY_OFFICER', 1, 'SI', TRUE, TRUE),
('bankadmin2', 'admin@boc.lk', '$2a$12$LQv3c1yqBWVHxkd0LHAO4a.2rDjGd1hG3fA8d5m8vK8xH9f2eU6sG', 'BANK_ADMIN', 2, 'EN', TRUE, TRUE),
('security2', 'security@boc.lk', '$2a$12$LQv3c1yqBWVHxkd0LHAO4a.2rDjGd1hG3fA8d5m8vK8xH9f2eU6sG', 'SECURITY_OFFICER', 2, 'TA', TRUE, TRUE);

-- Insert sample ATM stations
INSERT INTO atm_stations (phone_number, bank_id, location_name, latitude, longitude, address, city, district, zone_type, firmware_version, status, created_by) VALUES
('+94771234567', 1, 'Commercial Bank - Colombo Fort', 6.9319, 79.8478, 'No. 45, Main Street, Colombo 01', 'Colombo', 'Colombo', 'CASH_COUNTER', '1.2.0', 'ACTIVE', 2),
('+94771234568', 1, 'Commercial Bank - Kandy', 7.2906, 80.6337, 'No. 12, Dalada Vidiya, Kandy', 'Kandy', 'Kandy', 'GENERAL', '1.2.0', 'ACTIVE', 2),
('+94771234569', 1, 'Commercial Bank - Galle', 6.0535, 80.2200, 'No. 89, Galle Road, Galle', 'Galle', 'Galle', 'PAWNING_AREA', '1.1.5', 'ACTIVE', 2),
('+94771234570', 2, 'Bank of Ceylon - Jaffna', 9.6615, 80.0255, 'No. 156, KKS Road, Jaffna', 'Jaffna', 'Jaffna', 'HIGH_SECURITY', '1.2.0', 'ACTIVE', 4),
('+94771234571', 2, 'Bank of Ceylon - Matara', 5.9549, 80.5550, 'No. 23, Matara Road, Matara', 'Matara', 'Matara', 'GENERAL', '1.1.5', 'ACTIVE', 4),
('+94771234572', 3, 'HNB - Negombo', 7.2083, 79.8357, 'No. 78, Main Street, Negombo', 'Negombo', 'Gampaha', 'CASH_COUNTER', '1.2.0', 'MAINTENANCE', 4),
('+94771234573', 3, 'HNB - Anuradhapura', 8.3114, 80.4037, 'No. 45, New Town, Anuradhapura', 'Anuradhapura', 'Anuradhapura', 'GENERAL', '1.1.5', 'ACTIVE', 4),
('+94771234574', 4, 'Sampath Bank - Kurunegala', 7.4818, 80.3617, 'No. 156, Kandy Road, Kurunegala', 'Kurunegala', 'Kurunegala', 'LOBBY', '1.2.0', 'ACTIVE', 4),
('+94771234575', 4, 'Sampath Bank - Ratnapura', 6.7056, 80.3846, 'No. 89, Main Street, Ratnapura', 'Ratnapura', 'Ratnapura', 'GENERAL', '1.1.5', 'ACTIVE', 4),
('+94771234576', 5, 'NTB - Batticaloa', 7.7102, 81.7065, 'No. 34, Main Street, Batticaloa', 'Batticaloa', 'Batticaloa', 'CASH_COUNTER', '1.2.0', 'ACTIVE', 4);

-- Insert sample alerts
INSERT INTO alerts (atm_id, severity, category, message, raw_sms, status, assigned_to, acknowledged_at, acknowledged_by, created_by) VALUES
(1, 'CRITICAL', 'UNAUTHORIZED_ACCESS', 'Unauthorized access detected at ATM-001. Multiple failed PIN attempts recorded.', 'Unauthorized access detected at ATM-001. Multiple failed PIN attempts recorded.', 'ACKNOWLEDGED', 3, NOW() - INTERVAL 30 MINUTE, 3, 1),
(2, 'WARNING', 'DOOR_OPEN', 'ATM-002 door opened outside business hours. Location: Kandy Branch.', 'ATM-002 door opened outside business hours. Location: Kandy Branch.', 'NEW', 3, NULL, NULL, 1),
(3, 'CRITICAL', 'FIRE', 'Fire alarm triggered at ATM-003. Smoke detected in cash dispenser area.', 'Fire alarm triggered at ATM-003. Smoke detected in cash dispenser area.', 'INVESTIGATING', 3, NOW() - INTERVAL 15 MINUTE, 3, 1),
(4, 'INFO', 'ARMING_DISARMING', 'ATM-004 system armed successfully for night mode.', 'ATM-004 system armed successfully for night mode.', 'RESOLVED', 4, NOW() - INTERVAL 2 HOUR, 4, 1),
(5, 'WARNING', 'POWER_FAILURE', 'Power failure detected at ATM-005. UPS battery activated.', 'Power failure detected at ATM-005. UPS battery activated.', 'NEW', 4, NULL, NULL, 1),
(6, 'CRITICAL', 'TAMPERING', 'Tampering detected at ATM-006. Physical access attempt blocked.', 'Tampering detected at ATM-006. Physical access attempt blocked.', 'NEW', 4, NULL, NULL, 1),
(7, 'INFO', 'MAINTENANCE', 'Scheduled maintenance completed at ATM-007. Firmware updated to v1.2.0.', 'Scheduled maintenance completed at ATM-007. Firmware updated to v1.2.0.', 'RESOLVED', 4, NOW() - INTERVAL 1 DAY, 4, 1),
(8, 'WARNING', 'LOW_BATTERY', 'Low battery warning at ATM-008. Battery level: 15%', 'Low battery warning at ATM-008. Battery level: 15%', 'NEW', 4, NULL, NULL, 1),
(9, 'CRITICAL', 'UNAUTHORIZED_ACCESS', 'Forced entry attempt at ATM-009. Security breach protocol activated.', 'Forced entry attempt at ATM-009. Security breach protocol activated.', 'ESCALATED', 4, NOW() - INTERVAL 45 MINUTE, 4, 1),
(10, 'INFO', 'SYSTEM_ERROR', 'Network connectivity restored at ATM-010. Connection stable.', 'Network connectivity restored at ATM-010. Connection stable.', 'RESOLVED', 4, NOW() - INTERVAL 3 HOUR, 4, 1);

-- Insert sample notifications
INSERT INTO notifications (user_id, alert_id, type, title, message, is_read, sent_via) VALUES
(3, 1, 'ALERT', 'Critical Alert: Unauthorized Access', 'Unauthorized access detected at ATM-001. Multiple failed PIN attempts recorded.', FALSE, 'BOTH'),
(3, 2, 'ALERT', 'Warning: Door Open', 'ATM-002 door opened outside business hours. Location: Kandy Branch.', FALSE, 'WEBSOCKET'),
(3, 3, 'ALERT', 'Critical Alert: Fire Detection', 'Fire alarm triggered at ATM-003. Smoke detected in cash dispenser area.', FALSE, 'BOTH'),
(4, 4, 'ALERT', 'System Update', 'ATM-004 system armed successfully for night mode.', TRUE, 'WEBSOCKET'),
(4, 5, 'ALERT', 'Power Warning', 'Power failure detected at ATM-005. UPS battery activated.', FALSE, 'WEBSOCKET'),
(4, 6, 'ALERT', 'Security Alert', 'Tampering detected at ATM-006. Physical access attempt blocked.', FALSE, 'BOTH'),
(4, 7, 'ALERT', 'Maintenance Complete', 'Scheduled maintenance completed at ATM-007. Firmware updated to v1.2.0.', TRUE, 'WEBSOCKET'),
(4, 8, 'ALERT', 'Battery Warning', 'Low battery warning at ATM-008. Battery level: 15%', FALSE, 'WEBSOCKET'),
(1, 9, 'ESCALATION', 'Alert Escalated', 'Forced entry attempt at ATM-009. Security breach protocol activated.', FALSE, 'BOTH'),
(4, 10, 'ALERT', 'System Update', 'Network connectivity restored at ATM-010. Connection stable.', TRUE, 'WEBSOCKET');

-- Insert sample audit logs
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, old_value, new_value, ip_address, user_agent) VALUES
(1, 'USER_LOGIN', 'USER', 1, NULL, 'admin@atmsecurity.com', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(2, 'USER_LOGIN', 'USER', 2, NULL, 'admin@combank.lk', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(3, 'ALERT_ACKNOWLEDGE', 'ALERT', 1, 'NEW', 'ACKNOWLEDGED', '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(4, 'ALERT_RESOLVE', 'ALERT', 4, 'NEW', 'RESOLVED', '192.168.1.102', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(1, 'USER_CREATE', 'USER', 5, NULL, 'security@boc.lk', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(2, 'ATM_CREATE', 'ATM', 4, NULL, 'Bank of Ceylon - Jaffna', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(3, 'ALERT_ESCALATE', 'ALERT', 9, 'INVESTIGATING', 'ESCALATED', '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(4, 'ATM_UPDATE', 'ATM', 7, 'ACTIVE', 'MAINTENANCE', '192.168.1.102', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36');

-- Insert sample SMS logs
INSERT INTO sms_logs (from_number, to_number, message, raw_message, status, sms_id, service_provider, received_at) VALUES
('+94771234567', '+94771234588', 'Unauthorized access detected at ATM-001. Multiple failed PIN attempts recorded.', 'Unauthorized access detected at ATM-001. Multiple failed PIN attempts recorded.', 'PROCESSED', 'SMS001', 'Dialog', NOW() - INTERVAL 1 HOUR),
('+94771234568', '+94771234588', 'ATM-002 door opened outside business hours. Location: Kandy Branch.', 'ATM-002 door opened outside business hours. Location: Kandy Branch.', 'PROCESSED', 'SMS002', 'Mobitel', NOW() - INTERVAL 45 MINUTE),
('+94771234569', '+94771234588', 'Fire alarm triggered at ATM-003. Smoke detected in cash dispenser area.', 'Fire alarm triggered at ATM-003. Smoke detected in cash dispenser area.', 'PROCESSED', 'SMS003', 'Dialog', NOW() - INTERVAL 30 MINUTE),
('+94771234570', '+94771234588', 'Forced entry attempt at ATM-009. Security breach protocol activated.', 'Forced entry attempt at ATM-009. Security breach protocol activated.', 'PROCESSED', 'SMS004', 'Mobitel', NOW() - INTERVAL 15 MINUTE);

-- Insert sample reports
INSERT INTO reports (generated_by, report_type, title, parameters, file_format, scheduled) VALUES
(1, 'DAILY', 'Daily Security Report - 2024-01-15', '{"date": "2024-01-15", "bankId": null}', 'PDF', TRUE),
(2, 'WEEKLY', 'Weekly Alert Summary - Week 2', '{"startDate": "2024-01-08", "endDate": "2024-01-14", "bankId": 1}', 'EXCEL', TRUE),
(4, 'MONTHLY', 'Monthly ATM Status Report - January 2024', '{"month": "2024-01", "bankId": 2}', 'PDF', TRUE),
(1, 'CUSTOM', 'Critical Alerts Analysis', '{"severity": "CRITICAL", "startDate": "2024-01-01", "endDate": "2024-01-31"}', 'CSV', FALSE);

-- Display sample data counts
SELECT 'Banks' as table_name, COUNT(*) as record_count FROM banks
UNION ALL
SELECT 'Users', COUNT(*) FROM users
UNION ALL
SELECT 'ATM Stations', COUNT(*) FROM atm_stations
UNION ALL
SELECT 'Alerts', COUNT(*) FROM alerts
UNION ALL
SELECT 'Notifications', COUNT(*) FROM notifications
UNION ALL
SELECT 'Audit Logs', COUNT(*) FROM audit_logs
UNION ALL
SELECT 'SMS Logs', COUNT(*) FROM sms_logs
UNION ALL
SELECT 'Reports', COUNT(*) FROM reports;

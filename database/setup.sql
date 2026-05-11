-- ATM Security System Database Setup Script
-- Run this script in MySQL to create the required databases

-- Create databases for the microservices
CREATE DATABASE IF NOT EXISTS atm_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS atm_stations CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS atm_alerts CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS atm_reports CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Show created databases
SHOW DATABASES LIKE 'atm_%';

-- Grant privileges (optional - adjust username/password as needed)
-- GRANT ALL PRIVILEGES ON atm_auth.* TO 'root'@'localhost';
-- GRANT ALL PRIVILEGES ON atm_stations.* TO 'root'@'localhost';
-- GRANT ALL PRIVILEGES ON atm_alerts.* TO 'root'@'localhost';
-- GRANT ALL PRIVILEGES ON atm_reports.* TO 'root'@'localhost';

-- Flush privileges
-- FLUSH PRIVILEGES;

SELECT 'ATM Security System databases created successfully!' as status;

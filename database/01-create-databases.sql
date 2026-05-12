-- ATM Security System Database Setup
-- Create main databases for the system

-- Main application database
CREATE DATABASE IF NOT EXISTS atm_security_system 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Audit database for security logs
CREATE DATABASE IF NOT EXISTS atm_security_audit 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Switch to main database
USE atm_security_system;

-- Show databases
SHOW DATABASES;

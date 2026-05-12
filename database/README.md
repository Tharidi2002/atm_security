# ATM Security System Database Setup

This directory contains all necessary SQL scripts to set up the ATM Security System database.

## Files Description

### 1. `01-create-databases.sql`
Creates the main databases required for the system:
- `atm_security_system` - Main application database
- `atm_security_audit` - Audit logs database (for compliance)

### 2. `02-create-tables.sql`
Creates all necessary tables with proper relationships:
- `banks` - Bank information
- `users` - User accounts with roles and security settings
- `atm_stations` - ATM locations and status
- `alerts` - Security alerts and incidents
- `notifications` - User notifications
- `websocket_sessions` - Active WebSocket connections
- `audit_logs` - Security audit trail
- `reports` - Generated reports metadata
- `sms_logs` - Incoming SMS messages

### 3. `03-insert-sample-data.sql`
Inserts sample data for testing:
- 5 sample banks
- 5 sample users (admin, bank admins, security officers)
- 10 sample ATM stations
- 10 sample alerts (various severities)
- Sample notifications, audit logs, SMS logs, and reports

## Database Configuration

### Connection Details
- **Host**: localhost
- **Port**: 3306
- **Database**: atm_security_system
- **Username**: root
- **Password**: MySQL@123

### Character Set
- **Charset**: utf8mb4
- **Collation**: utf8mb4_unicode_ci

## Setup Instructions

### Option 1: Manual Setup (Recommended for Development)
```bash
# 1. Connect to MySQL
mysql -u root -pMySQL@123

# 2. Execute scripts in order
source 01-create-databases.sql
source 02-create-tables.sql
source 03-insert-sample-data.sql
```

### Option 2: Hibernate Auto-Creation (Production)
- Set `spring.jpa.hibernate.ddl-auto=update` in application.yml
- Tables will be created/updated automatically when services start
- Use manual setup for initial data and proper constraints

### Option 3: Batch Setup
```bash
# Create batch script for Windows
mysql -u root -pMySQL@123 < 01-create-databases.sql
mysql -u root -pMySQL@123 < 02-create-tables.sql
mysql -u root -pMySQL@123 < 03-insert-sample-data.sql
```

## Table Relationships

```
banks (1) ── (N) atm_stations
banks (1) ── (N) users

users (1) ── (N) alerts (assigned_to)
users (1) ── (N) alerts (acknowledged_by)
users (1) ── (N) alerts (investigation_started_by)
users (1) ── (N) alerts (resolved_by)
users (1) ── (N) alerts (escalated_to)

atm_stations (1) ── (N) alerts

alerts (1) ── (N) notifications

users (1) ── (N) websocket_sessions
users (1) ── (N) audit_logs
users (1) ── (N) reports
```

## Security Features

### User Roles
- **SUPER_ADMIN**: Full system access
- **BANK_ADMIN**: Bank-specific access
- **SECURITY_OFFICER**: Alert management

### Data Integrity
- Foreign key constraints
- Proper indexing for performance
- Audit trail for all critical operations

### Compliance
- 7-year audit retention requirement
- GDPR-compliant data handling
- Role-based access control

## Performance Considerations

### Indexes
All tables have appropriate indexes:
- Primary keys (auto-increment)
- Foreign key relationships
- Search fields (username, email, phone_number)
- Date fields (created_at, last_heartbeat)
- Status fields (is_active, status)

### Storage Engine
- InnoDB for transaction support
- Row-level locking
- Foreign key constraints

## Testing Data

### Sample Users
- **admin@atmsecurity.com** (Super Admin)
- **admin@combank.lk** (Commercial Bank Admin)
- **security@combank.lk** (Commercial Bank Security Officer)
- **admin@boc.lk** (Bank of Ceylon Admin)
- **security@boc.lk** (Bank of Ceylon Security Officer)

### Sample ATMs
- 10 ATM stations across different banks
- Various locations (Colombo, Kandy, Galle, Jaffna, etc.)
- Different zones (Cash Counter, Pawning Area, High Security)
- Mixed statuses (Active, Maintenance)

### Sample Alerts
- Critical alerts (Unauthorized Access, Fire, Tampering)
- Warning alerts (Door Open, Power Failure)
- Info alerts (System Updates, Maintenance)

## Troubleshooting

### Common Issues

1. **Connection Errors**
   - Verify MySQL is running
   - Check credentials in application.yml
   - Ensure firewall allows port 3306

2. **Table Creation Errors**
   - Check MySQL version (8.0+ required)
   - Verify character set support
   - Check available storage engines

3. **Foreign Key Errors**
   - Ensure parent tables exist
   - Check data types match
   - Verify NOT NULL constraints

### Verification Commands
```sql
-- Check database exists
SHOW DATABASES;

-- Check tables exist
USE atm_security_system;
SHOW TABLES;

-- Check data counts
SELECT 
    (SELECT COUNT(*) FROM banks) as banks,
    (SELECT COUNT(*) FROM users) as users,
    (SELECT COUNT(*) FROM atm_stations) as atms,
    (SELECT COUNT(*) FROM alerts) as alerts;
```

## Maintenance

### Backup Strategy
```bash
# Full backup
mysqldump -u root -pMySQL@123 --single-transaction --routines --triggers atm_security_system > backup_$(date +%Y%m%d).sql

# Incremental backup (binary log)
mysqlbinlog --start-datetime="2024-01-01 00:00:00" /var/log/mysql/mysql-bin.000001 > incremental.sql
```

### Monitoring
```sql
-- Check table sizes
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'atm_security_system'
ORDER BY (data_length + index_length) DESC;

-- Check active connections
SHOW PROCESSLIST;

-- Check slow queries
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;
```

## Migration Strategy

### Development to Production
1. Export development schema: `mysqldump --no-data`
2. Review and optimize indexes
3. Test with production data volume
4. Plan migration window
5. Execute with minimal downtime

### Version Control
- Use Flyway or Liquibase for production
- Version all schema changes
- Maintain rollback procedures
- Document breaking changes

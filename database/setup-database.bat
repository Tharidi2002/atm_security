@echo off
echo ========================================
echo ATM Security System Database Setup
echo ========================================
echo.

echo Checking MySQL connection...
mysql -u root -pMySQL@123 -e "SELECT 1;" >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Cannot connect to MySQL server
    echo Please ensure MySQL is running and credentials are correct
    echo Host: localhost
    echo Port: 3306
    echo Username: root
    echo Password: MySQL@123
    pause
    exit /b 1
)

echo MySQL connection successful!
echo.

echo Creating databases and tables...
echo.

echo Step 1: Creating databases...
mysql -u root -pMySQL@123 < 01-create-databases.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to create databases
    pause
    exit /b 1
)
echo Databases created successfully!

echo.
echo Step 2: Creating tables...
mysql -u root -pMySQL@123 < 02-create-tables.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to create tables
    pause
    exit /b 1
)
echo Tables created successfully!

echo.
echo Step 3: Inserting sample data...
mysql -u root -pMySQL@123 < 03-insert-sample-data.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to insert sample data
    pause
    exit /b 1
)
echo Sample data inserted successfully!

echo.
echo ========================================
echo Database setup completed!
echo ========================================
echo.
echo Database: atm_security_system
echo Username: root
echo Password: MySQL@123
echo.
echo Sample data created:
echo - 5 Banks
echo - 5 Users (admin, bank admins, security officers)
echo - 10 ATM Stations
echo - 10 Security Alerts
echo - Sample Notifications, Audit Logs, SMS Logs
echo.
echo You can now start the backend services!
echo.
pause

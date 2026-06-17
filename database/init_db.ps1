# ATM Security System Database Initializer (PowerShell)
$DB_HOST = "localhost"
$DB_USER = "root"
$DB_PASSWORD = "Ijse@123"
$SQL_DIR = Join-Path $PSScriptRoot "sql"
$RUN_ALL_FILE = Join-Path $SQL_DIR "00_run_all.sql"

Write-Host "ATM Security System - Database Initialization" -ForegroundColor Cyan
Write-Host "Connecting to MySQL at $DB_HOST as $DB_USER..." -ForegroundColor Yellow

# Test if mysql command exists
if (-not (Get-Command mysql -ErrorAction SilentlyContinue)) {
    Write-Error "MySQL client ('mysql') is not installed or not in the system PATH."
    Write-Host "Please make sure MySQL is installed and added to your Environment Variables." -ForegroundColor Red
    Exit 1
}

# Run the MySQL script
Write-Host "Executing SQL scripts from $RUN_ALL_FILE..." -ForegroundColor Yellow

# We temporarily change location to SQL directory because 00_run_all.sql uses relative SOURCE commands
Push-Location $SQL_DIR
try {
    & mysql -h $DB_HOST -u $DB_USER "-p$DB_PASSWORD" --default-character-set=utf8 -e "source 00_run_all.sql"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Database initialization completed successfully!" -ForegroundColor Green
    } else {
        Write-Error "MySQL command failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}

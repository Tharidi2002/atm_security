@echo off
title ATM Security System DB Initializer
echo ATM Security System - Database Initialization
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0init_db.ps1"
pause

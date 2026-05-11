@echo off
echo Starting Auth Service (Port 8081)
echo ===============================

REM Find Maven in common locations
set MVN_CMD=
if exist "C:\maven\apache-maven-3.9.15\bin\mvn.cmd" (
    set MVN_CMD="C:\maven\apache-maven-3.9.15\bin\mvn.cmd"
) else if exist "C:\Program Files\Apache\maven\bin\mvn.cmd" (
    set MVN_CMD="C:\Program Files\Apache\maven\bin\mvn.cmd"
) else if exist "C:\Program Files (x86)\Apache\maven\bin\mvn.cmd" (
    set MVN_CMD="C:\Program Files (x86)\Apache\maven\bin\mvn.cmd"
) else if exist "C:\maven\bin\mvn.cmd" (
    set MVN_CMD="C:\maven\bin\mvn.cmd"
) else (
    echo Maven not found. Please install Maven or add to PATH
    echo Download from: https://maven.apache.org/download.cgi
    pause
    exit /b
)

echo Using Maven: %MVN_CMD%
%MVN_CMD% spring-boot:run

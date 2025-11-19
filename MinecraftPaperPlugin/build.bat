@echo off
REM QuizPlugin Build Script for Windows
REM This script builds the plugin JAR file using Maven

echo ======================================
echo QuizPlugin Build Script
echo ======================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed!
    echo Please install Maven from https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

REM Display Maven version
echo Maven version:
call mvn -version
echo.

REM Clean and build
echo Building QuizPlugin...
echo.
call mvn clean package

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================
    echo Build Successful!
    echo ======================================
    echo.
    echo The plugin JAR file is located at:
    echo   target\QuizPlugin-1.0.0.jar
    echo.
    echo Copy this file to your server's plugins/ folder
    echo.
) else (
    echo.
    echo ======================================
    echo Build Failed!
    echo ======================================
    echo.
    echo Please check the error messages above
    echo.
)

pause

@echo off
REM Start All Microservices
echo ========================================
echo   Starting All Microservices
echo ========================================
echo.

set GRADLE_PATH=D:\E-drive-Software\gradle-8.11\bin\gradle.bat

echo Building all services...
call "%GRADLE_PATH%" :services:catalog:build :services:cart:build :services:fulfillment:build :services:inventory:build :services:orders:build :services:payments:build :services:pricing:build :services:user-access:build -x test --no-daemon
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful! Starting services...
echo.

REM Start each service in a new window
start "User Access - 8080" cmd /k "cd /d %~dp0 && "%GRADLE_PATH%" :services:user-access:bootRun --no-daemon"
timeout /t 3 /nobreak >nul

start "Catalog - 8081" cmd /k "cd /d %~dp0 && "%GRADLE_PATH%" :services:catalog:bootRun --no-daemon"
timeout /t 3 /nobreak >nul

start "Pricing - 8082" cmd /k "cd /d %~dp0 && "%GRADLE_PATH%" :services:pricing:bootRun --no-daemon"
timeout /t 3 /nobreak >nul

start "Cart - 8083" cmd /k "cd /d %~dp0 && "%GRADLE_PATH%" :services:cart:bootRun --no-daemon"
timeout /t 3 /nobreak >nul

start "Orders - 8084" cmd /k "cd /d %~dp0 && "%GRADLE_PATH%" :services:orders:bootRun --no-daemon"
timeout /t 3 /nobreak >nul

start "Payments - 8085" cmd /k "cd /d %~dp0 && "%GRADLE_PATH%" :services:payments:bootRun --no-daemon"
timeout /t 3 /nobreak >nul

start "Fulfillment - 8086" cmd /k "cd /d %~dp0 && "%GRADLE_PATH%" :services:fulfillment:bootRun --no-daemon"
timeout /t 3 /nobreak >nul

start "Inventory - 8087" cmd /k "cd /d %~dp0 && "%GRADLE_PATH%" :services:inventory:bootRun --no-daemon"

echo.
echo ========================================
echo   All Services Started!
echo ========================================
echo.
echo Services:
echo   User Access  - http://localhost:8080
echo   Catalog      - http://localhost:8081
echo   Pricing      - http://localhost:8082
echo   Cart         - http://localhost:8083
echo   Orders       - http://localhost:8084
echo   Payments     - http://localhost:8085
echo   Fulfillment  - http://localhost:8086
echo   Inventory    - http://localhost:8087
echo.
echo Each service is running in its own window.
echo Close windows to stop individual services.
echo.
pause

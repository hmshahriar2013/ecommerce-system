@echo off
echo.
echo ============================================
echo   Memory Usage Check
echo ============================================
echo.

echo Checking Java processes memory consumption...
echo.

for /f "tokens=1,2" %%a in ('netstat -ano ^| findstr "LISTENING" ^| findstr "8080 8081 8082 8083 8084 8085 8086 8087"') do (
    set PORT=%%b
    for /f "tokens=5" %%p in ("%%a %%b") do (
        wmic process where processid=%%p get processid,workingsetsize,commandline /format:list 2>nul | findstr /i "="
        echo ---
    )
)

echo.
echo Total System Memory:
wmic OS get TotalVisibleMemorySize,FreePhysicalMemory /format:list
echo.
echo ============================================

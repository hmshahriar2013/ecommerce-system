@echo off
set SCENARIOS=%1
set RAMP=%2

if "%SCENARIOS%"=="" set SCENARIOS=100
if "%RAMP%"=="" set RAMP=60

echo Starting Performance Test: %SCENARIOS% scenarios over %RAMP% seconds ramp-up...
echo Memory Settings: 256MB initial, 512MB max

java -Xms256m -Xmx512m ^
     -XX:MaxMetaspaceSize=256m ^
     -XX:ReservedCodeCacheSize=128m ^
     -XX:+UseG1GC ^
     -XX:MaxGCPauseMillis=200 ^
     -XX:+UseStringDeduplication ^
     -Dscenarios=%SCENARIOS% ^
     -Dramp=%RAMP% ^
     -jar services\perf-test\build\libs\perf-test.jar

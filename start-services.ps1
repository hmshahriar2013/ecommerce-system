# Start All Microservices Script
# This script builds and starts all 8 microservices in separate PowerShell windows

$ErrorActionPreference = "Stop"

# Gradle path
$gradlePath = "D:\E-drive-Software\gradle-8.11\bin\gradle.bat"

# Service definitions
$services = @(
    @{ Name = "user-access"; Port = 8080 }
    @{ Name = "catalog"; Port = 8081 }
    @{ Name = "pricing"; Port = 8082 }
    @{ Name = "cart"; Port = 8083 }
    @{ Name = "orders"; Port = 8084 }
    @{ Name = "payments"; Port = 8085 }
    @{ Name = "fulfillment"; Port = 8086 }
    @{ Name = "inventory"; Port = 8087 }
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Building All Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Build all services first
Write-Host "Building all services (this may take a minute)..." -ForegroundColor Yellow
try {
    & $gradlePath build -x test --no-daemon 2>&1 | Out-Null
    Write-Host "✓ All services built successfully!" -ForegroundColor Green
}
catch {
    Write-Host "✗ Build failed. Please check the error messages." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$startedServices = @()

foreach ($service in $services) {
    Write-Host "Starting $($service.Name) on port $($service.Port)..." -ForegroundColor Yellow
    
    $serviceName = $service.Name
    $servicePort = $service.Port
    $windowTitle = "$serviceName Service - Port $servicePort"
    
    # Create a command to run in the new window
    $command = "Set-Location '$PSScriptRoot'; " +
    "`$Host.UI.RawUI.WindowTitle = '$windowTitle'; " +
    "Write-Host '======================================' -ForegroundColor Cyan; " +
    "Write-Host '  $($serviceName.ToUpper()) SERVICE - PORT $servicePort' -ForegroundColor Cyan; " +
    "Write-Host '======================================' -ForegroundColor Cyan; " +
    "Write-Host ''; " +
    "& '$gradlePath' :services:$($serviceName):bootRun --no-daemon"
    
    try {
        $process = Start-Process powershell -ArgumentList "-NoExit", "-Command", $command -PassThru
        
        $startedServices += @{
            Name      = $service.Name
            Port      = $service.Port
            ProcessId = $process.Id
        }
        
        Write-Host "  ✓ Started in new window (PID: $($process.Id))" -ForegroundColor Green
        Start-Sleep -Seconds 2
    }
    catch {
        Write-Host "  ✗ Failed to start: $_" -ForegroundColor Red
    }
    
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All Services Started!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Service Status:" -ForegroundColor Yellow
foreach ($service in $startedServices) {
    Write-Host "  ✓ $($service.Name.PadRight(15)) | http://localhost:$($service.Port) | PID: $($service.ProcessId)" -ForegroundColor Green
}
Write-Host ""

Write-Host "Health Checks:" -ForegroundColor Yellow
foreach ($service in $startedServices) {
    Write-Host "  http://localhost:$($service.Port)/actuator/health" -ForegroundColor Cyan
}
Write-Host ""

Write-Host "Tips:" -ForegroundColor Yellow
Write-Host "  • Each service runs in its own window" -ForegroundColor White
Write-Host "  • Services take 30-60 seconds to fully start" -ForegroundColor White
Write-Host "  • Close windows or press Ctrl+C to stop services" -ForegroundColor White
Write-Host ""

Write-Host "Press any key to exit (services will continue running)..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')

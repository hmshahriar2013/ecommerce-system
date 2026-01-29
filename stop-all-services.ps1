# Stop All Services Script
# Detects and stops Java processes listening on service ports 8080-8087

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "  Stopping All Services" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

# Service ports and names
$servicePorts = @(
    @{Port = 8080; Name = "User Access" },
    @{Port = 8081; Name = "Catalog" },
    @{Port = 8082; Name = "Pricing" },
    @{Port = 8083; Name = "Cart" },
    @{Port = 8084; Name = "Orders" },
    @{Port = 8085; Name = "Payments" },
    @{Port = 8086; Name = "Fulfillment" },
    @{Port = 8087; Name = "Inventory" }
)

Write-Host "Detecting running services..." -ForegroundColor Yellow
Write-Host ""

# Find processes listening on service ports
$servicesToStop = @()

foreach ($service in $servicePorts) {
    $connections = Get-NetTCPConnection -LocalPort $service.Port -State Listen -ErrorAction SilentlyContinue
    
    if ($connections) {
        $processId = $connections[0].OwningProcess
        $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
        
        if ($process) {
            $servicesToStop += @{
                Port        = $service.Port
                Name        = $service.Name
                PID         = $processId
                ProcessName = $process.Name
            }
            Write-Host "  [CHECK] Found: $($service.Name) (Port $($service.Port), PID: $processId)" -ForegroundColor White
        }
    }
}

Write-Host ""

if ($servicesToStop.Count -eq 0) {
    Write-Host "No running services found." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Note: This script detects Java processes on ports 8080-8087" -ForegroundColor Gray
    exit 0
}

Write-Host "Found $($servicesToStop.Count) running service(s)." -ForegroundColor Yellow
Write-Host ""

$confirmation = Read-Host "Stop all services? (y/N)"

if ($confirmation -ne 'y' -and $confirmation -ne 'Y') {
    Write-Host "Cancelled." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Stopping services..." -ForegroundColor Cyan

foreach ($service in $servicesToStop) {
    try {
        Write-Host "  Stopping: $($service.Name) (PID: $($service.PID))..." -ForegroundColor White
        Stop-Process -Id $service.PID -Force -ErrorAction Stop
        Write-Host "    [OK] Stopped" -ForegroundColor Green
    }
    catch {
        Write-Host "    [FAILED] Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Verifying services stopped..." -ForegroundColor Cyan

$anyStillRunning = $false
foreach ($service in $servicePorts) {
    $connections = Get-NetTCPConnection -LocalPort $service.Port -State Listen -ErrorAction SilentlyContinue
    
    if ($connections) {
        $processId = $connections[0].OwningProcess
        Write-Host "  [WARN] $($service.Name) still running on port $($service.Port) (PID: $processId)" -ForegroundColor Red
        $anyStillRunning = $true
    }
}

if (-not $anyStillRunning) {
    Write-Host "  [OK] All services confirmed stopped" -ForegroundColor Green
}

Write-Host ""
Write-Host "All services stopped." -ForegroundColor Green
Write-Host ""

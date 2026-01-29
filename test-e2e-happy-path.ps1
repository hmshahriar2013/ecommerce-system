# End-to-End Happy Path Test Script
# Tests the complete e-commerce flow across all 8 microservices

param(
    [string]$BaseUrl = "http://localhost",
    [switch]$Verbose
)

$ErrorActionPreference = "Continue"
$testsPassed = 0
$testsFailed = 0

function Write-TestHeader {
    param([string]$Message)
    Write-Host "`n================================================================" -ForegroundColor Cyan
    Write-Host "  $Message" -ForegroundColor Cyan
    Write-Host "================================================================" -ForegroundColor Cyan
}

function Write-TestStep {
    param([string]$Step, [string]$Description)
    Write-Host "`n[$Step] $Description" -ForegroundColor Yellow
}

function Write-Success {
    param([string]$Message)
    Write-Host "  ✓ $Message" -ForegroundColor Green
    $script:testsPassed++
}

function Write-Failure {
    param([string]$Message)
    Write-Host "  ✗ $Message" -ForegroundColor Red
    $script:testsFailed++
}

function Invoke-ApiCall {
    param(
        [string]$Method,
        [string]$Uri,
        [string]$Body = $null,
        [string]$Description
    )
    
    if ($Verbose) {
        Write-Host "  Request: $Method $Uri" -ForegroundColor Gray
        if ($Body) { Write-Host "  Body: $Body" -ForegroundColor Gray }
    }
    
    try {
        $params = @{
            Method      = $Method
            Uri         = $Uri
            ContentType = "application/json"
            ErrorAction = "Stop"
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-RestMethod @params
        
        if ($Verbose) {
            Write-Host "  Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
        }
        
        Write-Success $Description
        return $response
    }
    catch {
        Write-Failure "$Description - Error: $($_.Exception.Message)"
        return $null
    }
}

function Test-ServiceHealth {
    param([int]$Port, [string]$ServiceName)
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl`:$Port/actuator/health" -Method Get -ErrorAction Stop
        if ($response.status -eq "UP") {
            Write-Success "$ServiceName is UP"
            return $true
        }
    }
    catch {
        Write-Failure "$ServiceName is DOWN"
        return $false
    }
}

# Main Test Flow
Write-TestHeader "E2E Happy Path Test - Starting"

# Step 0: Check all services are running
Write-TestStep "STEP 0" "Checking Service Health"

$services = @(
    @{Port = 8080; Name = "User Access" },
    @{Port = 8081; Name = "Catalog" },
    @{Port = 8082; Name = "Pricing" },
    @{Port = 8083; Name = "Cart" },
    @{Port = 8084; Name = "Orders" },
    @{Port = 8085; Name = "Payments" },
    @{Port = 8086; Name = "Fulfillment" },
    @{Port = 8087; Name = "Inventory" }
)

$allHealthy = $true
foreach ($service in $services) {
    if (-not (Test-ServiceHealth -Port $service.Port -ServiceName $service.Name)) {
        $allHealthy = $false
    }
}

if (-not $allHealthy) {
    Write-Host "`nNot all services are running. Please start services first using start-all.bat" -ForegroundColor Red
    exit 1
}

# Step 1: Create User
Write-TestStep "STEP 1" "Create User (User Access Service)"

$userBody = @{
    email    = "test.user@example.com"
    fullName = "Test User"
    role     = "CUSTOMER"
} | ConvertTo-Json

$userResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8080/api/users" -Body $userBody -Description "User created successfully"

if (-not $userResponse) {
    Write-Host "`nTest aborted: Failed to create user" -ForegroundColor Red
    exit 1
}

$userId = $userResponse.userId
Write-Host "  User ID: $userId" -ForegroundColor White

# Step 2: Create Product
Write-TestStep "STEP 2" "Create Product (Catalog Service)"

$productBody = @{
    name        = "Premium Laptop"
    description = "High-performance laptop with 16GB RAM"
    sku         = "LAPTOP-001"
} | ConvertTo-Json

$productResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8081/api/catalog/products" -Body $productBody -Description "Product created successfully"

if (-not $productResponse) {
    Write-Host "`nTest aborted: Failed to create product" -ForegroundColor Red
    exit 1
}

$productId = $productResponse.productId
Write-Host "  Product ID: $productId" -ForegroundColor White

# Step 3: Publish Product
Write-TestStep "STEP 3" "Publish Product (Catalog Service)"

$publishResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8081/api/catalog/products/$productId/publish" -Description "Product published successfully"

# Step 4: Set Price
Write-TestStep "STEP 4" "Set Product Price (Pricing Service)"

$priceBody = @{
    amount   = 1299.99
    currency = "USD"
} | ConvertTo-Json

$priceResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8082/api/pricing/products/$productId/price" -Body $priceBody -Description "Price set successfully"

# Step 5: Initialize Stock
Write-TestStep "STEP 5" "Initialize Stock (Inventory Service)"

$stockBody = @{
    productId         = $productId
    initialQuantity   = 100
    lowStockThreshold = 10
} | ConvertTo-Json

$stockResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8087/api/inventory/stock" -Body $stockBody -Description "Stock initialized successfully"

# Step 6: Add to Cart
Write-TestStep "STEP 6" "Add Product to Cart (Cart Service)"

$cartId = [System.Guid]::NewGuid().ToString()
$cartBody = @{
    cartId    = $cartId
    userId    = $userId
    productId = $productId
    quantity  = 2
    price     = 1299.99
    currency  = "USD"
} | ConvertTo-Json

$cartResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8083/api/cart/items" -Body $cartBody -Description "Product added to cart successfully"

# Step 7: Reserve Stock
Write-TestStep "STEP 7" "Reserve Stock (Inventory Service)"

$reservationId = [System.Guid]::NewGuid().ToString()
$reserveBody = @{
    productId     = $productId
    reservationId = $reservationId
    quantity      = 2
} | ConvertTo-Json

# Use productId as stockId for now (simplified)
$reserveResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8087/api/inventory/stock/$productId/reserve" -Body $reserveBody -Description "Stock reserved successfully"

Write-Host "  Reservation ID: $reservationId" -ForegroundColor White

# Step 8: Place Order
Write-TestStep "STEP 8" "Place Order (Orders Service)"

$orderBody = @{
    userId = $userId
    items  = @(
        @{
            productId = $productId
            quantity  = 2
        }
    )
} | ConvertTo-Json -Depth 10

$orderResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8084/api/orders" -Body $orderBody -Description "Order placed successfully"

if ($orderResponse) {
    $orderId = $orderResponse.orderId
    Write-Host "  Order ID: $orderId" -ForegroundColor White
}
else {
    Write-Host "`nTest aborted: Failed to place order" -ForegroundColor Red
    exit 1
}

# Step 9: Process Payment
Write-TestStep "STEP 9" "Process Payment (Payments Service)"

$paymentBody = @{
    orderId  = $orderId
    amount   = 2599.98
    currency = "USD"
} | ConvertTo-Json

$paymentResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8085/api/payments" -Body $paymentBody -Description "Payment processed successfully"

if ($paymentResponse) {
    $paymentId = $paymentResponse.paymentId
    Write-Host "  Payment ID: $paymentId" -ForegroundColor White
}
else {
    Write-Host "`nTest aborted: Failed to process payment" -ForegroundColor Red
    exit 1
}

# Step 10: Create Shipment
Write-TestStep "STEP 10" "Create Shipment (Fulfillment Service)"

$shipmentBody = @{
    orderId = $orderId
} | ConvertTo-Json

$shipmentResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8086/api/fulfillment/shipments" -Body $shipmentBody -Description "Shipment created successfully"

if ($shipmentResponse) {
    $shipmentId = $shipmentResponse.shipmentId
    Write-Host "  Shipment ID: $shipmentId" -ForegroundColor White
}
else {
    Write-Host "`nTest aborted: Failed to create shipment" -ForegroundColor Red
    exit 1
}

# Step 11: Dispatch Shipment
Write-TestStep "STEP 11" "Dispatch Shipment (Fulfillment Service)"

$dispatchBody = @{
    carrier        = "FedEx"
    trackingNumber = "TRACK-" + [System.Guid]::NewGuid().ToString().Substring(0, 8)
} | ConvertTo-Json

$dispatchResponse = Invoke-ApiCall -Method Post -Uri "$BaseUrl`:8086/api/fulfillment/shipments/$shipmentId/dispatch" -Body $dispatchBody -Description "Shipment dispatched successfully"

# Test Summary
Write-TestHeader "Test Summary"

Write-Host "`nTotal Tests: $($testsPassed + $testsFailed)" -ForegroundColor White
Write-Host "Passed: $testsPassed" -ForegroundColor Green
Write-Host "Failed: $testsFailed" -ForegroundColor Red

if ($testsFailed -eq 0) {
    Write-Host "`n✓ ALL TESTS PASSED - Happy path complete!" -ForegroundColor Green
    exit 0
}
else {
    Write-Host "`n✗ SOME TESTS FAILED - Please check the errors above" -ForegroundColor Red
    exit 1
}

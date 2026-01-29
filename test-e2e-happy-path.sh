#!/bin/bash
# End-to-End Happy Path Test Script
# Tests the complete e-commerce flow across all 8 microservices

BASE_URL="${1:-http://localhost}"
VERBOSE="${VERBOSE:-false}"
TESTS_PASSED=0
TESTS_FAILED=0

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

function test_header() {
    echo -e "\n${CYAN}================================================================${NC}"
    echo -e "${CYAN}  $1${NC}"
    echo -e "${CYAN}================================================================${NC}"
}

function test_step() {
    echo -e "\n${YELLOW}[$1] $2${NC}"
}

function test_success() {
    echo -e "${GREEN}  ✓ $1${NC}"
    ((TESTS_PASSED++))
}

function test_failure() {
    echo -e "${RED}  ✗ $1${NC}"
    ((TESTS_FAILED++))
}

function api_call() {
    local method=$1
    local url=$2
    local body=$3
    local description=$4
    
    if [ "$VERBOSE" = "true" ]; then
        echo -e "${GRAY}  Request: $method $url${NC}"
        [ -n "$body" ] && echo -e "${GRAY}  Body: $body${NC}"
    fi
    
    if [ -n "$body" ]; then
        response=$(curl -s -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$body" \
            -w "\n%{http_code}")
    else
        response=$(curl -s -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -w "\n%{http_code}")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$VERBOSE" = "true" ]; then
        echo -e "${GRAY}  Response: $body${NC}"
    fi
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        test_success "$description"
        echo "$body"
        return 0
    else
        test_failure "$description - HTTP $http_code"
        return 1
    fi
}

function test_service_health() {
    local port=$1
    local name=$2
    
    status=$(curl -s "$BASE_URL:$port/actuator/health" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    
    if [ "$status" = "UP" ]; then
        test_success "$name is UP"
        return 0
    else
        test_failure "$name is DOWN"
        return 1
    fi
}

# Main Test Flow
test_header "E2E Happy Path Test - Starting"

# Step 0: Check all services
test_step "STEP 0" "Checking Service Health"

all_healthy=true
test_service_health 8080 "User Access" || all_healthy=false
test_service_health 8081 "Catalog" || all_healthy=false
test_service_health 8082 "Pricing" || all_healthy=false
test_service_health 8083 "Cart" || all_healthy=false
test_service_health 8084 "Orders" || all_healthy=false
test_service_health 8085 "Payments" || all_healthy=false
test_service_health 8086 "Fulfillment" || all_healthy=false
test_service_health 8087 "Inventory" || all_healthy=false

if [ "$all_healthy" = "false" ]; then
    echo -e "\n${RED}Not all services are running. Please start services first.${NC}"
    exit 1
fi

# Step 1: Create User
test_step "STEP 1" "Create User (User Access Service)"

user_body='{
  "email": "test.user@example.com",
  "fullName": "Test User",
  "role": "CUSTOMER"
}'

user_response=$(api_call POST "$BASE_URL:8080/api/users" "$user_body" "User created successfully")
if [ $? -ne 0 ]; then
    echo -e "\n${RED}Test aborted: Failed to create user${NC}"
    exit 1
fi

user_id=$(echo "$user_response" | grep -o '"userId":"[^"]*"' | cut -d'"' -f4)
echo -e "${WHITE}  User ID: $user_id${NC}"

# Step 2: Create Product
test_step "STEP 2" "Create Product (Catalog Service)"

product_body='{
  "name": "Premium Laptop",
  "description": "High-performance laptop with 16GB RAM",
  "sku": "LAPTOP-001"
}'

product_response=$(api_call POST "$BASE_URL:8081/api/catalog/products" "$product_body" "Product created successfully")
if [ $? -ne 0 ]; then
    echo -e "\n${RED}Test aborted: Failed to create product${NC}"
    exit 1
fi

product_id=$(echo "$product_response" | grep -o '"productId":"[^"]*"' | cut -d'"' -f4)
echo -e "${WHITE}  Product ID: $product_id${NC}"

# Step 3: Publish Product
test_step "STEP 3" "Publish Product (Catalog Service)"
api_call POST "$BASE_URL:8081/api/catalog/products/$product_id/publish" "" "Product published successfully"

# Step 4: Set Price
test_step "STEP 4" "Set Product Price (Pricing Service)"

price_body="{
  \"amount\": 1299.99,
  \"currency\": \"USD\"
}"

api_call POST "$BASE_URL:8082/api/pricing/products/$product_id/price" "$price_body" "Price set successfully"

# Step 5: Initialize Stock
test_step "STEP 5" "Initialize Stock (Inventory Service)"

stock_body="{
  \"productId\": \"$product_id\",
  \"initialQuantity\": 100,
  \"lowStockThreshold\": 10
}"

api_call POST "$BASE_URL:8087/api/inventory/stock" "$stock_body" "Stock initialized successfully"

# Step 6: Add to Cart
test_step "STEP 6" "Add Product to Cart (Cart Service)"

# Generate cart ID using PowerShell on Windows, uuidgen elsewhere
if command -v powershell.exe &> /dev/null; then
    cart_id=$(powershell.exe -Command "[guid]::NewGuid().ToString()" | tr -d '\r')
elif command -v uuidgen &> /dev/null; then
    cart_id=$(uuidgen | tr '[:upper:]' '[:lower:]')
else
    cart_id="$(date +%s)-$(( RANDOM % 1000 ))"
fi

cart_body="{
  \"cartId\": \"$cart_id\",
  \"userId\": \"$user_id\",
  \"productId\": \"$product_id\",
  \"quantity\": 2,
  \"price\": 1299.99,
  \"currency\": \"USD\"
}"

api_call POST "$BASE_URL:8083/api/cart/items" "$cart_body" "Product added to cart successfully"

# Step 7: Reserve Stock
test_step "STEP 7" "Reserve Stock (Inventory Service)"

# Generate reservation ID using PowerShell on Windows, uuidgen elsewhere
if command -v powershell.exe &> /dev/null; then
    reservation_id=$(powershell.exe -Command "[guid]::NewGuid().ToString()" | tr -d '\r')
elif command -v uuidgen &> /dev/null; then
    reservation_id=$(uuidgen | tr '[:upper:]' '[:lower:]')
else
    reservation_id="$(date +%s)-$(( RANDOM % 1000 ))"
fi

reserve_body="{
  \"productId\": \"$product_id\",
  \"reservationId\": \"$reservation_id\",
  \"quantity\": 2
}"

api_call POST "$BASE_URL:8087/api/inventory/stock/$product_id/reserve" "$reserve_body" "Stock reserved successfully"
echo -e "${WHITE}  Reservation ID: $reservation_id${NC}"

# Step 8: Place Order
test_step "STEP 8" "Place Order (Orders Service)"

order_body="{
  \"userId\": \"$user_id\",
  \"items\": [
    {
      \"productId\": \"$product_id\",
      \"quantity\": 2
    }
  ]
}"

order_response=$(api_call POST "$BASE_URL:8084/api/orders" "$order_body" "Order placed successfully")
if [ $? -ne 0 ]; then
    echo -e "\n${RED}Test aborted: Failed to place order${NC}"
    exit 1
fi

order_id=$(echo "$order_response" | grep -o '"orderId":"[^"]*"' | cut -d'"' -f4)
echo -e "${WHITE}  Order ID: $order_id${NC}"

# Step 9: Process Payment
test_step "STEP 9" "Process Payment (Payments Service)"

payment_body="{
  \"orderId\": \"$order_id\",
  \"amount\": 2599.98,
  \"currency\": \"USD\"
}"

payment_response=$(api_call POST "$BASE_URL:8085/api/payments" "$payment_body" "Payment processed successfully")
if [ $? -ne 0 ]; then
    echo -e "\n${RED}Test aborted: Failed to process payment${NC}"
    exit 1
fi

payment_id=$(echo "$payment_response" | grep -o '"paymentId":"[^"]*"' | cut -d'"' -f4)
echo -e "${WHITE}  Payment ID: $payment_id${NC}"

# Step 10: Create Shipment
test_step "STEP 10" "Create Shipment (Fulfillment Service)"

shipment_body="{
  \"orderId\": \"$order_id\"
}"

shipment_response=$(api_call POST "$BASE_URL:8086/api/fulfillment/shipments" "$shipment_body" "Shipment created successfully")
if [ $? -ne 0 ]; then
    echo -e "\n${RED}Test aborted: Failed to create shipment${NC}"
    exit 1
fi

shipment_id=$(echo "$shipment_response" | grep -o '"shipmentId":"[^"]*"' | cut -d'"' -f4)
echo -e "${WHITE}  Shipment ID: $shipment_id${NC}"

# Step 11: Dispatch Shipment
test_step "STEP 11" "Dispatch Shipment (Fulfillment Service)"

# Generate tracking number
if command -v powershell.exe &> /dev/null; then
    tracking_suffix=$(powershell.exe -Command "[guid]::NewGuid().ToString().Substring(0,8)" | tr -d '\r')
else
    tracking_suffix=$(date +%s | tail -c 8)
fi

dispatch_body="{
  \"carrier\": \"FedEx\",
  \"trackingNumber\": \"TRACK-$tracking_suffix\"
}"

api_call POST "$BASE_URL:8086/api/fulfillment/shipments/$shipment_id/dispatch" "$dispatch_body" "Shipment dispatched successfully"

# Test Summary
test_header "Test Summary"

total=$((TESTS_PASSED + TESTS_FAILED))
echo -e "\n${WHITE}Total Tests: $total${NC}"
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}✓ ALL TESTS PASSED - Happy path complete!${NC}"
    exit 0
else
    echo -e "\n${RED}✗ SOME TESTS FAILED - Please check the errors above${NC}"
    exit 1
fi

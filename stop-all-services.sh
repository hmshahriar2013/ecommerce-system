#!/bin/bash
# Stop All Services Script (Bash version)
# Detects and stops Java processes listening on service ports 8080-8087

set -e

# ANSI color codes
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
RED='\033[0;31m'
WHITE='\033[1;37m'
GRAY='\033[0;90m'
NC='\033[0m' # No Color

echo -e "${CYAN}=================================================================="
echo -e "  Stopping All Services"
echo -e "==================================================================${NC}"
echo ""

# Service ports
SERVICE_PORTS=(8080 8081 8082 8083 8084 8085 8086 8087)
SERVICE_NAMES=(
    "User Access"
    "Catalog"
    "Pricing"
    "Cart"
    "Orders"
    "Payments"
    "Fulfillment"
    "Inventory"
)

# Array to store PIDs to kill
declare -a PIDS_TO_KILL

# Detect OS
OS_TYPE="unknown"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS_TYPE="linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS_TYPE="mac"
elif [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    OS_TYPE="windows"
fi

echo -e "${YELLOW}Detecting running services...${NC}"
echo ""

# Function to find PIDs listening on a port
find_pid_by_port() {
    local port=$1
    local pid=""
    
    if [[ "$OS_TYPE" == "linux" ]]; then
        # Linux: use lsof or ss
        if command -v lsof &> /dev/null; then
            pid=$(lsof -ti:$port 2>/dev/null || true)
        elif command -v ss &> /dev/null; then
            pid=$(ss -tlnp 2>/dev/null | grep ":$port " | grep -oP 'pid=\K[0-9]+' | head -1 || true)
        fi
    elif [[ "$OS_TYPE" == "mac" ]]; then
        # macOS: use lsof
        if command -v lsof &> /dev/null; then
            pid=$(lsof -ti:$port 2>/dev/null || true)
        fi
    elif [[ "$OS_TYPE" == "windows" ]]; then
        # Windows (Git Bash/MSYS): use netstat
        pid=$(netstat -ano | grep ":$port " | grep LISTENING | awk '{print $5}' | head -1 || true)
    fi
    
    echo "$pid"
}

# Scan all service ports
for i in "${!SERVICE_PORTS[@]}"; do
    port=${SERVICE_PORTS[$i]}
    service_name=${SERVICE_NAMES[$i]}
    
    pid=$(find_pid_by_port $port)
    
    if [[ -n "$pid" ]]; then
        echo -e "  ${WHITE}• Found: $service_name (Port $port, PID: $pid)${NC}"
        PIDS_TO_KILL+=("$pid:$service_name:$port")
    fi
done

echo ""

# Check if any services found
if [[ ${#PIDS_TO_KILL[@]} -eq 0 ]]; then
    echo -e "${YELLOW}No running services found.${NC}"
    echo ""
    echo -e "${GRAY}Note: This script detects Java processes on ports 8080-8087${NC}"
    exit 0
fi

# Confirm stop
echo -e "${YELLOW}Found ${#PIDS_TO_KILL[@]} running service(s).${NC}"
echo ""
read -p "Stop all services? (y/N): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Cancelled.${NC}"
    exit 0
fi

echo ""
echo -e "${CYAN}Stopping services...${NC}"

# Kill each process
for entry in "${PIDS_TO_KILL[@]}"; do
    IFS=':' read -r pid service_name port <<< "$entry"
    
    echo -e "  ${WHITE}Stopping: $service_name (PID: $pid)...${NC}"
    
    if kill -9 "$pid" 2>/dev/null; then
        echo -e "    ${GREEN}✓ Stopped${NC}"
    else
        echo -e "    ${RED}✗ Failed (process may have already exited)${NC}"
    fi
done

echo ""
echo -e "${GREEN}All services stopped.${NC}"
echo ""

# Wait a moment and verify
sleep 1
echo -e "${CYAN}Verifying services stopped...${NC}"

ANY_STILL_RUNNING=false
for i in "${!SERVICE_PORTS[@]}"; do
    port=${SERVICE_PORTS[$i]}
    service_name=${SERVICE_NAMES[$i]}
    
    pid=$(find_pid_by_port $port)
    
    if [[ -n "$pid" ]]; then
        echo -e "  ${RED}✗ $service_name still running on port $port (PID: $pid)${NC}"
        ANY_STILL_RUNNING=true
    fi
done

if [[ "$ANY_STILL_RUNNING" == false ]]; then
    echo -e "  ${GREEN}✓ All services confirmed stopped${NC}"
fi

echo ""

#!/bin/bash

# =============================================================================
# check-service-status.sh - Check status of all services
# =============================================================================

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

check_service() {
    local name=$1
    local url=$2
    
    if command -v curl &> /dev/null; then
        if curl -s -f --max-time 2 "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓${NC} $name: ${GREEN}UP${NC}"
            return 0
        else
            echo -e "${RED}✗${NC} $name: ${RED}DOWN${NC}"
            return 1
        fi
    elif command -v wget &> /dev/null; then
        if wget -q --timeout=2 --spider "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓${NC} $name: ${GREEN}UP${NC}"
            return 0
        else
            echo -e "${RED}✗${NC} $name: ${RED}DOWN${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}?${NC} $name: ${YELLOW}UNKNOWN${NC} (curl/wget not available)"
        return 1
    fi
}

echo -e "${BLUE}==========================================${NC}"
echo -e "${BLUE}Service Status Check${NC}"
echo -e "${BLUE}==========================================${NC}"
echo ""

check_service "Authority-A" "http://localhost:8080/actuator/health"
check_service "Authority-B Gateway" "http://localhost:8090/actuator/health"
check_service "Authority-B Data" "http://localhost:8091/actuator/health"
check_service "Authority-B Rewards" "http://localhost:8092/actuator/health"
check_service "Authority-C Gateway" "http://localhost:8100/healthz"
check_service "Authority-C Data" "http://localhost:8101/actuator/health"
check_service "Authority-C Rewards" "http://localhost:8102/actuator/health"
check_service "Authority-D Gateway" "http://localhost:8110/api/health"
check_service "Authority-D Data" "http://localhost:8111/actuator/health"
check_service "Authority-D Rewards" "http://localhost:8112/actuator/health"
check_service "Authority-E Gateway" "http://localhost:8120/actuator/health"
check_service "Authority-E Data" "http://localhost:8121/actuator/health"
check_service "Authority-E Rewards" "http://localhost:8122/actuator/health"
check_service "Auth Service" "http://localhost:8083/actuator/health"
check_service "Frontend" "http://localhost:5173"

echo ""


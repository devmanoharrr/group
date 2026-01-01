#!/bin/bash

# =============================================================================
# test-all-services.sh - Test all services and endpoints
# =============================================================================
#
# This script tests all services to verify they are running and responding
# correctly. It tests:
# - Auth Service (port 8083)
# - Authority-A (port 8080)
# - Authority-B (ports 8090, 8091, 8092)
# - Authority-C (ports 8100, 8101, 8102)
# - Authority-D (ports 8110, 8111, 8112)
# - Authority-E (ports 8120, 8121, 8122)
#
# =============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test results
PASSED=0
FAILED=0

# Function to test an endpoint
test_endpoint() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    local expected_status=${5:-200}
    
    echo -e "${BLUE}Testing: ${name}${NC}"
    echo "  ${method} ${url}"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X ${method} "${url}" \
            -H "Content-Type: application/json" \
            -d "${data}" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X ${method} "${url}" 2>&1)
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq "$expected_status" ] || [ "$http_code" -eq "200" ] || [ "$http_code" -eq "201" ]; then
        echo -e "  ${GREEN}✓ PASSED${NC} (HTTP $http_code)"
        ((PASSED++))
        if [ -n "$body" ] && [ ${#body} -lt 500 ]; then
            echo "  Response: ${body:0:200}..."
        fi
        return 0
    else
        echo -e "  ${RED}✗ FAILED${NC} (HTTP $http_code, expected $expected_status)"
        echo "  Response: ${body:0:200}..."
        ((FAILED++))
        return 1
    fi
}

# Function to check if a service is running
check_service() {
    local name=$1
    local port=$2
    
    if lsof -Pi :${port} -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${GREEN}✓${NC} ${name} is running on port ${port}"
        return 0
    else
        echo -e "${RED}✗${NC} ${name} is NOT running on port ${port}"
        return 1
    fi
}

echo "=========================================="
echo "Testing All Services"
echo "=========================================="
echo ""

# Check if services are running
echo "=== Checking Service Status ==="
check_service "Auth Service" 8083
check_service "Authority-A" 8080
check_service "Authority-B Gateway" 8090
check_service "Authority-B Data" 8091
check_service "Authority-B Rewards" 8092
check_service "Authority-C Gateway" 8100
check_service "Authority-C Data" 8101
check_service "Authority-C Rewards" 8102
check_service "Authority-D Gateway" 8110
check_service "Authority-D Data" 8111
check_service "Authority-D Rewards" 8112
check_service "Authority-E Gateway" 8120
check_service "Authority-E Data" 8121
check_service "Authority-E Rewards" 8122
echo ""

# Test Auth Service
echo "=== Testing Auth Service (Port 8083) ==="
test_endpoint "Health Check" "GET" "http://localhost:8083/actuator/health" "" 200 || true
test_endpoint "Register User" "POST" "http://localhost:8083/auth/register" \
    '{"email":"test@example.com","password":"password123","name":"Test User"}' 200 || true
test_endpoint "Login" "POST" "http://localhost:8083/auth/login" \
    '{"email":"test@example.com","password":"password123"}' 200 || true
echo ""

# Test Authority-A
echo "=== Testing Authority-A (Port 8080) ==="
test_endpoint "Home" "GET" "http://localhost:8080/" "" 200 || true
test_endpoint "Health" "GET" "http://localhost:8080/health" "" 200 || true
test_endpoint "Get All Observations" "GET" "http://localhost:8080/api/crowdsourced/all" "" 200 || true
echo ""

# Test Authority-B
echo "=== Testing Authority-B (Port 8090) ==="
test_endpoint "Gateway Health" "GET" "http://localhost:8090/actuator/health" "" 200 || true
test_endpoint "Data Service Health" "GET" "http://localhost:8091/actuator/health" "" 200 || true
test_endpoint "Rewards Service Health" "GET" "http://localhost:8092/actuator/health" "" 200 || true
test_endpoint "Get Observations via Gateway" "GET" "http://localhost:8090/data/observations" "" 200 || true
test_endpoint "Get Stats via Gateway" "GET" "http://localhost:8090/data/stats" "" 200 || true
test_endpoint "Get Leaderboard via Gateway" "GET" "http://localhost:8090/rewards/leaderboard" "" 200 || true
echo ""

# Test Authority-C
echo "=== Testing Authority-C (Port 8100) ==="
test_endpoint "Gateway Health" "GET" "http://localhost:8100/healthz" "" 200 || true
test_endpoint "Data Service Health" "GET" "http://localhost:8101/healthz" "" 200 || true
test_endpoint "Rewards Service Health" "GET" "http://localhost:8102/healthz" "" 200 || true
test_endpoint "Get Latest Observations" "GET" "http://localhost:8100/api/data/observations/latest?limit=5" "" 200 || true
test_endpoint "Get Observations Count" "GET" "http://localhost:8100/api/data/observations/count" "" 200 || true
test_endpoint "Get Leaderboard" "GET" "http://localhost:8100/api/rewards/leaderboard" "" 200 || true
echo ""

# Test Authority-D
echo "=== Testing Authority-D (Port 8110) ==="
test_endpoint "Gateway Health" "GET" "http://localhost:8110/health" "" 200 || true
test_endpoint "Data Service Health" "GET" "http://localhost:8111/actuator/health" "" 200 || true
test_endpoint "Rewards Service Health" "GET" "http://localhost:8112/actuator/health" "" 200 || true
test_endpoint "List Observations" "GET" "http://localhost:8110/api/observations" "" 200 || true
echo ""

# Test Authority-E
echo "=== Testing Authority-E (Port 8120) ==="
test_endpoint "Gateway Health" "GET" "http://localhost:8120/actuator/health" "" 200 || true
test_endpoint "Data Service Health" "GET" "http://localhost:8121/actuator/health" "" 200 || true
test_endpoint "Rewards Service Health" "GET" "http://localhost:8122/actuator/health" "" 200 || true
test_endpoint "Get Observations" "GET" "http://localhost:8120/api/crowdsourced/observations" "" 200 || true
test_endpoint "Get Rewards" "GET" "http://localhost:8120/api/rewards/rewards" "" 200 || true
echo ""

# Summary
echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: ${PASSED}${NC}"
echo -e "${RED}Failed: ${FAILED}${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${YELLOW}Some tests failed. Check the output above.${NC}"
    exit 1
fi


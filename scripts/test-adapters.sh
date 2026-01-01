#!/bin/bash

# Test script for API adapter endpoints
# Tests the standardized contract endpoints for all authorities

set -e

echo "=========================================="
echo "Testing API Adapter Endpoints"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASSED=0
FAILED=0

test_endpoint() {
    local name=$1
    local url=$2
    local expected_status=${3:-200}
    
    echo -n "Testing: $name ... "
    # Use separate files for body and HTTP code to avoid issues with chunked encoding
    temp_file=$(mktemp)
    code_file=$(mktemp)
    # Write HTTP code to separate file, body to temp_file
    # Use --no-buffer to handle chunked encoding, ignore exit code
    curl -s --no-buffer -w "%{http_code}" -o "$temp_file" "$url" > "$code_file" 2>/dev/null || true
    # Read HTTP code from code_file
    http_code=$(cat "$code_file" 2>/dev/null | grep -oE '[0-9]{3}' | tail -1 || echo "000")
    # Fallback: try without --no-buffer if we didn't get a code
    if [ "$http_code" = "000" ] || [ -z "$http_code" ]; then
        curl -s -w "%{http_code}" -o "$temp_file" "$url" > "$code_file" 2>/dev/null || true
        http_code=$(cat "$code_file" 2>/dev/null | grep -oE '[0-9]{3}' | tail -1 || echo "000")
    fi
    body=$(cat "$temp_file" 2>/dev/null || echo "")
    rm -f "$temp_file" "$code_file"
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (HTTP $http_code)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC} (HTTP $http_code, expected $expected_status)"
        echo "  URL: $url"
        if [ ! -z "$body" ]; then
            echo "  Response: $(echo "$body" | head -c 200)"
        fi
        echo ""
        ((FAILED++))
        return 1
    fi
}

# Test Authority-A
echo "=== Testing Authority-A (Port 8080) ==="
test_endpoint "Observations Count" "http://localhost:8080/api/observations/count"
test_endpoint "Recent Observations" "http://localhost:8080/api/observations/recent?limit=5"
test_endpoint "Leaderboard" "http://localhost:8080/api/rewards/leaderboard?limit=3"
echo ""

# Test Authority-B
echo "=== Testing Authority-B (Port 8090) ==="
test_endpoint "Observations Count" "http://localhost:8090/api/observations/count"
test_endpoint "Recent Observations" "http://localhost:8090/api/observations/recent?limit=5"
test_endpoint "Leaderboard" "http://localhost:8090/api/rewards/leaderboard?limit=3"
echo ""

# Test Authority-C
echo "=== Testing Authority-C (Port 8100) ==="
test_endpoint "Observations Count" "http://localhost:8100/api/observations/count"
test_endpoint "Recent Observations" "http://localhost:8100/api/observations/recent?limit=5"
test_endpoint "Leaderboard" "http://localhost:8100/api/rewards/leaderboard?authority=authority-c&limit=3"
echo ""

# Test Authority-D
echo "=== Testing Authority-D (Port 8110) ==="
test_endpoint "Observations Count" "http://localhost:8110/api/observations/count"
test_endpoint "Recent Observations" "http://localhost:8110/api/observations/recent?limit=5"
test_endpoint "Leaderboard" "http://localhost:8110/api/rewards/leaderboard?limit=3"
echo ""

# Test Authority-E
echo "=== Testing Authority-E (Port 8120) ==="
test_endpoint "Observations Count" "http://localhost:8120/api/observations/count"
test_endpoint "Recent Observations" "http://localhost:8120/api/observations/recent?limit=5"
test_endpoint "Leaderboard" "http://localhost:8120/api/rewards/leaderboard?limit=3"
echo ""

# Summary
echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All adapter endpoints are working! ✓${NC}"
    exit 0
else
    echo -e "${YELLOW}Some tests failed. Services may need to be restarted to pick up new code.${NC}"
    exit 1
fi


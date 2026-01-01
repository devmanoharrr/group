#!/bin/bash

# =============================================================================
# run-all-tests.sh - Run all backend tests
# =============================================================================
#
# This script runs Maven tests for all Spring Boot modules.
#
# Usage: ./scripts/run-all-tests.sh
#

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PASSED=0
FAILED=0

log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✓${NC} $1"
    ((PASSED++))
}

log_error() {
    echo -e "${RED}✗${NC} $1"
    ((FAILED++))
}

log_warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

log "=========================================="
log "Running All Backend Tests"
log "=========================================="
log ""

# Test Auth Service
log "Testing auth-service..."
cd auth-service
if mvn test -q > /dev/null 2>&1; then
    log_success "auth-service tests passed"
else
    log_error "auth-service tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-A
log "Testing Authority-A..."
cd authority-a/CitizenScienceWaterQualityApplication
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-A tests passed"
else
    log_error "Authority-A tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-B (Gateway)
log "Testing Authority-B Gateway..."
cd authority-b/water-quality-monitoringfinal/api-gateway
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-B Gateway tests passed"
else
    log_error "Authority-B Gateway tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-B (Data Service)
log "Testing Authority-B Data Service..."
cd authority-b/water-quality-monitoringfinal/crowdsourced-data-service
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-B Data Service tests passed"
else
    log_error "Authority-B Data Service tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-B (Rewards Service)
log "Testing Authority-B Rewards Service..."
cd authority-b/water-quality-monitoringfinal/rewards-service
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-B Rewards Service tests passed"
else
    log_error "Authority-B Rewards Service tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-C (Gateway)
log "Testing Authority-C Gateway..."
cd authority-c/water-quality-service/api-gateway
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-C Gateway tests passed"
else
    log_error "Authority-C Gateway tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-C (Data Service)
log "Testing Authority-C Data Service..."
cd authority-c/water-quality-service/data-service
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-C Data Service tests passed"
else
    log_error "Authority-C Data Service tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-D (Gateway)
log "Testing Authority-D Gateway..."
cd authority-d/WaterQuality/Gateway
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-D Gateway tests passed"
else
    log_error "Authority-D Gateway tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-D (CrowdsourcedDataService)
log "Testing Authority-D CrowdsourcedDataService..."
cd authority-d/WaterQuality/CrowdsourcedDataService
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-D CrowdsourcedDataService tests passed"
else
    log_error "Authority-D CrowdsourcedDataService tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-D (RewardsService)
log "Testing Authority-D RewardsService..."
cd authority-d/WaterQuality/RewardsService
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-D RewardsService tests passed"
else
    log_error "Authority-D RewardsService tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-E (Gateway)
log "Testing Authority-E Gateway..."
cd authority-e/KF7014_IndividualComponent_24036443/api-gateway
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-E Gateway tests passed"
else
    log_error "Authority-E Gateway tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-E (CrowdsourcedDataService)
log "Testing Authority-E CrowdsourcedDataService..."
cd authority-e/KF7014_IndividualComponent_24036443/crowdsourced-data-service
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-E CrowdsourcedDataService tests passed"
else
    log_error "Authority-E CrowdsourcedDataService tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Test Authority-E (RewardsService)
log "Testing Authority-E RewardsService..."
cd authority-e/KF7014_IndividualComponent_24036443/rewards-service
if mvn test -q > /dev/null 2>&1; then
    log_success "Authority-E RewardsService tests passed"
else
    log_error "Authority-E RewardsService tests failed"
    mvn test 2>&1 | tail -20
fi
cd "$PROJECT_ROOT"
echo ""

# Summary
log ""
log "=========================================="
log "Test Summary"
log "=========================================="
log_success "Passed: $PASSED"
if [ $FAILED -gt 0 ]; then
    log_error "Failed: $FAILED"
    exit 1
else
    log_success "All tests passed!"
    exit 0
fi


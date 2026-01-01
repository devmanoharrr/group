#!/bin/bash

# =============================================================================
# run-all.sh - Start all microservices for Water Quality Monitoring Project
# =============================================================================
#
# This script starts all services in the correct order:
# 1. Data services (SQLite/MongoDB)
# 2. Rewards services
# 3. API Gateways
# 4. Authority-A (monolithic)
# 5. Auth service
# 6. Frontend
#
# Port Mapping Strategy (to avoid conflicts):
# - Authority-A: 8080 (monolithic)
# - Authority-B: 8090/8091/8092
# - Authority-C: 8100/8101/8102
# - Authority-D: 8110/8111/8112
# - Authority-E: 8120/8121/8122
# - Auth Service: 8083
# - Frontend: 5173
#
# Note: Port configuration files have been updated in each service's
# application.yml or application.properties to use the mapped ports.
#
# =============================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get the project root directory (parent of scripts/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PID_FILE="$PROJECT_ROOT/scripts/.service-pids"

# Create PID file if it doesn't exist
touch "$PID_FILE"

# Function to log messages
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✓${NC} $1"
}

log_error() {
    echo -e "${RED}✗${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to start a service and store its PID
start_service() {
    local service_name=$1
    local working_dir=$2
    local command=$3
    local port=$4
    
    if [ -z "$working_dir" ] || [ ! -d "$working_dir" ]; then
        log_warn "Skipping $service_name (directory not found: $working_dir)"
        return 1
    fi
    
    if [ -n "$port" ] && check_port "$port"; then
        log_warn "$service_name appears to be already running on port $port"
        return 1
    fi
    
    log "Starting $service_name..."
    cd "$working_dir" || return 1
    
    # Run the command in background and capture PID
    eval "$command" > "$PROJECT_ROOT/scripts/logs/${service_name}.log" 2>&1 &
    local pid=$!
    echo "$pid|$service_name|$port" >> "$PID_FILE"
    
    # Wait a moment to check if process started successfully
    sleep 2
    if ps -p $pid > /dev/null 2>&1; then
        log_success "$service_name started (PID: $pid, Port: ${port:-N/A})"
        return 0
    else
        log_error "$service_name failed to start. Check logs/${service_name}.log"
        return 1
    fi
}

# Create logs directory
mkdir -p "$PROJECT_ROOT/scripts/logs"

log "=========================================="
log "Starting Water Quality Monitoring Services"
log "=========================================="
log "Project root: $PROJECT_ROOT"
log ""

# Check prerequisites
if ! command -v mvn &> /dev/null; then
    log_error "Maven (mvn) is not installed or not in PATH"
    exit 1
fi

# Check if MongoDB is needed (Authority-D)
log "Checking MongoDB status for Authority-D..."
if ! pgrep -x mongod > /dev/null; then
    log_warn "MongoDB does not appear to be running. Authority-D services may fail."
    log_warn "Start MongoDB with: mongod --dbpath <path-to-data>"
fi

log ""
log "Starting services in order: Data → Rewards → Gateways → Auth → Frontend"
log ""

# =============================================================================
# Authority-B: water-quality-monitoringfinal
# =============================================================================
log "--- Starting Authority-B Services ---"

# Authority-B: Crowdsourced Data Service (8091)
start_service \
    "authority-b-data" \
    "$PROJECT_ROOT/authority-b/water-quality-monitoringfinal/crowdsourced-data-service" \
    "mvn spring-boot:run" \
    "8091"

sleep 3

# Authority-B: Rewards Service (8092)
start_service \
    "authority-b-rewards" \
    "$PROJECT_ROOT/authority-b/water-quality-monitoringfinal/rewards-service" \
    "mvn spring-boot:run" \
    "8092"

sleep 3

# Authority-B: API Gateway (8090)
start_service \
    "authority-b-gateway" \
    "$PROJECT_ROOT/authority-b/water-quality-monitoringfinal/api-gateway" \
    "mvn spring-boot:run" \
    "8090"

sleep 5

# =============================================================================
# Authority-C: water-quality-service
# =============================================================================
log ""
log "--- Starting Authority-C Services ---"

# Authority-C: Data Service (8101)
start_service \
    "authority-c-data" \
    "$PROJECT_ROOT/authority-c/water-quality-service/data-service" \
    "mvn spring-boot:run" \
    "8101"

sleep 3

# Authority-C: Rewards Service (8102)
start_service \
    "authority-c-rewards" \
    "$PROJECT_ROOT/authority-c/water-quality-service/rewards-service" \
    "mvn spring-boot:run" \
    "8102"

sleep 3

# Authority-C: API Gateway (8100)
start_service \
    "authority-c-gateway" \
    "$PROJECT_ROOT/authority-c/water-quality-service/api-gateway" \
    "mvn spring-boot:run" \
    "8100"

sleep 5

# =============================================================================
# Authority-D: WaterQuality (MongoDB)
# =============================================================================
log ""
log "--- Starting Authority-D Services ---"

# Authority-D: CrowdsourcedDataService (8111)
start_service \
    "authority-d-data" \
    "$PROJECT_ROOT/authority-d/WaterQuality/CrowdsourcedDataService" \
    "mvn spring-boot:run" \
    "8111"

sleep 3

# Authority-D: RewardsService (8112)
start_service \
    "authority-d-rewards" \
    "$PROJECT_ROOT/authority-d/WaterQuality/RewardsService" \
    "mvn spring-boot:run" \
    "8112"

sleep 3

# Authority-D: Gateway (8110)
start_service \
    "authority-d-gateway" \
    "$PROJECT_ROOT/authority-d/WaterQuality/Gateway" \
    "mvn spring-boot:run" \
    "8110"

sleep 5

# =============================================================================
# Authority-E: KF7014_IndividualComponent_24036443
# =============================================================================
log ""
log "--- Starting Authority-E Services ---"

# Authority-E: Crowdsourced Data Service (8121)
start_service \
    "authority-e-data" \
    "$PROJECT_ROOT/authority-e/KF7014_IndividualComponent_24036443/crowdsourced-data-service" \
    "mvn spring-boot:run" \
    "8121"

sleep 3

# Authority-E: Rewards Service (8122)
start_service \
    "authority-e-rewards" \
    "$PROJECT_ROOT/authority-e/KF7014_IndividualComponent_24036443/rewards-service" \
    "mvn spring-boot:run" \
    "8122"

sleep 3

# Authority-E: API Gateway (8120)
start_service \
    "authority-e-gateway" \
    "$PROJECT_ROOT/authority-e/KF7014_IndividualComponent_24036443/api-gateway" \
    "mvn spring-boot:run" \
    "8120"

sleep 5

# =============================================================================
# Authority-A: Monolithic (8080)
# =============================================================================
log ""
log "--- Starting Authority-A (Monolithic) ---"

start_service \
    "authority-a" \
    "$PROJECT_ROOT/authority-a/CitizenScienceWaterQualityApplication" \
    "mvn spring-boot:run" \
    "8080"

sleep 5

# =============================================================================
# Auth Service (8083)
# =============================================================================
log ""
log "--- Starting Auth Service ---"

if [ -d "$PROJECT_ROOT/auth-service" ] && [ -f "$PROJECT_ROOT/auth-service/pom.xml" ]; then
    start_service \
        "auth-service" \
        "$PROJECT_ROOT/auth-service" \
        "mvn spring-boot:run" \
        "8083"
else
    log_warn "Auth service not found. Skipping. (Will be created in Step 2)"
fi

sleep 3

# =============================================================================
# Frontend (5173)
# =============================================================================
log ""
log "--- Starting Frontend ---"

if [ -d "$PROJECT_ROOT/frontend" ] && [ -f "$PROJECT_ROOT/frontend/package.json" ]; then
    # Check if node_modules exists, if not run npm install
    if [ ! -d "$PROJECT_ROOT/frontend/node_modules" ]; then
        log "Installing frontend dependencies..."
        cd "$PROJECT_ROOT/frontend" && npm install
    fi
    
    start_service \
        "frontend" \
        "$PROJECT_ROOT/frontend" \
        "npm run dev" \
        "5173"
else
    log_warn "Frontend not found. Skipping. (Will be created in Step 4)"
fi

# =============================================================================
# Summary
# =============================================================================
log ""
log "=========================================="
log "Service Startup Complete"
log "=========================================="
log ""
log "Services started. PIDs stored in: $PID_FILE"
log ""
log "To view logs:"
log "  tail -f scripts/logs/<service-name>.log"
log ""
log "To stop all services:"
log "  ./scripts/stop-all.sh"
log ""
log "Service URLs:"
log "  Authority-A:  http://localhost:8080"
log "  Authority-B: http://localhost:8090"
log "  Authority-C: http://localhost:8100"
log "  Authority-D: http://localhost:8110"
log "  Authority-E: http://localhost:8120"
log "  Auth Service: http://localhost:8083 (if started)"
log "  Frontend:     http://localhost:5173 (if started)"
log ""


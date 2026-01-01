#!/bin/bash

# =============================================================================
# stop-all.sh - Stop all microservices for Water Quality Monitoring Project
# =============================================================================
#
# This script stops all services that were started by run-all.sh
# It reads the PID file and gracefully stops each service.
#
# =============================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get the project root directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PID_FILE="$PROJECT_ROOT/scripts/.service-pids"

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

# Function to stop a service by PID
stop_service() {
    local pid=$1
    local service_name=$2
    local port=$3
    
    if [ -z "$pid" ]; then
        return 1
    fi
    
    # Check if process is still running
    if ps -p $pid > /dev/null 2>&1; then
        log "Stopping $service_name (PID: $pid)..."
        
        # Try graceful shutdown first (SIGTERM)
        kill $pid 2>/dev/null
        
        # Wait up to 10 seconds for graceful shutdown
        local count=0
        while ps -p $pid > /dev/null 2>&1 && [ $count -lt 10 ]; do
            sleep 1
            count=$((count + 1))
        done
        
        # Force kill if still running
        if ps -p $pid > /dev/null 2>&1; then
            log_warn "$service_name did not stop gracefully, forcing shutdown..."
            kill -9 $pid 2>/dev/null
            sleep 1
        fi
        
        if ! ps -p $pid > /dev/null 2>&1; then
            log_success "$service_name stopped"
            return 0
        else
            log_error "Failed to stop $service_name (PID: $pid)"
            return 1
        fi
    else
        log_warn "$service_name (PID: $pid) is not running"
        return 1
    fi
}

log "=========================================="
log "Stopping Water Quality Monitoring Services"
log "=========================================="
log ""

# Check if PID file exists
if [ ! -f "$PID_FILE" ]; then
    log_warn "PID file not found: $PID_FILE"
    log_warn "No services appear to be running via run-all.sh"
    log ""
    log "Attempting to find and stop services by port..."
    
    # Try to find and stop services by known ports
    PORTS=(8080 8083 5173 8090 8091 8092 8100 8101 8102 8110 8111 8112 8120 8121 8122)
    
    for port in "${PORTS[@]}"; do
        if command -v lsof &> /dev/null; then
            PID=$(lsof -ti :$port 2>/dev/null)
            if [ -n "$PID" ]; then
                log "Found process on port $port (PID: $PID), stopping..."
                kill $PID 2>/dev/null || kill -9 $PID 2>/dev/null
                sleep 1
            fi
        fi
    done
    
    log ""
    log "Done. Some services may still be running if they weren't started by run-all.sh"
    exit 0
fi

# Read PID file and stop services in reverse order
# (Stop frontend/auth first, then gateways, then services)
log "Reading PID file: $PID_FILE"
log ""

# Count total services
TOTAL_SERVICES=$(wc -l < "$PID_FILE" | tr -d ' ')
STOPPED_COUNT=0

# Read PIDs and stop services
while IFS='|' read -r pid service_name port; do
    if [ -n "$pid" ] && [ "$pid" != "" ]; then
        stop_service "$pid" "$service_name" "$port"
        if [ $? -eq 0 ]; then
            STOPPED_COUNT=$((STOPPED_COUNT + 1))
        fi
        sleep 1
    fi
done < "$PID_FILE"

# Clear PID file
> "$PID_FILE"

log ""
log "=========================================="
log "Service Shutdown Complete"
log "=========================================="
log ""
log "Stopped $STOPPED_COUNT out of $TOTAL_SERVICES services"
log ""

# Final check for any remaining processes on known ports
log "Checking for any remaining services on known ports..."
REMAINING=0

PORTS=(8080 8083 5173 8090 8091 8092 8100 8101 8102 8110 8111 8112 8120 8121 8122)
for port in "${PORTS[@]}"; do
    if command -v lsof &> /dev/null; then
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            log_warn "Port $port is still in use"
            REMAINING=$((REMAINING + 1))
        fi
    fi
done

if [ $REMAINING -eq 0 ]; then
    log_success "All services stopped successfully"
else
    log_warn "$REMAINING port(s) still in use. You may need to stop services manually."
fi

log ""


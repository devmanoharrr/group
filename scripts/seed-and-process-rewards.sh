#!/bin/bash

# =============================================================================
# seed-and-process-rewards.sh - Trigger rewards processing
# =============================================================================
#
# This script calls rewards processing endpoints after services have started
# to ensure seed data is processed and leaderboards are populated.
#
# Usage: ./scripts/seed-and-process-rewards.sh
#

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✓${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

log "=========================================="
log "Processing Rewards for Seed Data"
log "=========================================="
log ""

# Wait for services to be ready
log "Waiting for services to be ready..."
sleep 10

# Process rewards for Authority-A (monolithic)
log "Processing rewards for Authority-A..."
if curl -s -f --max-time 5 "http://localhost:8080/api/rewards/processAll" > /dev/null 2>&1; then
    log_success "Authority-A rewards processed"
else
    log_warn "Authority-A rewards processing failed or endpoint not available"
fi

# Process rewards for Authority-B
log "Processing rewards for Authority-B..."
if curl -s -f --max-time 5 -X POST "http://localhost:8090/rewards/process" > /dev/null 2>&1; then
    log_success "Authority-B rewards processed"
else
    log_warn "Authority-B rewards processing failed or endpoint not available"
fi

# Process rewards for Authority-C
log "Processing rewards for Authority-C..."
if curl -s -f --max-time 5 -X POST "http://localhost:8100/rewards/process" > /dev/null 2>&1; then
    log_success "Authority-C rewards processed"
else
    log_warn "Authority-C rewards processing failed or endpoint not available"
fi

# Process rewards for Authority-D
log "Processing rewards for Authority-D..."
# Authority-D may need individual citizen recompute or automatic processing
if curl -s -f --max-time 5 -X POST "http://localhost:8110/api/rewards/recompute/citizen-001" > /dev/null 2>&1; then
    log_success "Authority-D rewards processed (citizen-001)"
fi
if curl -s -f --max-time 5 -X POST "http://localhost:8110/api/rewards/recompute/citizen-002" > /dev/null 2>&1; then
    log_success "Authority-D rewards processed (citizen-002)"
fi
if curl -s -f --max-time 5 -X POST "http://localhost:8110/api/rewards/recompute/citizen-003" > /dev/null 2>&1; then
    log_success "Authority-D rewards processed (citizen-003)"
fi

# Process rewards for Authority-E
log "Processing rewards for Authority-E..."
if curl -s -f --max-time 5 -X POST "http://localhost:8120/api/rewards/process" > /dev/null 2>&1; then
    log_success "Authority-E rewards processed"
else
    log_warn "Authority-E rewards processing failed or endpoint not available"
fi

log ""
log "=========================================="
log "Rewards Processing Complete"
log "=========================================="
log ""
log "Note: Some services process rewards automatically via scheduled tasks."
log "      Leaderboards may take 30-60 seconds to update."
log ""


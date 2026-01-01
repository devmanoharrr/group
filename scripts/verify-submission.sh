#!/bin/bash

# =============================================================================
# verify-submission.sh - Verify submission package completeness
# =============================================================================
#
# This script runs smoke checks to verify the submission package is complete.
#
# Usage: ./scripts/verify-submission.sh [path-to-submission-dir-or-zip]
#

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"
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

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Determine submission directory
if [ -n "$1" ]; then
    SUBMISSION_PATH="$1"
else
    SUBMISSION_PATH="GroupSubmission"
fi

# Extract zip if needed
EXTRACTED_DIR=""
if [ -f "$SUBMISSION_PATH" ] && [[ "$SUBMISSION_PATH" == *.zip ]]; then
    log "Extracting zip file..."
    EXTRACTED_DIR="/tmp/submission-verify-$$"
    mkdir -p "$EXTRACTED_DIR"
    unzip -q "$SUBMISSION_PATH" -d "$EXTRACTED_DIR" || {
        log_error "Failed to extract zip file"
        exit 1
    }
    SUBMISSION_DIR="$EXTRACTED_DIR/GroupSubmission"
elif [ -d "$SUBMISSION_PATH" ]; then
    SUBMISSION_DIR="$SUBMISSION_PATH"
else
    log_error "Submission path not found: $SUBMISSION_PATH"
    exit 1
fi

if [ ! -d "$SUBMISSION_DIR" ]; then
    log_error "Submission directory not found: $SUBMISSION_DIR"
    exit 1
fi

log "=========================================="
log "Verifying Submission Package"
log "=========================================="
log "Checking: $SUBMISSION_DIR"
log ""

PASSED=0
FAILED=0

check_file() {
    local file="$1"
    local desc="${2:-$file}"
    if [ -f "$SUBMISSION_DIR/$file" ] || [ -d "$SUBMISSION_DIR/$file" ]; then
        log_success "$desc"
        ((PASSED++))
        return 0
    else
        log_error "$desc (MISSING)"
        ((FAILED++))
        return 1
    fi
}

check_build_artifact() {
    local pattern="$1"
    local desc="$2"
    if find "$SUBMISSION_DIR" -name "$pattern" -type f | grep -q .; then
        log_success "$desc"
        ((PASSED++))
        return 0
    else
        log_warn "$desc (not found, may need to build)"
        return 0  # Not critical for verification
    fi
}

# Check required files
log "=== Required Files ==="
check_file "README.md" "README.md"
check_file "notes.txt" "notes.txt"
check_file "scripts/run-all.sh" "scripts/run-all.sh"
check_file "scripts/stop-all.sh" "scripts/stop-all.sh"
check_file "docs" "docs/ directory"
check_file "auth-service" "auth-service/"
check_file "frontend" "frontend/"
check_file "authority-a" "authority-a/"
check_file "authority-b" "authority-b/"
check_file "authority-c" "authority-c/"
check_file "authority-d" "authority-d/"
check_file "authority-e" "authority-e/"
echo ""

# Check documentation files
log "=== Documentation Files ==="
check_file "docs/README.md" "docs/README.md (if exists)"
check_file "docs/demo-credentials.md" "docs/demo-credentials.md"
check_file "docs/demo-script.md" "docs/demo-script.md"
check_file "docs/project-plan.md" "docs/project-plan.md"
check_file "docs/logbook.md" "docs/logbook.md"
check_file "docs/peer-assessment-template.md" "docs/peer-assessment-template.md"
check_file "docs/api-contract.md" "docs/api-contract.md"
echo ""

# Check build artifacts (optional - may need to build)
log "=== Build Artifacts (Optional) ==="
check_build_artifact "*.jar" "JAR files"
check_build_artifact "frontend/dist" "Frontend dist/ directory"
echo ""

# Check source files
log "=== Source Files ==="
check_file "auth-service/src" "auth-service source code"
check_file "frontend/src" "frontend source code"
check_file "authority-a/CitizenScienceWaterQualityApplication/src" "Authority-A source code"
check_file "authority-b/water-quality-monitoringfinal/api-gateway/src" "Authority-B gateway source"
check_file "authority-c/water-quality-service/api-gateway/src" "Authority-C gateway source"
check_file "authority-d/WaterQuality/Gateway/src" "Authority-D gateway source"
check_file "authority-e/KF7014_IndividualComponent_24036443/api-gateway/src" "Authority-E gateway source"
echo ""

# Check configuration files
log "=== Configuration Files ==="
check_file "auth-service/src/main/resources/application.properties" "auth-service config"
check_file "frontend/package.json" "frontend package.json"
check_file "frontend/.env.example" "frontend .env.example"
check_file "authority-a/CitizenScienceWaterQualityApplication/pom.xml" "Authority-A pom.xml"
echo ""

# Validate README structure
log "=== README Validation ==="
if [ -f "$SUBMISSION_DIR/README.md" ]; then
    README="$SUBMISSION_DIR/README.md"
    
    # Check for key sections
    if grep -q "Architecture Overview" "$README"; then
        log_success "README contains 'Architecture Overview'"
        ((PASSED++))
    else
        log_warn "README missing 'Architecture Overview' section"
    fi
    
    if grep -q "Port Assignments" "$README"; then
        log_success "README contains 'Port Assignments'"
        ((PASSED++))
    else
        log_warn "README missing 'Port Assignments' section"
    fi
    
    if grep -q "Run Steps" "$README" || grep -q "Quick Start" "$README"; then
        log_success "README contains run/start instructions"
        ((PASSED++))
    else
        log_warn "README missing run/start instructions"
    fi
    
    if grep -q "auth-service" "$README" && grep -q "frontend" "$README"; then
        log_success "README mentions core services"
        ((PASSED++))
    else
        log_warn "README may be missing service descriptions"
    fi
else
    log_error "README.md not found"
    ((FAILED++))
fi
echo ""

# Check adapter controllers exist
log "=== Adapter Controllers ==="
check_file "authority-a/CitizenScienceWaterQualityApplication/src/main/java/citizen/adapter/AdapterController.java" "Authority-A adapter"
check_file "authority-b/water-quality-monitoringfinal/api-gateway/src/main/java/com/waterquality/gateway/adapter/AdapterController.java" "Authority-B adapter"
check_file "authority-c/water-quality-service/api-gateway/src/main/java/com/bharath/wq/gateway/adapter/AdapterController.java" "Authority-C adapter"
check_file "authority-d/WaterQuality/Gateway/src/main/java/kf7014/gateway/adapter/AdapterController.java" "Authority-D adapter"
check_file "authority-e/KF7014_IndividualComponent_24036443/api-gateway/src/main/java/com/citizenscience/gateway/adapter/AdapterController.java" "Authority-E adapter"
echo ""

# Check scripts are executable
log "=== Script Permissions ==="
if [ -x "$SUBMISSION_DIR/scripts/run-all.sh" ]; then
    log_success "run-all.sh is executable"
    ((PASSED++))
else
    log_warn "run-all.sh is not executable (may need: chmod +x)"
fi

if [ -x "$SUBMISSION_DIR/scripts/stop-all.sh" ]; then
    log_success "stop-all.sh is executable"
    ((PASSED++))
else
    log_warn "stop-all.sh is not executable"
fi
echo ""

# Summary
log ""
log "=========================================="
log "Verification Summary"
log "=========================================="
log_success "Passed: $PASSED"
if [ $FAILED -gt 0 ]; then
    log_error "Failed: $FAILED"
    log ""
    log "Some required files are missing. Please review and fix before submission."
    exit 1
else
    log_success "All critical checks passed!"
    log ""
    log "Submission package appears complete."
    log "Recommendation: Test on a clean machine if possible."
    exit 0
fi

# Cleanup
if [ -n "$EXTRACTED_DIR" ] && [ -d "$EXTRACTED_DIR" ]; then
    rm -rf "$EXTRACTED_DIR"
fi


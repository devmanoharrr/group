#!/bin/bash

# =============================================================================
# package-submission.sh - Package complete project for submission
# =============================================================================
#
# This script builds all services and creates a submission-ready zip file.
#
# Usage: ./scripts/package-submission.sh
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

SUBMISSION_DIR="GroupSubmission"
ZIP_FILE="GroupSubmission.zip"

log "=========================================="
log "Packaging Project for Submission"
log "=========================================="
log ""

# Clean up any previous submission
if [ -d "$SUBMISSION_DIR" ]; then
    log "Cleaning up previous submission directory..."
    rm -rf "$SUBMISSION_DIR"
fi

if [ -f "$ZIP_FILE" ]; then
    log "Removing previous zip file..."
    rm -f "$ZIP_FILE"
fi

mkdir -p "$SUBMISSION_DIR"

# Build Auth Service
log "Building auth-service..."
cd auth-service
if mvn clean package -DskipTests -q > /dev/null 2>&1; then
    log_success "auth-service built successfully"
else
    log_warn "auth-service build had warnings (continuing...)"
    mvn clean package -DskipTests 2>&1 | tail -10
fi
cd "$PROJECT_ROOT"

# Build Frontend
log "Building frontend..."
cd frontend
if npm run build > /dev/null 2>&1; then
    log_success "frontend built successfully"
else
    log_error "frontend build failed"
    exit 1
fi
cd "$PROJECT_ROOT"

# Build Authority-A
log "Building Authority-A..."
cd authority-a/CitizenScienceWaterQualityApplication
if mvn clean package -DskipTests -q > /dev/null 2>&1; then
    log_success "Authority-A built successfully"
else
    log_warn "Authority-A build had warnings (continuing...)"
    mvn clean package -DskipTests 2>&1 | tail -10
fi
cd "$PROJECT_ROOT"

# Build Authority-B Services
log "Building Authority-B services..."
cd authority-b/water-quality-monitoringfinal/api-gateway
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-B Gateway build warning"
cd "$PROJECT_ROOT"

cd authority-b/water-quality-monitoringfinal/crowdsourced-data-service
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-B Data Service build warning"
cd "$PROJECT_ROOT"

cd authority-b/water-quality-monitoringfinal/rewards-service
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-B Rewards Service build warning"
cd "$PROJECT_ROOT"

log_success "Authority-B services built"

# Build Authority-C Services
log "Building Authority-C services..."
cd authority-c/water-quality-service/api-gateway
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-C Gateway build warning"
cd "$PROJECT_ROOT"

cd authority-c/water-quality-service/data-service
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-C Data Service build warning"
cd "$PROJECT_ROOT"

cd authority-c/water-quality-service/rewards-service
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-C Rewards Service build warning"
cd "$PROJECT_ROOT"

log_success "Authority-C services built"

# Build Authority-D Services
log "Building Authority-D services..."
cd authority-d/WaterQuality/Gateway
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-D Gateway build warning"
cd "$PROJECT_ROOT"

cd authority-d/WaterQuality/CrowdsourcedDataService
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-D Data Service build warning"
cd "$PROJECT_ROOT"

cd authority-d/WaterQuality/RewardsService
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-D Rewards Service build warning"
cd "$PROJECT_ROOT"

log_success "Authority-D services built"

# Build Authority-E Services
log "Building Authority-E services..."
cd authority-e/KF7014_IndividualComponent_24036443/api-gateway
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-E Gateway build warning"
cd "$PROJECT_ROOT"

cd authority-e/KF7014_IndividualComponent_24036443/crowdsourced-data-service
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-E Data Service build warning"
cd "$PROJECT_ROOT"

cd authority-e/KF7014_IndividualComponent_24036443/rewards-service
mvn clean package -DskipTests -q > /dev/null 2>&1 || log_warn "Authority-E Rewards Service build warning"
cd "$PROJECT_ROOT"

log_success "Authority-E services built"

# Copy project structure
log "Copying project files..."

# Copy root files
cp README.md "$SUBMISSION_DIR/" 2>/dev/null || log_warn "README.md not found"
cp notes.txt "$SUBMISSION_DIR/" 2>/dev/null || log_warn "notes.txt not found"
cp docker-compose.yml "$SUBMISSION_DIR/" 2>/dev/null || log_warn "docker-compose.yml not found"

# Copy scripts
log "Copying scripts..."
cp -r scripts "$SUBMISSION_DIR/" 2>/dev/null || log_warn "scripts directory not found"
# Remove log files from scripts directory
rm -rf "$SUBMISSION_DIR/scripts/logs"/*.log 2>/dev/null || true

# Copy docs
log "Copying documentation..."
cp -r docs "$SUBMISSION_DIR/" 2>/dev/null || log_warn "docs directory not found"

# Copy auth-service (source + target)
log "Copying auth-service..."
cp -r auth-service "$SUBMISSION_DIR/" 2>/dev/null || log_warn "auth-service not found"
# Remove unnecessary target files (keep JAR)
find "$SUBMISSION_DIR/auth-service/target" -type f ! -name "*.jar" -delete 2>/dev/null || true

# Copy frontend (source + dist)
log "Copying frontend..."
cp -r frontend "$SUBMISSION_DIR/" 2>/dev/null || log_warn "frontend not found"
# Remove node_modules
rm -rf "$SUBMISSION_DIR/frontend/node_modules" 2>/dev/null || true

# Copy authority-a
log "Copying Authority-A..."
cp -r authority-a "$SUBMISSION_DIR/" 2>/dev/null || log_warn "authority-a not found"
# Clean target directories
find "$SUBMISSION_DIR/authority-a" -type d -name "target" -exec rm -rf {} + 2>/dev/null || true
# Keep database file
# Remove IDE files
rm -rf "$SUBMISSION_DIR/authority-a"/*/nbproject 2>/dev/null || true
rm -f "$SUBMISSION_DIR/authority-a"/*/nbactions.xml 2>/dev/null || true

# Copy authority-b
log "Copying Authority-B..."
cp -r authority-b "$SUBMISSION_DIR/" 2>/dev/null || log_warn "authority-b not found"
# Clean target directories
find "$SUBMISSION_DIR/authority-b" -type d -name "target" -exec rm -rf {} + 2>/dev/null || true
# Keep database file

# Copy authority-c
log "Copying Authority-C..."
cp -r authority-c "$SUBMISSION_DIR/" 2>/dev/null || log_warn "authority-c not found"
# Clean target directories
find "$SUBMISSION_DIR/authority-c" -type d -name "target" -exec rm -rf {} + 2>/dev/null || true
# Keep database file

# Copy authority-d
log "Copying Authority-D..."
cp -r authority-d "$SUBMISSION_DIR/" 2>/dev/null || log_warn "authority-d not found"
# Clean target directories
find "$SUBMISSION_DIR/authority-d" -type d -name "target" -exec rm -rf {} + 2>/dev/null || true

# Copy authority-e
log "Copying Authority-E..."
cp -r authority-e "$SUBMISSION_DIR/" 2>/dev/null || log_warn "authority-e not found"
# Clean target directories
find "$SUBMISSION_DIR/authority-e" -type d -name "target" -exec rm -rf {} + 2>/dev/null || true
# Keep database file

# Create .gitignore if it doesn't exist
if [ ! -f "$SUBMISSION_DIR/.gitignore" ]; then
    cat > "$SUBMISSION_DIR/.gitignore" << 'EOF'
# Build artifacts
target/
dist/
node_modules/
*.jar
*.war
*.ear

# IDE files
.idea/
.vscode/
*.iml
nbproject/
nbactions.xml

# Logs
*.log
logs/

# OS files
.DS_Store
Thumbs.db

# Environment files
.env
.env.local
EOF
fi

# Create submission README
cat > "$SUBMISSION_DIR/SUBMISSION_README.txt" << 'EOF'
Water Quality Monitoring - Group Microservices Project
Submission Package

This package contains the complete integrated application.

QUICK START:
1. Ensure Java 17+, Node.js 18+, and Maven 3.6+ are installed
2. For Authority-D, ensure MongoDB is installed and running
3. Run: ./scripts/run-all.sh
4. Wait 2-5 minutes for all services to start
5. Open browser to: http://localhost:5173
6. Register a new user or use demo credentials (see docs/demo-credentials.md)

STRUCTURE:
- auth-service/          : Authentication service (Spring Boot)
- frontend/              : React frontend application
- authority-a/           : Authority A (Monolithic)
- authority-b/           : Authority B (Microservices)
- authority-c/           : Authority C (Microservices)
- authority-d/           : Authority D (Microservices with MongoDB)
- authority-e/           : Authority E (Microservices)
- scripts/               : Utility scripts (run-all.sh, stop-all.sh, etc.)
- docs/                  : Complete documentation

DOCUMENTATION:
See docs/ directory for:
- README.md              : Main project documentation
- demo-credentials.md    : Demo user information
- demo-script.md         : 20-minute demo script
- project-plan.md        : Sprint planning and backlog
- logbook.md             : Project meeting log
- api-contract.md        : API contract specification

For detailed setup instructions, see README.md in the root directory.
EOF

# Create zip file
log "Creating zip file..."
cd "$PROJECT_ROOT"
if command -v zip &> /dev/null; then
    zip -r "$ZIP_FILE" "$SUBMISSION_DIR" -q
    log_success "Zip file created: $ZIP_FILE"
else
    log_warn "zip command not found. Using tar instead..."
    tar -czf "${ZIP_FILE%.zip}.tar.gz" "$SUBMISSION_DIR"
    log_success "Archive created: ${ZIP_FILE%.zip}.tar.gz"
fi

# Calculate size
if [ -f "$ZIP_FILE" ]; then
    SIZE=$(du -h "$ZIP_FILE" | cut -f1)
    log_success "Submission package size: $SIZE"
elif [ -f "${ZIP_FILE%.zip}.tar.gz" ]; then
    SIZE=$(du -h "${ZIP_FILE%.zip}.tar.gz" | cut -f1)
    log_success "Submission package size: $SIZE"
fi

log ""
log "=========================================="
log "Packaging Complete"
log "=========================================="
log ""
log "Submission package: $ZIP_FILE"
log "Package directory: $SUBMISSION_DIR"
log ""
log "Next steps:"
log "1. Verify package: ./scripts/verify-submission.sh"
log "2. Test on clean machine if possible"
log "3. Submit $ZIP_FILE"
log ""


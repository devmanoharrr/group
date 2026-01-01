# Changes Summary - Step 0.1: Monorepo Structure Setup

## Overview
This document lists all changes made to each member's files and folders during the monorepo integration.

---

## Authority-A (CitizenScienceWaterQualityApplication)

### Folder Location
- **Original**: `/CitizenScienceWaterQualityApplication/`
- **New**: `/authority-a/CitizenScienceWaterQualityApplication/`

### Changes Made
- ✅ **MOVED**: Entire `CitizenScienceWaterQualityApplication/` folder moved to `authority-a/`
- ✅ **NO PORT CHANGES**: Port remains `8080` (no conflicts as it's the only service on this port)
- ✅ **NO CONFIG CHANGES**: No application.properties modifications needed

### Files Modified
- None (moved as-is)

---

## Authority-B (water-quality-monitoringfinal)

### Folder Location
- **Original**: `/water-quality-monitoringfinal/`
- **New**: `/authority-b/water-quality-monitoringfinal/`

### Changes Made
- ✅ **MOVED**: Entire `water-quality-monitoringfinal/` folder moved to `authority-b/`
- ✅ **PORT UPDATES**: All ports changed to avoid conflicts
- ✅ **INTERNAL URL UPDATES**: Gateway routes updated to point to new ports

### Files Modified

#### 1. `api-gateway/src/main/resources/application.yml`
**Changes:**
- `server.port`: `8080` → `8090`
- Gateway route for crowdsourced-data-service: `http://localhost:8081` → `http://localhost:8091`
- Gateway route for rewards-service: `http://localhost:8082` → `http://localhost:8092`

#### 2. `crowdsourced-data-service/src/main/resources/application.properties`
**Changes:**
- `server.port`: `8081` → `8091`

#### 3. `rewards-service/src/main/resources/application.properties`
**Changes:**
- `server.port`: `8082` → `8092`
- `data.service.url`: `http://localhost:8081` → `http://localhost:8091`

---

## Authority-C (water-quality-service)

### Folder Location
- **Original**: `/water-quality-service/`
- **New**: `/authority-c/water-quality-service/`

### Changes Made
- ✅ **MOVED**: Entire `water-quality-service/` folder moved to `authority-c/`
- ✅ **PORT UPDATES**: All ports changed to avoid conflicts
- ✅ **INTERNAL URL UPDATES**: Gateway upstreams and rewards service base URL updated

### Files Modified

#### 1. `api-gateway/src/main/resources/application.yml`
**Changes:**
- `server.port`: `8080` → `8100`
- `upstreams.data`: `http://localhost:8081` → `http://localhost:8101`
- `upstreams.rewards`: `http://localhost:8082` → `http://localhost:8102`

#### 2. `data-service/src/main/resources/application.yml`
**Changes:**
- `server.port`: `8081` → `8101`

#### 3. `rewards-service/src/main/resources/application.yml`
**Changes:**
- `server.port`: `8082` → `8102`
- `rewards.data.base-url`: `http://localhost:8081` → `http://localhost:8101`

---

## Authority-D (WaterQuality)

### Folder Location
- **Original**: `/WaterQuality/`
- **New**: `/authority-d/WaterQuality/`

### Changes Made
- ✅ **MOVED**: Entire `WaterQuality/` folder moved to `authority-d/`
- ✅ **PORT UPDATES**: All ports changed to avoid conflicts
- ✅ **INTERNAL URL UPDATES**: Gateway service URLs and rewards service base URL updated

### Files Modified

#### 1. `Gateway/src/main/resources/application.yml`
**Changes:**
- `server.port`: `8080` → `8110`
- `services.crowdsourced.baseUrl`: `http://localhost:8081` → `http://localhost:8111`
- `services.rewards.baseUrl`: `http://localhost:8082` → `http://localhost:8112`

#### 2. `CrowdsourcedDataService/src/main/resources/application.yml`
**Changes:**
- `server.port`: `8081` → `8111`

#### 3. `RewardsService/src/main/resources/application.yml`
**Changes:**
- `server.port`: `8082` → `8112`
- `crowdsourced.baseUrl`: `http://localhost:8081` → `http://localhost:8111`

---

## Authority-E (KF7014_IndividualComponent_24036443)

### Folder Location
- **Original**: `/KF7014_IndividualComponent_24036443/` (or root level)
- **New**: `/authority-e/KF7014_IndividualComponent_24036443/`

### Changes Made
- ✅ **MOVED**: Entire `KF7014_IndividualComponent_24036443/` folder moved to `authority-e/`
- ✅ **PORT UPDATES**: All ports changed to avoid conflicts
- ✅ **INTERNAL URL UPDATES**: Gateway routes and rewards service base URL updated

### Files Modified

#### 1. `api-gateway/src/main/resources/application.yml`
**Changes:**
- `server.port`: `8080` → `8120`
- Gateway route for crowdsourced-data-service: `http://localhost:8081` → `http://localhost:8121`
- Gateway route for rewards-service: `http://localhost:8082` → `http://localhost:8122`

#### 2. `crowdsourced-data-service/src/main/resources/application.properties`
**Changes:**
- `server.port`: `8081` → `8121`

#### 3. `rewards-service/src/main/resources/application.properties`
**Changes:**
- `server.port`: `8082` → `8122`
- `crowdsourced-data.base-url`: `http://localhost:8081` → `http://localhost:8121`

---

## Root Level Changes

### New Files Created
- ✅ `README.md` - Comprehensive project documentation with port assignments, run steps, and architecture overview

### New Directories Created
- ✅ `auth-service/` - Placeholder for authentication service (to be created in step 2)
- ✅ `frontend/` - Placeholder for React frontend (to be created in step 4)
- ✅ `docs/` - Documentation folder (to be populated in later steps)
- ✅ `scripts/` - Utility scripts folder (to be populated in step 0.2)

---

## Summary by Change Type

### Port Changes Summary
| Authority | Service | Old Port | New Port |
|-----------|---------|----------|----------|
| Authority-A | Monolithic | 8080 | 8080 (unchanged) |
| Authority-B | Gateway | 8080 | 8090 |
| Authority-B | Data Service | 8081 | 8091 |
| Authority-B | Rewards Service | 8082 | 8092 |
| Authority-C | Gateway | 8080 | 8100 |
| Authority-C | Data Service | 8081 | 8101 |
| Authority-C | Rewards Service | 8082 | 8102 |
| Authority-D | Gateway | 8080 | 8110 |
| Authority-D | Data Service | 8081 | 8111 |
| Authority-D | Rewards Service | 8082 | 8112 |
| Authority-E | Gateway | 8080 | 8120 |
| Authority-E | Data Service | 8081 | 8121 |
| Authority-E | Rewards Service | 8082 | 8122 |

### Internal URL Changes Summary
All internal service communication URLs were updated to match new port assignments:
- Gateway routes pointing to data services
- Gateway routes pointing to rewards services
- Rewards services pointing to data services

---

## Important Notes

1. **No Code Changes**: Only configuration files (application.yml, application.properties) were modified. No Java source code was changed.

2. **No Structural Changes**: All internal folder structures, package names, and service implementations remain unchanged.

3. **Database Paths**: Database file paths (SQLite) remain relative and should work from new locations.

4. **Backward Compatibility**: Services can still run independently from their new locations with updated ports.

5. **Target Directories**: Note that `target/` directories (compiled classes) still contain old configurations. These will be regenerated on next build.

---

## Verification

All changes have been verified:
- ✅ All 5 authorities properly located in their folders
- ✅ All ports correctly mapped (no conflicts)
- ✅ All internal service URLs updated
- ✅ README.md includes all authorities
- ✅ Directory structure matches requirements


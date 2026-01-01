# Water Quality Monitoring - Group Microservices Project

## Architecture Overview

This is a monorepo containing a group microservices project for water quality monitoring with citizen science data collection. The project consists of:

- **auth-service** (Spring Boot) - Authentication and authorization service
- **frontend** (Vite React) - Single-page application for user interface
- **authority-a** - Monolithic Spring Boot application (CitizenScienceWaterQualityApplication)
- **authority-b** - Microservices architecture (api-gateway, crowdsourced-data-service, rewards-service)
- **authority-c** - Microservices architecture (api-gateway, data-service, rewards-service)
- **authority-d** - Microservices architecture (Gateway, CrowdsourcedDataService, RewardsService) with MongoDB
- **authority-e** - Microservices architecture (api-gateway, crowdsourced-data-service, rewards-service) with SQLite

## Port Assignments

### Core Services
- **auth-service**: `8083`
- **frontend**: `5173`

### Authority-A (Monolithic)
- **CitizenScienceWaterQualityApplication**: `8080`
  - Database: SQLite (`./data/water_quality.db`)

### Authority-B (Microservices)
- **api-gateway**: `8080` (⚠️ Port conflict - see Port Mapping Strategy below)
- **crowdsourced-data-service**: `8081` (⚠️ Port conflict)
- **rewards-service**: `8082` (⚠️ Port conflict)
  - Database: SQLite (`./data/water-quality.db`)

### Authority-C (Microservices)
- **api-gateway**: `8080` (⚠️ Port conflict)
- **data-service**: `8081` (⚠️ Port conflict)
- **rewards-service**: `8082` (⚠️ Port conflict)
  - Database: SQLite (`./data/observations.db`)

### Authority-D (Microservices)
- **Gateway**: `8080` (⚠️ Port conflict)
- **CrowdsourcedDataService**: `8081` (⚠️ Port conflict)
- **RewardsService**: `8082` (⚠️ Port conflict)
  - Database: MongoDB

### Authority-E (Microservices)
- **api-gateway**: `8080` (⚠️ Port conflict - see Port Mapping Strategy below)
- **crowdsourced-data-service**: `8081` (⚠️ Port conflict)
- **rewards-service**: `8082` (⚠️ Port conflict)
  - Database: SQLite (`./data/crowdsourced-data.db`)

## Port Mapping Strategy

Due to port conflicts (multiple services use 8080, 8081, 8082), the following port mapping strategy is recommended:

| Authority | Gateway Port | Data Service Port | Rewards Service Port |
|-----------|-------------|-------------------|---------------------|
| Authority-A | 8080 (monolithic) | N/A | N/A |
| Authority-B | 8090 | 8091 | 8092 |
| Authority-C | 8100 | 8101 | 8102 |
| Authority-D | 8110 | 8111 | 8112 |
| Authority-E | 8120 | 8121 | 8122 |

**Note:** Port configuration files need to be updated in each service's `application.yml` or `application.properties` to use the mapped ports. See individual authority READMEs for details.

## Run Steps

### Prerequisites
- Java 17+ (for Spring Boot services)
- Node.js 18+ and npm (for frontend)
- Maven 3.6+ (for Spring Boot services)
- MongoDB (for Authority-D only)

### Quick Start (Using Scripts)

```bash
# Start all services (recommended)
./scripts/run-all.sh

# Check service status
./scripts/check-service-status.sh

# Stop all services
./scripts/stop-all.sh

# View logs
tail -f scripts/logs/<service-name>.log
```

**Note**: Services start in the correct order with health checks. First startup may take 2-5 minutes.

### Manual Start (Per Authority)

#### Authority-A
```bash
cd authority-a/CitizenScienceWaterQualityApplication
mvn spring-boot:run
# Service runs on http://localhost:8080
```

#### Authority-B
```bash
# Terminal 1: Start crowdsourced-data-service
cd authority-b/water-quality-monitoringfinal/crowdsourced-data-service
mvn spring-boot:run
# Service runs on http://localhost:8091 (after port mapping)

# Terminal 2: Start rewards-service
cd authority-b/water-quality-monitoringfinal/rewards-service
mvn spring-boot:run
# Service runs on http://localhost:8092 (after port mapping)

# Terminal 3: Start api-gateway
cd authority-b/water-quality-monitoringfinal/api-gateway
mvn spring-boot:run
# Service runs on http://localhost:8090 (after port mapping)
```

#### Authority-C
```bash
# Terminal 1: Start data-service
cd authority-c/water-quality-service/data-service
mvn spring-boot:run
# Service runs on http://localhost:8101 (after port mapping)

# Terminal 2: Start rewards-service
cd authority-c/water-quality-service/rewards-service
mvn spring-boot:run
# Service runs on http://localhost:8102 (after port mapping)

# Terminal 3: Start api-gateway
cd authority-c/water-quality-service/api-gateway
mvn spring-boot:run
# Service runs on http://localhost:8100 (after port mapping)
```

#### Authority-D
```bash
# Ensure MongoDB is running
# mongod --dbpath <path-to-data>

# Terminal 1: Start CrowdsourcedDataService
cd authority-d/WaterQuality/CrowdsourcedDataService
mvn spring-boot:run
# Service runs on http://localhost:8111 (after port mapping)

# Terminal 2: Start RewardsService
cd authority-d/WaterQuality/RewardsService
mvn spring-boot:run
# Service runs on http://localhost:8112 (after port mapping)

# Terminal 3: Start Gateway
cd authority-d/WaterQuality/Gateway
mvn spring-boot:run
# Service runs on http://localhost:8110 (after port mapping)
```

#### Authority-E
```bash
# Terminal 1: Start crowdsourced-data-service
cd authority-e/KF7014_IndividualComponent_24036443/crowdsourced-data-service
mvn spring-boot:run
# Service runs on http://localhost:8121 (after port mapping)

# Terminal 2: Start rewards-service
cd authority-e/KF7014_IndividualComponent_24036443/rewards-service
mvn spring-boot:run
# Service runs on http://localhost:8122 (after port mapping)

# Terminal 3: Start api-gateway
cd authority-e/KF7014_IndividualComponent_24036443/api-gateway
mvn spring-boot:run
# Service runs on http://localhost:8120 (after port mapping)
```

#### Auth Service
```bash
cd auth-service
mvn spring-boot:run
# Service runs on http://localhost:8083
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
# Application runs on http://localhost:5173
```

## Demo Credentials

See `docs/demo-credentials.md` for complete demo user information and seed data details.

### Quick Start - Register a User

```bash
curl -X POST http://localhost:8083/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@example.com",
    "password": "demo123",
    "name": "Demo User"
  }'
```

### Example Users

| Email | Password | Name |
|-------|----------|------|
| `demo@example.com` | `demo123` | Demo User |
| `admin@waterquality.gov` | `admin123` | Admin User |

**Note:** Users must be registered before use. Auth service uses in-memory storage.

### Seed Data

All authorities are automatically seeded with demo data on first startup:
- **14 observations** per authority
- **3 contributors** with different point totals
- **Leaderboards** populated after rewards processing

To process rewards after startup:
```bash
./scripts/seed-and-process-rewards.sh
```

## Project Structure

```
.
├── auth-service/              # Authentication service (to be created)
├── frontend/                  # React frontend (to be created)
├── authority-a/
│   └── CitizenScienceWaterQualityApplication/  # Monolithic Spring Boot app
├── authority-b/
│   └── water-quality-monitoringfinal/          # 3 microservices
│       ├── api-gateway/
│       ├── crowdsourced-data-service/
│       └── rewards-service/
├── authority-c/
│   └── water-quality-service/                  # 3 microservices
│       ├── api-gateway/
│       ├── data-service/
│       └── rewards-service/
├── authority-d/
│   └── WaterQuality/                          # 3 microservices (MongoDB)
│       ├── Gateway/
│       ├── CrowdsourcedDataService/
│       └── RewardsService/
├── authority-e/
│   └── KF7014_IndividualComponent_24036443/  # 3 microservices
│       ├── api-gateway/
│       ├── crowdsourced-data-service/
│       └── rewards-service/
├── docs/                      # Documentation
│   ├── api-contract.md
│   ├── project-plan.md
│   ├── logbook.md
│   └── demo-script.md
└── scripts/                   # Utility scripts
    ├── run-all.sh
    └── stop-all.sh
```

## API Contract

All authority services expose a consistent API contract for dashboard integration:

- `GET /api/observations/count` - Get total observation count
- `GET /api/observations/recent?limit=5` - Get recent observations
- `GET /api/rewards/leaderboard?limit=3` - Get leaderboard

See `docs/api-contract.md` for detailed API specification.

## Development Workflow

1. **Start databases** (if needed): MongoDB for Authority-D
2. **Start authority services** in order: data services → rewards services → gateways
3. **Start auth-service**
4. **Start frontend**

## Testing

```bash
# Run tests for a specific service
cd authority-a/CitizenScienceWaterQualityApplication
mvn test

# Run all tests (when scripts are ready)
./scripts/test-all.sh
```

## Troubleshooting

### Port Conflicts
If you encounter port conflicts, ensure:
1. Port mapping strategy is applied (see Port Mapping Strategy section)
2. Configuration files are updated with new ports
3. No other services are using the same ports

### Database Issues
- **SQLite**: Ensure database files exist in `./data/` directories
- **MongoDB**: Ensure MongoDB is running and accessible

### Service Communication
- Verify gateway routes are correctly configured
- Check service URLs in gateway configuration files
- Ensure services are started in the correct order

## Contributing

Each authority maintains its own microservices. When adding new features:
1. Do not modify existing controllers/services
2. Add adapter endpoints to match the API contract
3. Update documentation in `docs/` folder

## License

[Add license information if applicable]


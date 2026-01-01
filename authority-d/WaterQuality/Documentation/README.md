# Citizen Science Water Quality - Microservices

This repository contains three Spring Boot microservices and an API Gateway for a citizen science water quality monitoring system.

- CrowdsourcedDataService (port 8081): Receives, validates, and stores observations in MongoDB
- RewardsService (port 8082): Computes points and badges based on observations
- Gateway (port 8080): Public wrapper that exposes APIs and forwards to the internal services

## Tech Stack
- Java 17, Spring Boot 3
- Spring Data MongoDB (CrowdsourcedDataService)
- Spring WebFlux (Gateway wrapper)
- Maven
- MongoDB (local, default: `mongodb://localhost:27017/waterquality`)

## Prerequisites
- Java 17 installed
- Maven installed
- MongoDB running locally on `localhost:27017`

## Project Structure
```
CrowdsourcedDataService/
RewardsService/
Gateway/
Documentation/
```

## Architecture (Mermaid)

```mermaid
flowchart TD
    client[Client\n(Web Browser / Postman)] -->|HTTP/HTTPS| gateway[API Gateway\nPort 8080\nSpring WebFlux]

    gateway -->|WebClient (reactive)| cds["CrowdsourcedDataService\nPort 8081\nSpring Web + MongoDB"]
    gateway -->|WebClient (reactive)| rewards["RewardsService\nPort 8082\nSpring Web"]

    rewards -->|RestTemplate (sync)| cds
    cds -->|Spring Data MongoDB| mongo[(MongoDB\nlocalhost:27017\nwaterquality)]

    classDef svc fill:#fff2cc,stroke:#d6b656,color:#000;
    classDef gw fill:#d5e8d4,stroke:#82b366,color:#000;
    classDef client fill:#dae8fc,stroke:#6c8ebf,color:#000;
    classDef db fill:#f8cecc,stroke:#b85450,color:#000;

    class client client;
    class gateway gw;
    class cds,rewards svc;
    class mongo db;
```

## Build and Run (Maven)
Build each project using Maven from your IDE or terminal.

- Build (per project):
```
mvn clean package
```

- Run (per project):
```
mvn spring-boot:run
```

Start order (recommended):
1) CrowdsourcedDataService (8081)
2) RewardsService (8082)
3) Gateway (8080)

## Configuration
- CrowdsourcedDataService: `src/main/resources/application.yml`
  - Mongo URI: `spring.data.mongodb.uri: mongodb://localhost:27017/waterquality`
  - Port: 8081
  - Seed data auto-loads from `src/main/resources/data/seed-observations.json` if empty

- RewardsService: `src/main/resources/application.yml`
  - Port: 8082
  - `crowdsourced.baseUrl` defaults to `http://localhost:8081`

- Gateway: `src/main/resources/application.yml`
  - Port: 8080
  - Internal service endpoints used by the wrapper:
```
services:
  crowdsourced:
    baseUrl: http://localhost:8081
  rewards:
    baseUrl: http://localhost:8082
```

## Data Model (CrowdsourcedDataService)
- WaterObservation
  - `id` (UUID), `citizenId` (string), `postcode` (string)
  - `measurements` (temperatureCelsius, ph, alkalinityMgPerL, turbidityNtu)
  - `observations` (array of strings: Clear, Cloudy, Murky, Foamy, Oily, Discoloured, Presence of Odour)
  - `imageData` (array of strings; optional; up to 3)
  - `submittedAt` (timestamp)

## Validation Rules
- Postcode: required
- Citizen ID: required
- Must include at least one measurement OR one observation
- Image list is limited to 3 entries

## Reward Rules (RewardsService)
- +10 points per valid submission
- +10 bonus points if complete (citizenId + postcode + all four measurements + ≥1 observation)
- Badges: Bronze (≥100), Silver (≥200), Gold (≥500)

## Health Endpoints

### Gateway Health Check (Custom)
Check the health of the Gateway and all downstream services:

```bash
curl -X GET http://localhost:8080/health
```

**Response (200 OK - All Services Up):**
```json
{
  "status": "UP",
  "gateway": "UP",
  "crowdsourcedService": "UP",
  "rewardsService": "UP"
}
```

**Response (503 Service Unavailable - Some Services Down):**
```json
{
  "status": "DEGRADED",
  "gateway": "UP",
  "crowdsourcedService": "DOWN",
  "rewardsService": "UP"
}
```

**Response (500 Internal Server Error - Timeout):**
```json
{
  "status": "DOWN",
  "gateway": "UP",
  "error": "Health check timeout"
}
```

### Gateway Actuator Health
Spring Boot Actuator health endpoint:

```bash
curl -X GET http://localhost:8080/actuator/health
```

### Service Health Endpoints (Direct Access)
If accessing services directly (not through Gateway):

```bash
# CrowdsourcedDataService
curl -X GET http://localhost:8081/actuator/health

# RewardsService
curl -X GET http://localhost:8082/actuator/health
```

## Endpoints (via Gateway)
Base URL: `http://localhost:8080`

- Create observation
  - POST `/api/observations`
  - Body example:
```
{
  "citizenId": "citizen-001",
  "postcode": "NE1 1AA",
  "measurements": {
    "temperatureCelsius": 12.5,
    "ph": 7.2,
    "alkalinityMgPerL": 40.0,
    "turbidityNtu": 2.0
  },
  "observations": ["Clear"],
  "imageData": []
}
```
  - Responses: 201 Created (on success), 400 Bad Request (on validation error)

- List observations (all)
  - GET `/api/observations`

- List observations by citizen
  - GET `/api/observations?citizenId=citizen-001`

- Recompute rewards for a citizen
  - POST `/api/rewards/recompute/{citizenId}`
  - Example: `/api/rewards/recompute/citizen-001`

- Get rewards for a citizen
  - GET `/api/rewards/{citizenId}`
  - Example response:
```
{
  "citizenId": "citizen-001",
  "totalPoints": 20,
  "badge": ""
}
```

## How the Gateway Talks to Services
- The Gateway acts as a wrapper using WebFlux (`WebClient`) to call internal services:
  - `services.crowdsourced.baseUrl` + `/api/observations[...]`
  - `services.rewards.baseUrl` + `/api/rewards[...]`
- Only the Gateway must be publicly accessible; the two microservices can be private/behind a firewall.

## Unit Testing

### Running Tests

Run tests for all services:

```bash
# RewardsService
cd RewardsService
mvn test

# CrowdsourcedDataService
cd CrowdsourcedDataService
mvn test

# Gateway
cd Gateway
mvn test
```

Run all tests from the root directory:
```bash
cd RewardsService && mvn test && cd ..
cd CrowdsourcedDataService && mvn test && cd ..
cd Gateway && mvn test
```

### Test Coverage

All services use **JaCoCo** for code coverage reporting with a minimum threshold of **80%** for business logic classes.

#### Coverage Reports

After running tests, HTML coverage reports are generated at:
- **RewardsService**: `RewardsService/target/site/jacoco/index.html`
- **CrowdsourcedDataService**: `CrowdsourcedDataService/target/site/jacoco/index.html`
- **Gateway**: `Gateway/target/site/jacoco/index.html`

Open these HTML files in your browser to view:
- Overall coverage percentage
- Coverage by package/class
- Line-by-line coverage (green = covered, red = not covered)
- Branch coverage
- Method coverage

#### Coverage Configuration

**Excluded from Coverage Requirements:**
- Application main classes (`*Application.java`) - not typically unit tested
- Model/DTO classes - data classes with getters/setters
- Gateway coverage check is disabled due to WebFlux reactive code instrumentation limitations

**Coverage Requirements:**
- **80% line coverage** for:
  - Controllers
  - Services
  - Clients
  - Repositories
  - Configuration classes

#### Generating Coverage Reports

```bash
# Generate report after tests
mvn jacoco:report

# Check coverage thresholds
mvn jacoco:check
```

### Test Structure

#### RewardsService Tests
- **RewardComputationServiceTest**: 27+ tests covering:
  - Valid/invalid observation validation
  - Complete vs incomplete observations
  - Badge computation with various thresholds
  - Edge cases (null, empty, special characters)
  - Caching behavior
  - Point calculation logic
- **RewardsControllerTest**: 12+ tests covering:
  - All HTTP endpoints
  - Request/response validation
  - Edge cases with different citizen IDs
- **CrowdsourcedClientTest**: Tests for HTTP client behavior and error handling
- **BadgePropertiesTest**: Tests for badge configuration and sorting

#### CrowdsourcedDataService Tests
- **WaterObservationServiceTest**: 30+ tests covering:
  - All validation edge cases (null, empty, blank fields)
  - Measurement validation (partial, full, null)
  - Observation validation
  - Image limit enforcement (3 max)
  - Repository interactions
- **WaterObservationControllerTest**: 20+ tests covering:
  - All HTTP endpoints
  - Request validation
  - Error responses
  - Query parameter handling
- **RestExceptionHandlerTest**: Tests for exception handling and response structure

#### Gateway Tests
- **PublicApiControllerTest**: Tests for all proxy endpoints and error handling
- **HealthControllerTest**: Tests for health check scenarios (all up, one down, both down, timeouts)
- **DownstreamClientsTest**: Tests for all client methods and error handling

### Test Coverage Summary

The test suite includes comprehensive edge case coverage:
- ✅ Null and empty value handling
- ✅ Special characters and Unicode
- ✅ Large payloads
- ✅ Error scenarios and exception handling
- ✅ Boundary conditions
- ✅ Validation rules
- ✅ Business logic paths

### Running Specific Tests

```bash
# Run a specific test class
mvn test -Dtest=RewardComputationServiceTest

# Run multiple test classes
mvn test -Dtest=RewardComputationServiceTest,RewardsControllerTest

# Run tests matching a pattern
mvn test -Dtest=*Test
```

### Skipping Tests

```bash
# Skip tests during build
mvn clean install -DskipTests

# Compile tests but don't run them
mvn clean install -Dmaven.test.skip=true
```

## Postman Collection
A Postman collection JSON is provided at `Documentation/WaterQuality.postman_collection.json` covering the endpoints above via the Gateway.

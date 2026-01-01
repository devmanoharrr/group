# Technical Documentation

## Citizen Science Water Quality Monitoring System

**Version:** 1.0  
**Date:** 2024  
**Author:** Water Quality Development Team

---

## Table of Contents

1. [Architecture Diagram](#architecture-diagram)
2. [Database Schema](#database-schema)
3. [Class Diagram](#class-diagram)
4. [APIs Documentation](#apis-documentation)
5. [Critical Evaluation](#critical-evaluation)
6. [References](#references)

---

## Architecture Diagram

The architecture diagram illustrates the microservices-based solution architecture, showing the communication flow between services.

**File:** `architecture-diagram.drawio`

The system consists of three main components:

1. **API Gateway (Port 8080)** - Public-facing entry point using Spring WebFlux
2. **CrowdsourcedDataService (Port 8081)** - Handles observation storage using Spring Web and MongoDB
3. **RewardsService (Port 8082)** - Computes rewards and badges using Spring Web

### Communication Flow

- **Client → Gateway**: All external requests enter through the Gateway
- **Gateway → CrowdsourcedDataService**: Forwards observation-related requests via WebClient (reactive)
- **Gateway → RewardsService**: Forwards rewards-related requests via WebClient (reactive)
- **RewardsService → CrowdsourcedDataService**: Fetches citizen observations using RestTemplate (synchronous)
- **CrowdsourcedDataService → MongoDB**: Persists observations in MongoDB database

### Key Technologies

- **Gateway**: Spring Cloud Gateway, Spring WebFlux, WebClient
- **CrowdsourcedDataService**: Spring Web, Spring Data MongoDB
- **RewardsService**: Spring Web, RestTemplate
- **Database**: MongoDB (NoSQL document database)

---

## Database Schema

The database schema diagram shows the MongoDB document structure for the WaterObservation collection.

**File:** `database-schema.drawio`

### MongoDB Collection: `observations`

The system uses MongoDB as a NoSQL document database. The schema is represented as a single collection:

#### Collection: observations

**Document Structure:**
```json
{
  "_id": "String (UUID)",
  "citizenId": "String (required)",
  "postcode": "String (required)",
  "measurements": {
    "temperatureCelsius": "Double (optional)",
    "ph": "Double (optional)",
    "alkalinityMgPerL": "Double (optional)",
    "turbidityNtu": "Double (optional)"
  },
  "observations": ["String[] (optional)"],
  "imageData": ["String[] (optional, max 3)"],
  "submittedAt": "Instant (timestamp)"
}
```

**Validation Rules:**
- `citizenId`: Required, non-blank
- `postcode`: Required, non-blank
- At least one measurement OR one observation must be present
- `imageData`: Maximum 3 entries
- Valid observation values: "Clear", "Cloudy", "Murky", "Foamy", "Oily", "Discoloured", "Presence of Odour"

**Indexes:**
- `_id`: Primary key (auto-generated UUID)
- `citizenId`: Used for querying observations by citizen (custom repository method)

**Relationships:**
- No foreign key relationships (NoSQL document database)
- Data is denormalized for performance
- Citizen observations are linked via `citizenId` field

---

## Class Diagram

The class diagram illustrates the design of all classes, interfaces, and their relationships across the three microservices.

**File:** `class-diagram.drawio`

### Gateway Service

**Package:** `kf7014.gateway`

#### Controllers
- **PublicApiController**: REST controller exposing public API endpoints
  - Methods: `createObservation()`, `listObservations()`, `recompute()`, `get()`
- **HealthController**: Custom health check endpoint
  - Methods: `health()`, `checkDownstreamServices()`

#### Clients
- **DownstreamClients**: WebClient-based service client for downstream services
  - Methods: `createObservation()`, `listObservations()`, `recomputeRewards()`, `getRewards()`, `checkCrowdsourcedHealth()`, `checkRewardsHealth()`

### CrowdsourcedDataService

**Package:** `kf7014.crowdsourceddata`

#### Controllers
- **WaterObservationController**: REST controller for observation endpoints
  - Methods: `submit()`, `list()`
- **RestExceptionHandler**: Global exception handler
  - Methods: `handleIllegalArgument()`, `handleValidation()`

#### Models
- **WaterObservation**: MongoDB document entity
  - Fields: `id`, `citizenId`, `postcode`, `measurements`, `observations`, `imageData`, `submittedAt`
- **MeasurementSet**: Nested measurement data
  - Fields: `temperatureCelsius`, `ph`, `alkalinityMgPerL`, `turbidityNtu`

#### Repository
- **WaterObservationRepository**: MongoDB repository interface
  - Extends: `MongoRepository<WaterObservation, String>`
  - Methods: `findByCitizenId(String citizenId)`

#### Service
- **WaterObservationService**: Business logic for observations
  - Methods: `createObservation()`, `listAll()`, `listByCitizen()`, `validateObservation()`, `hasAnyMeasurement()`, `enforceImageLimit()`

#### Config
- **SeedDataLoader**: Loads initial seed data on startup

### RewardsService

**Package:** `kf7014.rewards`

#### Controllers
- **RewardsController**: REST controller for rewards endpoints
  - Methods: `recompute()`, `get()`

#### Models
- **RewardSummary**: Reward computation result
  - Fields: `citizenId`, `totalPoints`, `badge`

#### Service
- **RewardComputationService**: Business logic for reward computation
  - Methods: `recomputeForCitizen()`, `getSummary()`, `isValid()`, `isComplete()`, `computeBadge()`
  - Cache: In-memory `Map<String, RewardSummary>`

#### Clients
- **CrowdsourcedClient**: RestTemplate-based client for CrowdsourcedDataService
  - Methods: `findByCitizenId()`
  - Inner Classes: `ObservationDto`, `Measurements`

### Relationships

- **Gateway** → **DownstreamClients** (composition)
- **Gateway** → **PublicApiController** (composition)
- **Gateway** → **HealthController** (composition)
- **CrowdsourcedDataService** → **WaterObservationService** (composition)
- **CrowdsourcedDataService** → **WaterObservationRepository** (composition)
- **RewardsService** → **RewardComputationService** (composition)
- **RewardsService** → **CrowdsourcedClient** (composition)
- **RewardsService** → **CrowdsourcedDataService** (service-to-service communication)

---

## APIs Documentation

### Base URLs

- **Gateway**: `http://localhost:8080`
- **CrowdsourcedDataService** (direct): `http://localhost:8081`
- **RewardsService** (direct): `http://localhost:8082`

### Health Endpoints

#### 1. Gateway Health Check (Custom)

**Endpoint:** `GET /health`

**Description:** Custom health check endpoint that verifies gateway and downstream services status.

**Request:**
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

#### 2. Gateway Actuator Health

**Endpoint:** `GET /actuator/health`

**Description:** Spring Boot Actuator health endpoint.

**Request:**
```bash
curl -X GET http://localhost:8080/actuator/health
```

**Response:**
```json
{
  "status": "UP"
}
```

#### 3. CrowdsourcedDataService Health

**Endpoint:** `GET /actuator/health`

**Description:** Spring Boot Actuator health endpoint for CrowdsourcedDataService.

**Request:**
```bash
curl -X GET http://localhost:8081/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP"
    }
  }
}
```

#### 4. RewardsService Health

**Endpoint:** `GET /actuator/health`

**Description:** Spring Boot Actuator health endpoint for RewardsService.

**Request:**
```bash
curl -X GET http://localhost:8082/actuator/health
```

**Response:**
```json
{
  "status": "UP"
}
```

### Observation Endpoints

#### 5. Create Observation

**Endpoint:** `POST /api/observations`

**Description:** Creates a new water quality observation.

**Request:**
```bash
curl -X POST http://localhost:8080/api/observations \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

**Request Body:**
```json
{
  "citizenId": "string (required)",
  "postcode": "string (required)",
  "measurements": {
    "temperatureCelsius": "number (optional)",
    "ph": "number (optional)",
    "alkalinityMgPerL": "number (optional)",
    "turbidityNtu": "number (optional)"
  },
  "observations": ["string[] (optional)"],
  "imageData": ["string[] (optional, max 3)"]
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "citizenId": "citizen-001",
  "postcode": "NE1 1AA",
  "measurements": {
    "temperatureCelsius": 12.5,
    "ph": 7.2,
    "alkalinityMgPerL": 40.0,
    "turbidityNtu": 2.0
  },
  "observations": ["Clear"],
  "imageData": [],
  "submittedAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses:**

**400 Bad Request - Missing Postcode:**
```json
{
  "error": "Postcode is required"
}
```

**400 Bad Request - Missing Citizen ID:**
```json
{
  "error": "Citizen ID is required"
}
```

**400 Bad Request - No Measurements or Observations:**
```json
{
  "error": "At least one measurement or observation is required"
}
```

**400 Bad Request - Validation Error:**
```json
{
  "error": "Validation failed"
}
```

#### 6. List All Observations

**Endpoint:** `GET /api/observations`

**Description:** Retrieves all water quality observations.

**Request:**
```bash
curl -X GET http://localhost:8080/api/observations
```

**Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "citizenId": "citizen-001",
    "postcode": "NE1 1AA",
    "measurements": {
      "temperatureCelsius": 12.5,
      "ph": 7.2,
      "alkalinityMgPerL": 40.0,
      "turbidityNtu": 2.0
    },
    "observations": ["Clear"],
    "imageData": [],
    "submittedAt": "2024-01-15T10:30:00Z"
  }
]
```

#### 7. List Observations by Citizen

**Endpoint:** `GET /api/observations?citizenId={citizenId}`

**Description:** Retrieves all observations for a specific citizen.

**Request:**
```bash
curl -X GET "http://localhost:8080/api/observations?citizenId=citizen-001"
```

**Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "citizenId": "citizen-001",
    "postcode": "NE1 1AA",
    "measurements": {
      "temperatureCelsius": 12.5,
      "ph": 7.2,
      "alkalinityMgPerL": 40.0,
      "turbidityNtu": 2.0
    },
    "observations": ["Clear"],
    "imageData": [],
    "submittedAt": "2024-01-15T10:30:00Z"
  }
]
```

**Response (200 OK - No Observations):**
```json
[]
```

### Rewards Endpoints

#### 8. Recompute Rewards

**Endpoint:** `POST /api/rewards/recompute/{citizenId}`

**Description:** Recomputes and caches rewards for a specific citizen based on all their observations.

**Request:**
```bash
curl -X POST http://localhost:8080/api/rewards/recompute/citizen-001
```

**Path Parameters:**
- `citizenId` (string, required): The citizen identifier

**Response (200 OK):**
```json
{
  "citizenId": "citizen-001",
  "totalPoints": 20,
  "badge": ""
}
```

**Reward Calculation Rules:**
- +10 points per valid submission
- +10 bonus points for complete submissions (all four measurements + at least one observation + citizenId + postcode)
- Badge thresholds:
  - Bronze: ≥100 points
  - Silver: ≥200 points
  - Gold: ≥500 points

**Response Examples:**

**Bronze Badge:**
```json
{
  "citizenId": "citizen-001",
  "totalPoints": 150,
  "badge": "Bronze"
}
```

**Silver Badge:**
```json
{
  "citizenId": "citizen-002",
  "totalPoints": 250,
  "badge": "Silver"
}
```

**Gold Badge:**
```json
{
  "citizenId": "citizen-003",
  "totalPoints": 550,
  "badge": "Gold"
}
```

#### 9. Get Rewards Summary

**Endpoint:** `GET /api/rewards/{citizenId}`

**Description:** Retrieves the cached rewards summary for a citizen. Returns default values if not previously computed.

**Request:**
```bash
curl -X GET http://localhost:8080/api/rewards/citizen-001
```

**Path Parameters:**
- `citizenId` (string, required): The citizen identifier

**Response (200 OK - Cached):**
```json
{
  "citizenId": "citizen-001",
  "totalPoints": 20,
  "badge": ""
}
```

**Response (200 OK - Not Computed):**
```json
{
  "citizenId": "citizen-001",
  "totalPoints": 0,
  "badge": ""
}
```

### Error Codes Summary

| HTTP Status Code | Description | Endpoints |
|-----------------|-------------|-----------|
| 200 OK | Successful GET request | All GET endpoints |
| 201 Created | Resource successfully created | POST /api/observations |
| 400 Bad Request | Validation error or invalid input | POST /api/observations |
| 500 Internal Server Error | Server error | All endpoints |
| 503 Service Unavailable | Downstream service unavailable | GET /health |

### Postman Collection

A complete Postman collection is available at: `Documentation/WaterQuality.postman_collection.json`

**Screenshots of API Testing:**

*Note: Screenshots should be added here showing Postman requests and responses for:*
- Creating an observation (POST /api/observations)
- Listing observations (GET /api/observations)
- Recomputing rewards (POST /api/rewards/recompute/{citizenId})
- Getting rewards (GET /api/rewards/{citizenId})
- Health check (GET /health)

---

## Critical Evaluation

The development of this microservices-based citizen science water quality monitoring system has been a valuable learning experience in distributed systems architecture. This critical evaluation reflects on the journey from inception to completion, highlighting both successes and areas for improvement.

### What Has Been Done Well

The architecture demonstrates a clear separation of concerns with three distinct microservices, each with a single responsibility. The Gateway service effectively acts as a unified entry point, abstracting the internal service structure from clients. This design promotes scalability and maintainability, as each service can be developed, deployed, and scaled independently.

The implementation of health endpoints across all services provides excellent observability. The custom health check endpoint in the Gateway that aggregates downstream service health is particularly valuable for monitoring system status. Using Spring Boot Actuator ensures standard health monitoring capabilities are available out-of-the-box.

Data validation is robust, with comprehensive checks ensuring data integrity. The business logic for reward computation is well-structured, with clear rules for point calculation and badge assignment. The use of MongoDB as a document database is appropriate for the flexible schema requirements of observational data.

The reactive programming model in the Gateway using Spring WebFlux and WebClient demonstrates modern asynchronous communication patterns, which is beneficial for handling concurrent requests efficiently.

### What Could Have Been Done Better

Several areas present opportunities for improvement. The RewardsService uses an in-memory cache (`HashMap`) for storing reward summaries, which means data is lost on service restart. This design choice limits scalability and reliability. A persistent storage solution, such as a database or distributed cache like Redis, would provide better durability and support horizontal scaling.

The communication patterns between services are inconsistent. The Gateway uses reactive WebClient for asynchronous communication, while the RewardsService uses synchronous RestTemplate to communicate with the CrowdsourcedDataService. This inconsistency could lead to blocking operations and reduced performance. Standardizing on reactive communication throughout would improve overall system responsiveness.

Error handling, while functional, could be more comprehensive. The current implementation provides basic error messages, but structured error responses with error codes, timestamps, and detailed validation messages would enhance developer experience and debugging capabilities.

The system lacks comprehensive logging and distributed tracing. In a microservices architecture, tracing requests across services is crucial for debugging and performance monitoring. Implementing a solution like Spring Cloud Sleuth or Zipkin would provide valuable insights into request flows and performance bottlenecks.

Security considerations are minimal. The system lacks authentication and authorization mechanisms, making it vulnerable to unauthorized access. Implementing OAuth2, JWT tokens, or API keys would secure the endpoints. Additionally, input sanitization could be enhanced to prevent potential injection attacks.

Testing coverage, while present, could be expanded. Integration tests that verify end-to-end workflows across services would increase confidence in the system's reliability. Load testing would also help identify performance bottlenecks under high concurrent usage.

### Conclusion

Overall, the project successfully demonstrates core microservices principles and provides a functional system for citizen science water quality monitoring. The architecture is sound, and the implementation follows Spring Boot best practices. However, production readiness would require addressing the caching strategy, standardizing communication patterns, enhancing security, and improving observability. The foundation is solid, and with the suggested improvements, this system could evolve into a robust, scalable, and production-ready solution.

**Word Count:** ~500 words

---

## References

Spring Boot Documentation. (2024). *Spring Boot Reference Documentation*. Retrieved from https://spring.io/projects/spring-boot

Spring Data MongoDB Documentation. (2024). *Spring Data MongoDB - Reference Documentation*. Retrieved from https://spring.io/projects/spring-data-mongodb

Spring WebFlux Documentation. (2024). *Spring WebFlux Reference Documentation*. Retrieved from https://docs.spring.io/spring-framework/reference/web/webflux.html

MongoDB Documentation. (2024). *MongoDB Manual*. Retrieved from https://www.mongodb.com/docs/

Newman, S. (2021). *Building Microservices: Designing Fine-Grained Systems* (2nd ed.). O'Reilly Media.

Richardson, C. (2018). *Microservices Patterns: With Examples in Java*. Manning Publications.

---

**Document End**


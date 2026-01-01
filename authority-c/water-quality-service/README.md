# Water Quality Platform

A microservices-based platform for crowdsourcing water quality observations with a rewards system.

## Overview

This project consists of three main microservices:

- **Data Service** - Handles observation submissions and storage
- **Rewards Service** - Calculates points and badges for citizen contributions
- **API Gateway** - Routes requests to appropriate microservices

## Prerequisites

- Java 17 or higher
- Maven 3.6+ (or use included `mvnw`)
- SQLite (for database)

## Project Structure

```
water-quality/
├── data-service/          # Crowdsourced data microservice
├── rewards-service/       # Rewards calculation microservice
├── api-gateway/          # API Gateway for routing
├── auth-service/         # Authentication service (not used in individual component)
├── data/                 # Database and uploaded images
│   ├── observations.db   # SQLite database
│   └── images/           # Uploaded observation images
└── pom.xml               # Parent POM for multi-module build
```

## Building the Project

From the project root directory:

```bash
# Build all modules
./mvnw clean install

# Or skip tests for faster build
./mvnw clean install -DskipTests

# Format code
make format

# Run checkstyle
make check
```

## Running the Services

### Start Data Service

```bash
cd data-service
../mvnw spring-boot:run
```

Runs on **port 8081**

### Start Rewards Service

```bash
cd rewards-service
../mvnw spring-boot:run
```

Runs on **port 8082**

### Start API Gateway

```bash
cd api-gateway
../mvnw spring-boot:run
```

Runs on **port 8080**

**Note:** Start services in this order: Data Service → Rewards Service → API Gateway

## Service Ports

- **API Gateway:** 8080
- **Data Service:** 8081
- **Rewards Service:** 8082

## API Endpoints

### Data Service (via Gateway: `/api/data`)

#### Create Observation
```http
POST /api/data/observations
Content-Type: application/json

{
  "citizenId": "citizen-123",
  "postcode": "NE1 4LP",
  "temperatureC": 12.5,
  "pH": 7.2,
  "alkalinityMgL": 95.0,
  "turbidityNTU": 2.1,
  "observations": ["CLEAR"],
  "imagePaths": ["images/test.jpg"],
  "authority": "NE"
}
```

#### Get Observation by ID
```http
GET /api/data/observations/{id}
```

#### Get Latest Observations
```http
GET /api/data/observations/latest?authority=NE&limit=10
```

#### Get Observation Count
```http
GET /api/data/observations/count?authority=NE
```

#### Upload Observation with Images
```http
POST /api/data/observations/upload
Content-Type: multipart/form-data

payload: { JSON observation data }
images: [file1.jpg, file2.jpg, file3.jpg]  # Max 3 images
```

### Rewards Service (via Gateway: `/api/rewards`)

#### Get Citizen Rewards
```http
GET /api/rewards/citizens/{citizenId}
```

Response:
```json
{
  "citizenId": "citizen-123",
  "totalPoints": 60,
  "badges": ["BRONZE"]
}
```

#### Get Leaderboard
```http
GET /api/rewards/leaderboard?authority=NE&limit=10
```

## Database Setup

The Data Service uses SQLite with Flyway migrations:

- Database file: `data/observations.db`
- Migrations: `data-service/src/main/resources/db/migration/`
- Sample data is automatically seeded on first run

### Database Schema

```sql
CREATE TABLE observation (
  id TEXT PRIMARY KEY,
  citizen_id TEXT NOT NULL,
  postcode TEXT NOT NULL,
  temperature_c REAL,
  ph REAL,
  alkalinity_mg_l REAL,
  turbidity_ntu REAL,
  observations TEXT,
  image_paths TEXT,
  authority TEXT,
  created_at TEXT NOT NULL
);
```

## Validation Rules

### Observation Creation

- **Postcode:** Must be valid UK postcode format
- **pH:** Must be between 0 and 14
- **Measurements:** Temperature, alkalinity, turbidity must be non-negative
- **At least one:** Must provide at least one measurement OR observation tag
- **Images:** Maximum 3 images allowed
- **Citizen ID:** Required, non-blank

### Rewards Calculation

- **Base Points:** 10 points for any valid observation
- **Bonus Points:** +10 points for complete submissions (all measurements + observation tag)
- **Badges:**
  - Bronze: 100 points
  - Silver: 200 points
  - Gold: 500 points

## Testing

### Run All Tests

```bash
./mvnw test
```

### Run Tests for Specific Module

```bash
cd data-service
../mvnw test
```

### Integration Tests

Integration tests use an in-memory SQLite database and are located in:
- `data-service/src/test/java/com/bharath/wq/data/api/ObservationControllerIntegrationTest.java`
- `rewards-service/src/test/java/com/bharath/wq/rewards/api/RewardsControllerIntegrationTest.java`

## Error Handling

All services return structured error responses using RFC 7807 ProblemDetail format:

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "Validation error details..."
}
```

### HTTP Status Codes

- **200 OK** - Successful GET request
- **201 Created** - Successful POST request
- **202 Accepted** - Observation ingestion accepted
- **400 Bad Request** - Validation error or invalid input
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Unexpected server error
- **503 Service Unavailable** - Upstream service unavailable (Gateway)

## Configuration

### Data Service (`data-service/src/main/resources/application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:sqlite:${wq.data.dir:../data}/observations.db
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 16MB

wq:
  seed:
    enabled: true  # Set to false to disable sample data
```

### Rewards Service (`rewards-service/src/main/resources/application.yml`)

```yaml
rewards:
  data:
    base-url: http://localhost:8081
    batch-size: 50
  poll:
    ms: 5000  # Poll interval in milliseconds
```

### API Gateway (`api-gateway/src/main/resources/application.yml`)

```yaml
upstreams:
  data: http://localhost:8081
  rewards: http://localhost:8082

gateway:
  cors:
    allowed-origins: "http://localhost:5173,http://localhost:3000"
```

## Example API Calls

### Using cURL

```bash
# Create observation
curl -X POST http://localhost:8080/api/data/observations \
  -H "Content-Type: application/json" \
  -d '{
    "citizenId": "citizen-123",
    "postcode": "NE1 4LP",
    "temperatureC": 12.5,
    "pH": 7.2,
    "observations": ["CLEAR"],
    "authority": "NE"
  }'

# Get observation by ID
curl http://localhost:8080/api/data/observations/{id}

# Get citizen rewards
curl http://localhost:8080/api/rewards/citizens/citizen-123

# Get leaderboard
curl http://localhost:8080/api/rewards/leaderboard?authority=NE&limit=10
```

## Development

### Code Quality

- **Checkstyle:** Configured in `config/checkstyle/checkstyle.xml`
- **Spotless:** Code formatting with Google Java Format
- **Run checks:** `make check` or `make verify`

### Logging

All services use SLF4J with Logback. Log levels can be configured in `application.yml`:

```yaml
logging:
  level:
    com.bharath.wq: DEBUG
    org.springframework: INFO
```

## Troubleshooting

### Database Issues

- Ensure `data/` directory exists and is writable
- Check Flyway migration logs in application startup
- Delete `observations.db` to reset database (will reseed sample data)

### Service Connection Issues

- Verify all services are running on correct ports
- Check `application.yml` for correct upstream URLs
- Check firewall settings if services can't communicate

### Image Upload Issues

- Verify `data/images/` directory exists and is writable
- Check file size limits in `application.yml`
- Ensure images are valid image formats (jpg, png, gif, webp)

## License

This project is part of the KF7014 Advanced Programming assessment.

# Citizen Science Water Quality Microservices

This repository hosts a Spring Boot multi-module project that implements the Citizen Science Water Quality Application. The solution is organised into three deployable microservices and a shared data folder:

- **crowdsourced-data-service** – REST API that validates and stores citizen-submitted water quality observations in a persistent SQLite database.
- **rewards-service** – REST API that consumes the crowdsourced data service and awards citizens with points and badges according to configurable rules.
- **api-gateway** – Spring Cloud Gateway project that provides a single entry point for routing requests to the backing services.
- **data/** – Stores the SQLite database file that is generated when the crowdsourced data service runs.

## Running the services

Each service exposes a standalone Spring Boot application. Build and launch them individually from the project root:

```bash
mvn -pl crowdsourced-data-service spring-boot:run
mvn -pl rewards-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

Ports:

| Service | Port |
|---------|------|
| Crowdsourced Data Service | 8081 |
| Rewards Service | 8082 |
| API Gateway | 8080 |

Once all services are running, you can interact through the gateway.

### Example requests

Submit the first observation to automatically register a citizen. The response contains the generated identifier (e.g.
`CTZ-001`) alongside the stored record:

```bash
curl -X POST http://localhost:8080/api/crowdsourced/observations \
  -H 'Content-Type: application/json' \
  -d '{
    "postcode": "NE1 4LP",
    "measurements": {
      "temperature": 12.5,
      "ph": 7.1,
      "alkalinity": 50.3,
      "turbidity": 2.2
    },
    "observations": ["Clear"],
    "images": ["aGVsbG8="]
  }'
```

Use the returned `citizenId` for subsequent submissions and lookups:

```bash
curl -X POST http://localhost:8080/api/crowdsourced/observations/citizen/<citizen-id> \
  -H 'Content-Type: application/json' \
  -d '{
    "postcode": "NE1 4LP",
    "observations": ["Cloudy"]
  }'
```

Retrieve all stored observations for a citizen:

```bash
curl http://localhost:8080/api/crowdsourced/observations/citizen/<citizen-id>
```

Retrieve rewards for a citizen:

```bash
curl http://localhost:8080/api/rewards/<citizen-id>
```

## Testing

Execute the unit and web layer tests across all modules with:

```bash
mvn test
```

## Data storage

The `crowdsourced-data-service` persists submissions inside the SQLite database located at `data/crowdsourced-data.db`.
Spring Boot will create the database file automatically on first run. You can inspect or modify the contents with `sqlite3`
or any SQLite-compatible tool once records have been captured.

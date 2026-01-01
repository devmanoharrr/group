# API Contract for Authority Dashboards

## Overview

This document defines a minimal, consistent API contract that all authority services must implement to support the unified frontend dashboard. The contract is implementation-agnostic and focuses on the data requirements for displaying:

1. Total observation count
2. Recent observations list
3. Leaderboard of top contributors

All authority services (Authority-A through Authority-E) must expose these endpoints, even if they internally call existing microservices or use different data structures.

---

## Base URL

Each authority service exposes these endpoints at its own base URL:
- Authority-A: `http://localhost:8080`
- Authority-B: `http://localhost:8090`
- Authority-C: `http://localhost:8100`
- Authority-D: `http://localhost:8110`
- Authority-E: `http://localhost:8120`

---

## Endpoints

### 1. Get Observation Count

Returns the total number of observations in the system.

**Endpoint:** `GET /api/observations/count`

**Query Parameters:**
- `authority` (optional, string): Filter by authority identifier (for future use)

**Response:**
```json
{
  "count": 42
}
```

**Example Request:**
```
GET /api/observations/count
GET /api/observations/count?authority=authority-b
```

**Example Response:**
```json
{
  "count": 42
}
```

---

### 2. Get Recent Observations

Returns a list of recent observations, ordered by creation time (newest first).

**Endpoint:** `GET /api/observations/recent`

**Query Parameters:**
- `limit` (optional, number): Maximum number of observations to return
  - Default: `5`
  - Minimum: `1`
  - Maximum: `50`
- `authority` (optional, string): Filter by authority identifier (for future use)

**Response:**
```json
[
  {
    "postcode": "SW1A 1AA",
    "measurements": {
      "pH": 7.2,
      "turbidity": 0.5,
      "dissolvedOxygen": 8.1
    },
    "observation": "Water quality appears good",
    "createdAt": "2024-01-15T10:30:00Z",
    "contributorId": "user123"
  },
  {
    "postcode": "NW1 6XE",
    "measurements": {
      "pH": 6.8,
      "turbidity": 1.2,
      "dissolvedOxygen": 7.5
    },
    "observation": "Slight discoloration observed",
    "createdAt": "2024-01-15T09:15:00Z",
    "contributorId": "user456"
  }
]
```

**Response Fields:**
- `postcode` (string, required): Postcode or location identifier
- `measurements` (object, required): Key-value pairs of measurement types and values
  - Common keys: `pH`, `turbidity`, `dissolvedOxygen`
  - Values should be numbers
  - Additional measurement types are allowed
- `observation` (string, optional): Free-text observation notes
- `createdAt` (string, required): ISO 8601 timestamp (UTC)
- `contributorId` (string, required): Identifier of the contributor who created the observation

**Example Request:**
```
GET /api/observations/recent
GET /api/observations/recent?limit=10
GET /api/observations/recent?limit=5&authority=authority-c
```

**Example Response:**
```json
[
  {
    "postcode": "SW1A 1AA",
    "measurements": {
      "pH": 7.2,
      "turbidity": 0.5
    },
    "observation": "Water quality appears good",
    "createdAt": "2024-01-15T10:30:00Z",
    "contributorId": "user123"
  }
]
```

**Ordering:** Results must be ordered by `createdAt` in descending order (newest first).

---

### 3. Get Leaderboard

Returns a list of top contributors ranked by their total points.

**Endpoint:** `GET /api/rewards/leaderboard`

**Query Parameters:**
- `limit` (optional, number): Maximum number of contributors to return
  - Default: `3`
  - Minimum: `1`
  - Maximum: `50`
- `authority` (optional, string): Filter by authority identifier (for future use)

**Response:**
```json
[
  {
    "contributorId": "user123",
    "points": 1250,
    "rank": 1
  },
  {
    "contributorId": "user456",
    "points": 980,
    "rank": 2
  },
  {
    "contributorId": "user789",
    "points": 750,
    "rank": 3
  }
]
```

**Response Fields:**
- `contributorId` (string, required): Identifier of the contributor
- `points` (number, required): Total points earned by the contributor
- `rank` (number, required): Ranking position (1-based, where 1 is the highest)

**Example Request:**
```
GET /api/rewards/leaderboard
GET /api/rewards/leaderboard?limit=5
GET /api/rewards/leaderboard?limit=3&authority=authority-d
```

**Example Response:**
```json
[
  {
    "contributorId": "user123",
    "points": 1250,
    "rank": 1
  },
  {
    "contributorId": "user456",
    "points": 980,
    "rank": 2
  }
]
```

**Ordering:** Results must be ordered by `points` in descending order (highest first).

---

## Error Responses

All endpoints must return errors in the following consistent format:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid limit parameter. Must be between 1 and 50.",
  "path": "/api/observations/recent"
}
```

**Error Response Fields:**
- `timestamp` (string, required): ISO 8601 timestamp (UTC) when the error occurred
- `status` (number, required): HTTP status code
- `error` (string, required): Error type/category
- `message` (string, required): Human-readable error message
- `path` (string, required): The request path that caused the error

---

## HTTP Status Codes

### Success
- **200 OK**: Request succeeded, response contains data

### Client Errors
- **400 Bad Request**: Invalid query parameters (e.g., limit out of range, invalid format)
  - Example: `limit=100` (exceeds maximum of 50)
  - Example: `limit=abc` (not a number)

- **404 Not Found**: Resource not found
  - Example: Invalid endpoint path
  - Example: Requested observation/contributor does not exist

### Server Errors
- **500 Internal Server Error**: Unexpected server error
  - Example: Database connection failure
  - Example: Unhandled exception

- **503 Service Unavailable**: Downstream service unavailable
  - Example: Data service is down (for microservices architectures)
  - Example: Database is unreachable
  - Example: External dependency failure

---

## Implementation Notes

### For Monolithic Services (Authority-A)
- Implement endpoints directly by querying repositories/services
- Map internal data models to the contract response format

### For Microservices (Authority-B, C, D, E)
- Implement adapter endpoints in the API Gateway
- Gateway should call internal services and transform responses to match the contract
- If a downstream service is unavailable, return 503 with proper error JSON
- Do not modify existing microservice endpoints; add new adapter endpoints

### Data Transformation
- If internal data structures differ from the contract, implement transformation logic
- Ensure all required fields are present in responses
- Handle missing optional fields gracefully (use `null` or omit the field)

### Validation
- Validate `limit` parameter: must be a number between 1 and 50
- Return 400 Bad Request if validation fails
- Use default values if optional parameters are not provided

### Performance
- Responses should be reasonably fast (< 2 seconds for typical queries)
- Consider caching for frequently accessed data (leaderboard, counts)

---

## Testing

Each authority should provide at least basic smoke tests for these endpoints:
- Verify endpoint exists and returns 200 OK
- Verify response shape matches the contract
- Verify `limit` parameter validation works
- Verify error responses match the contract format

---

## Versioning

This is version 1.0 of the API contract. Future versions may:
- Add additional query parameters
- Extend response fields
- Add new endpoints

For now, all authorities must implement exactly as specified above.

---

## Examples by Authority

### Authority-A (Monolithic)
```
GET http://localhost:8080/api/observations/count
GET http://localhost:8080/api/observations/recent?limit=5
GET http://localhost:8080/api/rewards/leaderboard?limit=3
```

### Authority-B (Microservices)
```
GET http://localhost:8090/api/observations/count
GET http://localhost:8090/api/observations/recent?limit=5
GET http://localhost:8090/api/rewards/leaderboard?limit=3
```

### Authority-C (Microservices)
```
GET http://localhost:8100/api/observations/count
GET http://localhost:8100/api/observations/recent?limit=5
GET http://localhost:8100/api/rewards/leaderboard?limit=3
```

### Authority-D (Microservices)
```
GET http://localhost:8110/api/observations/count
GET http://localhost:8110/api/observations/recent?limit=5
GET http://localhost:8110/api/rewards/leaderboard?limit=3
```

### Authority-E (Microservices)
```
GET http://localhost:8120/api/observations/count
GET http://localhost:8120/api/observations/recent?limit=5
GET http://localhost:8120/api/rewards/leaderboard?limit=3
```

---

## Summary

All authority services must implement these three endpoints:
1. `GET /api/observations/count` → `{count: number}`
2. `GET /api/observations/recent?limit=5` → `[{postcode, measurements, observation, createdAt, contributorId}]`
3. `GET /api/rewards/leaderboard?limit=3` → `[{contributorId, points, rank}]`

Error responses must follow the standard format with `timestamp`, `status`, `error`, `message`, and `path` fields.


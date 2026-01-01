# Testing Guide - All Services

This guide provides comprehensive testing instructions for all services in the Water Quality Monitoring system.

## Prerequisites

1. **Start all services** using the run script:
   ```bash
   ./scripts/run-all.sh
   ```

2. **Wait for services to start** (approximately 30-60 seconds)

3. **Verify services are running**:
   ```bash
   ./scripts/test-all-services.sh
   ```

---

## Quick Test Script

Run the automated test script:
```bash
chmod +x scripts/test-all-services.sh
./scripts/test-all-services.sh
```

---

## Manual Testing

### 1. Auth Service (Port 8083)

#### Register a new user
```bash
curl -X POST http://localhost:8083/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "email": "test@example.com",
    "name": "Test User"
  }
}
```

#### Login
```bash
curl -X POST http://localhost:8083/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Expected Response:** Same format as register

#### Get current user (requires token)
```bash
# Save token from login response
TOKEN="your-token-here"

curl -X GET http://localhost:8083/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "id": 1,
  "email": "test@example.com",
  "name": "Test User"
}
```

#### Change password (requires token)
```bash
curl -X POST http://localhost:8083/auth/change-password \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "password123",
    "newPassword": "newpassword123"
  }'
```

**Expected Response:**
```json
{
  "message": "Password changed successfully"
}
```

#### Test error handling (wrong password)
```bash
curl -X POST http://localhost:8083/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "wrongpassword"
  }'
```

**Expected Response (401):**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/auth/login"
}
```

---

### 2. Authority-A (Port 8080) - Monolithic

#### Health Check
```bash
curl http://localhost:8080/health
```

#### Home Page
```bash
curl http://localhost:8080/
```

#### Get All Observations
```bash
curl http://localhost:8080/api/crowdsourced/all
```

#### Submit Observation
```bash
curl -X POST http://localhost:8080/api/crowdsourced/submit \
  -H "Content-Type: application/json" \
  -d '{
    "postcode": "SW1A 1AA",
    "measurements": {
      "pH": 7.2,
      "turbidity": 0.5
    },
    "observation": "Water quality appears good",
    "citizenId": "user123"
  }'
```

#### Get Rewards for Citizen
```bash
curl http://localhost:8080/api/rewards/user123
```

---

### 3. Authority-B (Ports 8090, 8091, 8092)

#### Gateway Health
```bash
curl http://localhost:8090/actuator/health
```

#### Data Service Health (Direct)
```bash
curl http://localhost:8091/actuator/health
```

#### Rewards Service Health (Direct)
```bash
curl http://localhost:8092/actuator/health
```

#### Get Observations via Gateway
```bash
curl http://localhost:8090/data/observations
```

#### Get Stats via Gateway
```bash
curl http://localhost:8090/data/stats
```

#### Get Leaderboard via Gateway
```bash
curl http://localhost:8090/rewards/leaderboard
```

#### Submit Observation via Gateway
```bash
curl -X POST http://localhost:8090/data/submit \
  -H "Content-Type: application/json" \
  -d '{
    "postcode": "NE1 4LP",
    "measurements": {
      "pH": 7.1,
      "turbidity": 2.2
    },
    "observations": ["Clear"],
    "citizenId": "CTZ-001"
  }'
```

---

### 4. Authority-C (Ports 8100, 8101, 8102)

#### Gateway Health
```bash
curl http://localhost:8100/healthz
```

#### Data Service Health (Direct)
```bash
curl http://localhost:8101/healthz
```

#### Rewards Service Health (Direct)
```bash
curl http://localhost:8102/healthz
```

#### Get Latest Observations
```bash
curl "http://localhost:8100/api/data/observations/latest?limit=5"
```

#### Get Observations Count
```bash
curl http://localhost:8100/api/data/observations/count
```

#### Get Leaderboard
```bash
curl http://localhost:8100/api/rewards/leaderboard
```

#### Create Observation
```bash
curl -X POST http://localhost:8100/api/data/observations \
  -H "Content-Type: application/json" \
  -d '{
    "postcode": "NW1 6XE",
    "authority": "authority-c",
    "measurements": {
      "pH": 6.8,
      "turbidity": 1.2
    }
  }'
```

---

### 5. Authority-D (Ports 8110, 8111, 8112)

#### Gateway Health
```bash
curl http://localhost:8110/health
```

#### Data Service Health (Direct)
```bash
curl http://localhost:8111/actuator/health
```

#### Rewards Service Health (Direct)
```bash
curl http://localhost:8112/actuator/health
```

#### List Observations
```bash
curl http://localhost:8110/api/observations
```

#### Create Observation
```bash
curl -X POST http://localhost:8110/api/observations \
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
    "observations": ["Clear"]
  }'
```

#### Get Rewards for Citizen
```bash
curl http://localhost:8110/api/rewards/citizen-001
```

---

### 6. Authority-E (Ports 8120, 8121, 8122)

#### Gateway Health
```bash
curl http://localhost:8120/actuator/health
```

#### Data Service Health (Direct)
```bash
curl http://localhost:8121/actuator/health
```

#### Rewards Service Health (Direct)
```bash
curl http://localhost:8122/actuator/health
```

#### Get Observations
```bash
curl http://localhost:8120/api/crowdsourced/observations
```

#### Submit Observation
```bash
curl -X POST http://localhost:8120/api/crowdsourced/observations \
  -H "Content-Type: application/json" \
  -d '{
    "postcode": "NE1 4LP",
    "measurements": {
      "temperature": 12.5,
      "ph": 7.1,
      "alkalinity": 50.3,
      "turbidity": 2.2
    },
    "observations": ["Clear"],
    "images": []
  }'
```

#### Get Rewards
```bash
curl http://localhost:8120/api/rewards/rewards
```

---

## Testing Checklist

### Auth Service
- [ ] Register new user
- [ ] Login with correct credentials
- [ ] Login with wrong password (should return 401)
- [ ] Get current user with valid token
- [ ] Get current user without token (should return 401)
- [ ] Change password with valid token
- [ ] Change password with wrong old password (should return 401)

### Authority-A
- [ ] Health check returns 200
- [ ] Home page accessible
- [ ] Get all observations
- [ ] Submit observation
- [ ] Get rewards for citizen

### Authority-B
- [ ] Gateway health check
- [ ] Data service health (direct)
- [ ] Rewards service health (direct)
- [ ] Get observations via gateway
- [ ] Get stats via gateway
- [ ] Get leaderboard via gateway
- [ ] Submit observation via gateway

### Authority-C
- [ ] Gateway health check
- [ ] Data service health (direct)
- [ ] Rewards service health (direct)
- [ ] Get latest observations
- [ ] Get observations count
- [ ] Get leaderboard
- [ ] Create observation

### Authority-D
- [ ] Gateway health check
- [ ] Data service health (direct)
- [ ] Rewards service health (direct)
- [ ] List observations
- [ ] Create observation
- [ ] Get rewards for citizen

### Authority-E
- [ ] Gateway health check
- [ ] Data service health (direct)
- [ ] Rewards service health (direct)
- [ ] Get observations
- [ ] Submit observation
- [ ] Get rewards

---

## Troubleshooting

### Service not responding
1. Check if service is running: `lsof -i :PORT`
2. Check service logs: `tail -f scripts/logs/SERVICE_NAME.log`
3. Verify port configuration in `application.properties` or `application.yml`

### Connection refused
- Ensure all services are started
- Check for port conflicts
- Verify firewall settings

### 503 Service Unavailable
- Check if downstream services are running
- Verify service URLs in gateway configuration
- Check service logs for errors

### 401 Unauthorized
- Verify token is valid and not expired
- Check Authorization header format: `Bearer TOKEN`
- Ensure token was obtained from login/register endpoint

---

## Next Steps

After testing all services:
1. Verify all endpoints respond correctly
2. Check error handling (wrong credentials, missing fields, etc.)
3. Test service-to-service communication (gateways calling data/rewards services)
4. Proceed to Step 3: Standardize authority services (add adapter endpoints)


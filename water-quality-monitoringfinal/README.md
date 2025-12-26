# Water Quality Monitoring System

A citizen science project for monitoring water quality across communities.

## What is this project?

This is a microservices-based application that helps citizens report water quality observations and get rewarded for their participation. Local authorities can use this data to monitor water safety across different areas.

## How it works

The system has three main parts:

**1. Data Collection Service** - Where citizens submit their water quality observations
- Records things like pH, temperature, turbidity, and visual observations
- Stores everything in a database for later analysis
- Runs on port 8081

**2. Rewards Service** - Encourages people to participate
- Gives points to citizens who submit observations (10 points normally, 20 for complete data)
- Awards badges when they reach milestones (Bronze at 100 points, Silver at 200, Gold at 500)
- Shows leaderboards of top contributors
- Runs on port 8082

**3. API Gateway** - Makes everything easy to access
- Single entry point for all features
- Routes requests to the right service automatically
- Runs on port 8080

## What you need

- Java 17 or newer
- Maven (for building the project)
- Any IDE you like (IntelliJ IDEA, Eclipse, VS Code, etc.)

## Getting started

### Building the project

Open three terminal windows and run these commands:

**Terminal 1 - Data Service:**
```bash
cd crowdsourced-data-service
mvn clean install
mvn spring-boot:run
```

**Terminal 2 - Rewards Service:**
```bash
cd rewards-service
mvn clean install
mvn spring-boot:run
```

**Terminal 3 - API Gateway:**
```bash
cd api-gateway
mvn clean install
mvn spring-boot:run
```

Wait for all three to start up. You'll see success messages when they're ready.

### Testing it out

Once everything is running, you can test the APIs using Postman or any HTTP client.

**Submit a water quality observation:**
```bash
POST http://localhost:8080/data/submit

{
  "citizenId": "yourname123",
  "postcode": "NE1 8ST",
  "measurements": {
    "temperature": 18.5,
    "ph": 7.2,
    "alkalinity": 120.0,
    "turbidity": 2.5
  },
  "observations": ["Clear"],
  "images": ["photo.jpg"]
}
```

**Check the statistics:**
```bash
GET http://localhost:8080/data/stats
```

**Process rewards and see the leaderboard:**
```bash
POST http://localhost:8080/rewards/process
GET http://localhost:8080/rewards/leaderboard
```

## Project structure

```
water-quality-monitoring/
├── crowdsourced-data-service/    # Handles data collection
├── rewards-service/              # Manages points and badges
├── api-gateway/                  # Routes all requests
└── data/                         # Database files
    └── water-quality.db
```

## Technologies used

- **Java 17** - Programming language
- **Spring Boot** - Framework for building the services
- **Spring Cloud Gateway** - For the API gateway
- **SQLite** - Database for storing observations
- **JPA/Hibernate** - Database access
- **JUnit & Mockito** - Testing
- **Maven** - Build tool

## The points system

**How citizens earn points:**
- Submit any valid observation: 10 points
- Submit complete observation (all measurements + visual notes): 20 points

**Badges earned:**
-    Bronze: 100 points
-    Silver: 200 points
-    Gold: 500 points

## What can be submitted

**Required:**
- Postcode (where you took the measurement)
- At least one measurement OR one observation

**Measurements (all optional):**
- Temperature (in °C)
- pH level
- Alkalinity (mg/L)
- Turbidity (NTU)

**Visual observations (pick what applies):**
- Clear
- Cloudy
- Murky
- Foamy
- Oily
- Discoloured
- Presence of Odour

**You can also add up to 3 photos!**

## Running tests

Each service has unit tests. To run them:

```bash
cd crowdsourced-data-service
mvn test
```

Same for the other services. You should see all tests passing.

## Common issues

**"Port already in use"**
- Close any other running instances
- Or change the port in application.properties

**"Connection refused"**
- Make sure the Data Service is running before starting Rewards Service
- The services need to start in order (Data → Rewards → Gateway)

**"Database locked"**
- Only one service should access the database at a time
- Restart the Data Service if this happens

## API documentation

When the services are running, you can view interactive API documentation:

- Data Service: http://localhost:8081/swagger-ui.html
- Rewards Service: http://localhost:8082/swagger-ui.html (if enabled)

Or just check the startup messages - they list all available endpoints!

## Sample data

The database comes with 10 sample observations from different citizens across various UK postcodes. This is just for testing and demonstration.

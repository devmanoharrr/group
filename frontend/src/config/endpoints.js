/**
 * API Endpoints Configuration
 * Defines all available endpoints for each authority
 */

export const AUTHORITY_ENDPOINTS = {
  a: [
    // Contract endpoints
    { method: 'GET', path: '/api/observations/count', description: 'Get total observation count' },
    { method: 'GET', path: '/api/observations/recent', description: 'Get recent observations', queryParams: 'limit=5' },
    { method: 'GET', path: '/api/rewards/leaderboard', description: 'Get leaderboard', queryParams: 'limit=3' },
    // Original endpoints
    { method: 'GET', path: '/api/crowdsourced/all', description: 'Get all crowdsourced observations' },
    { method: 'GET', path: '/api/crowdsourced/citizen/CITZ1234', description: 'Get observations by citizen ID (example: CITZ1234)' },
    { method: 'POST', path: '/api/crowdsourced/submit', description: 'Submit new observation', body: '{"postcode":"SW1A 1AA","ph":7.2,"temperature":22.5,"citizenId":"user123","observations":["Clear","Test"]}' },
    { method: 'GET', path: '/api/rewards/CITZ1234', description: 'Get rewards for citizen (example: CITZ1234)' },
    { method: 'POST', path: '/api/rewards/processAll', description: 'Process all rewards' },
  ],
  b: [
    // Contract endpoints
    { method: 'GET', path: '/api/observations/count', description: 'Get total observation count' },
    { method: 'GET', path: '/api/observations/recent', description: 'Get recent observations', queryParams: 'limit=5' },
    { method: 'GET', path: '/api/rewards/leaderboard', description: 'Get leaderboard', queryParams: 'limit=3' },
    // Data service endpoints (via gateway)
    { method: 'GET', path: '/data/observations', description: 'Get all observations', queryParams: 'limit=10' },
    { method: 'GET', path: '/data/stats', description: 'Get statistics' },
    { method: 'GET', path: '/data/citizen/invalidForeignKeyUser', description: 'Get observations by citizen ID (example: invalidForeignKeyUser)' },
    { method: 'POST', path: '/data/submit', description: 'Submit observation', body: '{"postcode":"NE1 4LP","citizenId":"CTZ-001","measurements":{"temperature":18.5,"ph":7.1},"observations":["Clear","Foamy"]}' },
    // Rewards service endpoints
    { method: 'GET', path: '/rewards/leaderboard', description: 'Get leaderboard (original)', queryParams: 'top=10' },
    { method: 'GET', path: '/rewards/points/invalidForeignKeyUser', description: 'Get rewards for citizen (example: invalidForeignKeyUser)' },
    { method: 'POST', path: '/rewards/process', description: 'Process rewards' },
    { method: 'GET', path: '/actuator/health', description: 'Gateway health check' },
  ],
  c: [
    // Contract endpoints
    { method: 'GET', path: '/api/observations/count', description: 'Get total observation count' },
    { method: 'GET', path: '/api/observations/recent', description: 'Get recent observations', queryParams: 'limit=5' },
    { method: 'GET', path: '/api/rewards/leaderboard', description: 'Get leaderboard', queryParams: 'limit=3' },
    // Data service endpoints (via gateway proxy)
    { method: 'GET', path: '/api/data/observations/count', description: 'Get count (data service proxy)' },
    { method: 'GET', path: '/api/data/observations/latest', description: 'Get latest observations', queryParams: 'limit=10' },
    { method: 'POST', path: '/api/data/observations', description: 'Create observation', body: '{"citizenId":"citizen-123","postcode":"NE1 4LP","authority":"NE","measurements":{"temperatureCelsius":18.5,"ph":7.1},"observations":["CLEAR"]}' },
    // Rewards service endpoints (via gateway proxy)
    { method: 'GET', path: '/api/rewards/leaderboard', description: 'Get leaderboard (with authority)', queryParams: 'authority=authority-c&limit=3' },
    { method: 'GET', path: '/api/rewards/citizens/citizen-123', description: 'Get rewards for citizen (example: citizen-123)' },
    { method: 'POST', path: '/api/rewards/ingest', description: 'Ingest observation event' },
    { method: 'GET', path: '/healthz', description: 'Gateway health check' },
  ],
  d: [
    // Contract endpoints
    { method: 'GET', path: '/api/observations/count', description: 'Get total observation count' },
    { method: 'GET', path: '/api/observations/recent', description: 'Get recent observations', queryParams: 'limit=5' },
    { method: 'GET', path: '/api/rewards/leaderboard', description: 'Get leaderboard', queryParams: 'limit=3' },
    // Original endpoints
    { method: 'GET', path: '/api/observations', description: 'List all observations' },
    { method: 'GET', path: '/api/observations', description: 'Get observations by citizen', queryParams: 'citizenId=citizen-001' },
    { method: 'POST', path: '/api/observations', description: 'Create observation', body: '{"citizenId":"citizen-001","postcode":"NE1 1AA","measurements":{"temperatureCelsius":12.5,"ph":7.1,"alkalinityMgPerL":40.0,"turbidityNtu":2.0},"observations":["Clear"]}' },
    { method: 'GET', path: '/api/rewards/citizen-001', description: 'Get rewards for citizen (example: citizen-001)' },
    { method: 'POST', path: '/api/rewards/recompute/citizen-001', description: 'Recompute rewards for citizen (example: citizen-001)' },
    { method: 'GET', path: '/health', description: 'Gateway health check' },
  ],
  e: [
    // Contract endpoints
    { method: 'GET', path: '/api/observations/count', description: 'Get total observation count' },
    { method: 'GET', path: '/api/observations/recent', description: 'Get recent observations', queryParams: 'limit=5' },
    { method: 'GET', path: '/api/rewards/leaderboard', description: 'Get leaderboard', queryParams: 'limit=3' },
    // Original endpoints (via gateway routes)
    { method: 'GET', path: '/api/crowdsourced/observations', description: 'List all observations (via gateway)' },
    { method: 'GET', path: '/api/crowdsourced/observations/citizen/CTZ-001', description: 'Get observations by citizen (example: CTZ-001)' },
    { method: 'POST', path: '/api/crowdsourced/observations', description: 'Create observation for new citizen', body: '{"postcode":"NE1 4LP","measurements":{"temperature":18.5,"ph":7.2,"alkalinity":120.0,"turbidity":1.5},"observations":["CLEAR"]}' },
    { method: 'POST', path: '/api/crowdsourced/observations/citizen/CTZ-001', description: 'Create observation for existing citizen (example: CTZ-001)', body: '{"postcode":"NE1 4LP","measurements":{"temperature":18.5,"ph":7.2},"observations":["CLEAR"]}' },
    { method: 'GET', path: '/api/rewards/CTZ-001', description: 'Get rewards for citizen (example: CTZ-001)' },
    { method: 'GET', path: '/api/rewards', description: 'Get all cached rewards' },
    { method: 'GET', path: '/actuator/health', description: 'Gateway health check' },
  ],
};


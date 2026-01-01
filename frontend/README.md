# Water Quality Monitoring Frontend

Vite React frontend for the Water Quality Monitoring System.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create `.env.local` file (or use `.env`) with the following variables:
```
VITE_AUTH_SERVICE_URL=http://localhost:8083
VITE_AUTHORITY_A_URL=http://localhost:8080
VITE_AUTHORITY_B_URL=http://localhost:8090
VITE_AUTHORITY_C_URL=http://localhost:8100
VITE_AUTHORITY_D_URL=http://localhost:8110
VITE_AUTHORITY_E_URL=http://localhost:8120
```

3. Start development server:
```bash
npm run dev
```

The app will be available at `http://localhost:5173`

## Features

- **Authentication**: Login, Register, Change Password
- **Protected Routes**: All dashboard pages require authentication
- **Hub Page**: Central navigation to all authority dashboards
- **Dashboard Pages**: Individual dashboards for each authority showing:
  - Total observation count
  - Recent observations (top 5)
  - Leaderboard (top 3 contributors)
- **Error Handling**: Graceful error handling with retry functionality
- **Responsive Design**: Clean, modern UI

## Project Structure

```
src/
├── components/          # Reusable components
│   ├── Dashboard.jsx
│   ├── ErrorBanner.jsx
│   ├── Leaderboard.jsx
│   ├── Loading.jsx
│   ├── MetricCard.jsx
│   ├── ProtectedRoute.jsx
│   └── RecentObservationsTable.jsx
├── config/            # Configuration
│   └── api.js         # API base URLs
├── context/           # React Context
│   └── AuthContext.jsx
├── pages/             # Page components
│   ├── ChangePassword.jsx
│   ├── DashboardAuthorityA.jsx
│   ├── DashboardAuthorityB.jsx
│   ├── DashboardAuthorityC.jsx
│   ├── DashboardAuthorityD.jsx
│   ├── Hub.jsx
│   ├── Login.jsx
│   └── Register.jsx
└── utils/             # Utility functions
    └── api.js         # API client with error handling
```

## Authentication

The app uses JWT tokens stored in localStorage. The token is automatically attached to all API requests via the `Authorization: Bearer <token>` header.

**Note**: Storing tokens in localStorage has security implications (XSS vulnerability). For production, consider using httpOnly cookies or secure storage mechanisms.

## API Integration

All API calls go through the centralized `apiRequest` utility in `src/utils/api.js`, which provides:
- Automatic token attachment
- Request timeout (10 seconds default)
- Error normalization
- Friendly error messages

## Build

To build for production:
```bash
npm run build
```

The built files will be in the `dist/` directory.

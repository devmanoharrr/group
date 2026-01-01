import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import ChangePassword from './pages/ChangePassword';
import Hub from './pages/Hub';
import DashboardAuthorityA from './pages/DashboardAuthorityA';
import DashboardAuthorityB from './pages/DashboardAuthorityB';
import DashboardAuthorityC from './pages/DashboardAuthorityC';
import DashboardAuthorityD from './pages/DashboardAuthorityD';
import DashboardAuthorityE from './pages/DashboardAuthorityE';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/change-password"
            element={
              <ProtectedRoute>
                <ChangePassword />
              </ProtectedRoute>
            }
          />
          <Route
            path="/hub"
            element={
              <ProtectedRoute>
                <Hub />
              </ProtectedRoute>
            }
          />
          <Route
            path="/dashboard/authority-a"
            element={
              <ProtectedRoute>
                <DashboardAuthorityA />
              </ProtectedRoute>
            }
          />
          <Route
            path="/dashboard/authority-b"
            element={
              <ProtectedRoute>
                <DashboardAuthorityB />
              </ProtectedRoute>
            }
          />
          <Route
            path="/dashboard/authority-c"
            element={
              <ProtectedRoute>
                <DashboardAuthorityC />
              </ProtectedRoute>
            }
          />
          <Route
            path="/dashboard/authority-d"
            element={
              <ProtectedRoute>
                <DashboardAuthorityD />
              </ProtectedRoute>
            }
          />
          <Route
            path="/dashboard/authority-e"
            element={
              <ProtectedRoute>
                <DashboardAuthorityE />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/hub" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;

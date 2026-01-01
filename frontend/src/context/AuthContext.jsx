import { createContext, useContext, useState, useEffect } from 'react';
import { apiRequest } from '../utils/api';
import { API_CONFIG } from '../config/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  const getAuthErrorMessage = (error) => {
    if (error.status === 400) {
      return error.data?.message || 'Invalid request';
    }
    if (error.status === 401) {
      return 'Invalid credentials';
    }
    return error.message || 'An error occurred';
  };

  // Load token from localStorage on mount
  useEffect(() => {
    const storedToken = localStorage.getItem('authToken');
    if (storedToken) {
      setToken(storedToken);
      // Optionally verify token by fetching user info
      fetchUserInfo(storedToken);
    } else {
      setLoading(false);
    }
  }, []);

  const fetchUserInfo = async (authToken) => {
    try {
      const { data } = await apiRequest(`${API_CONFIG.auth.baseUrl}/auth/me`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
        },
      });
      setUser(data);
      setToken(authToken);
    } catch {
      // Token invalid, clear it
      localStorage.removeItem('authToken');
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    try {
      const { data } = await apiRequest(`${API_CONFIG.auth.baseUrl}/auth/login`, {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });

      const authToken = data.token;
      localStorage.setItem('authToken', authToken);
      setToken(authToken);
      setUser(data.user);
      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: getAuthErrorMessage(error),
      };
    }
  };

  const register = async (name, email, password) => {
    try {
      const { data } = await apiRequest(`${API_CONFIG.auth.baseUrl}/auth/register`, {
        method: 'POST',
        body: JSON.stringify({ name, email, password }),
      });

      const authToken = data.token;
      localStorage.setItem('authToken', authToken);
      setToken(authToken);
      setUser(data.user);
      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: getAuthErrorMessage(error),
      };
    }
  };

  const changePassword = async (oldPassword, newPassword) => {
    try {
      await apiRequest(`${API_CONFIG.auth.baseUrl}/auth/change-password`, {
        method: 'POST',
        body: JSON.stringify({ oldPassword, newPassword }),
      });
      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: getAuthErrorMessage(error),
      };
    }
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    setToken(null);
    setUser(null);
  };

  const value = {
    user,
    token,
    loading,
    login,
    register,
    changePassword,
    logout,
    isAuthenticated: !!token,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}


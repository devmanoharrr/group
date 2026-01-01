import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { theme, getButtonStyle, getInputStyle, getCardStyle } from '../styles/theme';
import ErrorBanner from '../components/ErrorBanner';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    const result = await login(email, password);

    if (result.success) {
      navigate('/hub');
    } else {
      setError(result.error);
    }

    setLoading(false);
  };

  const buttonStyle = getButtonStyle('primary');
  const inputStyle = getInputStyle();

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      backgroundColor: theme.colors.bgSecondary,
      padding: theme.spacing.md,
    }}>
      <div style={{
        ...getCardStyle(false),
        width: '100%',
        maxWidth: '440px',
      }}>
        <div style={{ textAlign: 'center', marginBottom: theme.spacing.xl }}>
          <h1 style={{ 
            marginBottom: theme.spacing.sm,
            fontSize: '2rem',
            fontWeight: '700',
            color: theme.colors.textPrimary,
            background: `linear-gradient(135deg, ${theme.colors.primary} 0%, ${theme.colors.secondary} 100%)`,
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}>
            Welcome Back
          </h1>
          <p style={{ 
            color: theme.colors.textSecondary,
            fontSize: '0.9375rem',
          }}>
            Sign in to access the Water Quality Monitoring System
          </p>
        </div>
        
        <ErrorBanner error={error} onDismiss={() => setError(null)} />

        <form onSubmit={handleSubmit} style={{ marginTop: theme.spacing.lg }}>
          <div style={{ marginBottom: theme.spacing.lg }}>
            <label style={{ 
              display: 'block', 
              marginBottom: theme.spacing.sm, 
              fontWeight: '600',
              color: theme.colors.textPrimary,
              fontSize: '0.9375rem',
            }}>
              Email Address
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="you@example.com"
              style={inputStyle}
            />
          </div>

          <div style={{ marginBottom: theme.spacing.xl }}>
            <label style={{ 
              display: 'block', 
              marginBottom: theme.spacing.sm, 
              fontWeight: '600',
              color: theme.colors.textPrimary,
              fontSize: '0.9375rem',
            }}>
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="Enter your password"
              style={inputStyle}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            style={{
              ...buttonStyle,
              width: '100%',
            }}
          >
            {loading ? 'Logging in...' : 'Sign In'}
          </button>
        </form>

        <p style={{ 
          marginTop: theme.spacing.xl, 
          textAlign: 'center', 
          color: theme.colors.textSecondary,
          fontSize: '0.9375rem',
        }}>
          Don't have an account?{' '}
          <Link to="/register" style={{ 
            color: theme.colors.primary,
            fontWeight: '600',
            textDecoration: 'none',
          }}>
            Create Account
          </Link>
        </p>
      </div>
    </div>
  );
}


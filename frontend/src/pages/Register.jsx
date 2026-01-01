import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { theme, getButtonStyle, getInputStyle, getCardStyle } from '../styles/theme';
import ErrorBanner from '../components/ErrorBanner';

export default function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);

    const result = await register(name, email, password);

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
            Create Account
          </h1>
          <p style={{ 
            color: theme.colors.textSecondary,
            fontSize: '0.9375rem',
          }}>
            Join the Water Quality Monitoring System
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
              Full Name
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              placeholder="John Doe"
              style={inputStyle}
            />
          </div>

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

          <div style={{ marginBottom: theme.spacing.lg }}>
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
              placeholder="At least 6 characters"
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
              Confirm Password
            </label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              placeholder="Re-enter your password"
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
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <p style={{ 
          marginTop: theme.spacing.xl, 
          textAlign: 'center', 
          color: theme.colors.textSecondary,
          fontSize: '0.9375rem',
        }}>
          Already have an account?{' '}
          <Link to="/login" style={{ 
            color: theme.colors.primary,
            fontWeight: '600',
            textDecoration: 'none',
          }}>
            Sign In
          </Link>
        </p>
      </div>
    </div>
  );
}


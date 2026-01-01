import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { theme, getButtonStyle, getInputStyle, getCardStyle } from '../styles/theme';
import ErrorBanner from '../components/ErrorBanner';

export default function ChangePassword() {
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const { changePassword } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);

    if (newPassword !== confirmPassword) {
      setError('New passwords do not match');
      return;
    }

    if (newPassword.length < 6) {
      setError('New password must be at least 6 characters');
      return;
    }

    setLoading(true);

    const result = await changePassword(oldPassword, newPassword);

    if (result.success) {
      setSuccess(true);
      setTimeout(() => {
        navigate('/hub');
      }, 2000);
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
            Change Password
          </h1>
          <p style={{ 
            color: theme.colors.textSecondary,
            fontSize: '0.9375rem',
          }}>
            Update your account password
          </p>
        </div>
        
        {success && (
          <div style={{
            backgroundColor: '#d1fae5',
            border: `1px solid ${theme.colors.success}`,
            borderRadius: theme.borderRadius.md,
            padding: theme.spacing.md,
            marginBottom: theme.spacing.lg,
            color: theme.colors.secondaryDark,
          }}>
            ✓ Password changed successfully! Redirecting...
          </div>
        )}

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
              Current Password
            </label>
            <input
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              required
              placeholder="Enter current password"
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
              New Password
            </label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
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
              Confirm New Password
            </label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              placeholder="Re-enter new password"
              style={inputStyle}
            />
          </div>

          <button
            type="submit"
            disabled={loading || success}
            style={{
              ...buttonStyle,
              width: '100%',
            }}
          >
            {loading ? 'Changing...' : success ? 'Success!' : 'Change Password'}
          </button>
        </form>
      </div>
    </div>
  );
}


import { theme } from '../styles/theme';

export default function Loading({ message = 'Loading...' }) {
  return (
    <div style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      alignItems: 'center', 
      justifyContent: 'center',
      minHeight: '400px',
      padding: theme.spacing['2xl'],
    }}>
      <div style={{
        width: '48px',
        height: '48px',
        border: `4px solid ${theme.colors.gray200}`,
        borderTop: `4px solid ${theme.colors.primary}`,
        borderRadius: theme.borderRadius.full,
        animation: 'spin 1s linear infinite',
      }}></div>
      <p style={{ 
        marginTop: theme.spacing.lg, 
        color: theme.colors.textSecondary,
        fontSize: '0.9375rem',
        fontWeight: '500',
      }}>
        {message}
      </p>
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}


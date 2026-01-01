import { theme, getButtonStyle } from '../styles/theme';

export default function ErrorBanner({ error, onRetry, onDismiss }) {
  if (!error) return null;

  return (
    <div style={{
      backgroundColor: '#fee2e2',
      border: `1px solid ${theme.colors.danger}`,
      borderRadius: theme.borderRadius.md,
      padding: theme.spacing.md,
      marginBottom: theme.spacing.md,
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      gap: theme.spacing.md,
    }}>
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: theme.spacing.sm }}>
        <span style={{ fontSize: '1.25rem' }}>⚠️</span>
        <div>
          <strong style={{ color: theme.colors.dangerDark, display: 'block', marginBottom: theme.spacing.xs }}>
            Error:
          </strong>
          <span style={{ color: theme.colors.textPrimary }}>{error}</span>
        </div>
      </div>
      <div style={{ display: 'flex', gap: theme.spacing.sm, flexShrink: 0 }}>
        {onRetry && (
          <button
            onClick={onRetry}
            style={getButtonStyle('primary', 'sm')}
          >
            Retry
          </button>
        )}
        {onDismiss && (
          <button
            onClick={onDismiss}
            style={getButtonStyle('ghost', 'sm')}
          >
            Dismiss
          </button>
        )}
      </div>
    </div>
  );
}


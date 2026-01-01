export default function ErrorBanner({ error, onRetry, onDismiss }) {
  if (!error) return null;

  return (
    <div style={{
      backgroundColor: '#fee',
      border: '1px solid #fcc',
      borderRadius: '4px',
      padding: '1rem',
      marginBottom: '1rem',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
    }}>
      <div style={{ flex: 1 }}>
        <strong style={{ color: '#c33' }}>Error:</strong>
        <span style={{ marginLeft: '0.5rem', color: '#333' }}>{error}</span>
      </div>
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        {onRetry && (
          <button
            onClick={onRetry}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#3498db',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            Retry
          </button>
        )}
        {onDismiss && (
          <button
            onClick={onDismiss}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#95a5a6',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            Dismiss
          </button>
        )}
      </div>
    </div>
  );
}


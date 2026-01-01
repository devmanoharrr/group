import { theme, getCardStyle } from '../styles/theme';

export default function MetricCard({ title, value, label }) {
  return (
    <div style={{
      ...getCardStyle(false),
      borderTop: `4px solid ${theme.colors.primary}`,
      position: 'relative',
      overflow: 'hidden',
    }}>
      <div style={{
        position: 'absolute',
        top: 0,
        right: 0,
        width: '120px',
        height: '120px',
        background: `linear-gradient(135deg, ${theme.colors.primary}10 0%, transparent 100%)`,
        borderRadius: '0 0 0 100%',
      }} />
      <div style={{ position: 'relative', zIndex: 1 }}>
      <h3 style={{ 
          margin: '0 0 0.75rem 0', 
          fontSize: '0.875rem', 
          color: theme.colors.textSecondary,
          fontWeight: '500',
          textTransform: 'uppercase',
          letterSpacing: '0.05em',
      }}>
        {title}
      </h3>
      <div style={{ 
          fontSize: '2.5rem', 
          fontWeight: '700', 
          color: theme.colors.textPrimary,
          lineHeight: '1.2',
          marginBottom: label ? theme.spacing.sm : 0,
      }}>
        {value !== null && value !== undefined ? value.toLocaleString() : '—'}
      </div>
      {label && (
        <p style={{ 
            margin: 0, 
            fontSize: '0.875rem', 
            color: theme.colors.textTertiary,
        }}>
          {label}
        </p>
      )}
      </div>
    </div>
  );
}


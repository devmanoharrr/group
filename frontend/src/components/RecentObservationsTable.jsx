import { theme, getCardStyle } from '../styles/theme';

export default function RecentObservationsTable({ observations, loading, error }) {
  if (loading) {
    return (
      <div style={{ 
        ...getCardStyle(false),
        padding: theme.spacing['2xl'],
        textAlign: 'center',
        color: theme.colors.textSecondary,
      }}>
        <div style={{ fontSize: '0.9375rem' }}>Loading observations...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ 
        ...getCardStyle(false),
        padding: theme.spacing['2xl'],
        textAlign: 'center',
        color: theme.colors.danger,
      }}>
        {error}
      </div>
    );
  }

  if (!observations || observations.length === 0) {
    return (
      <div style={{ 
        ...getCardStyle(false),
        padding: theme.spacing['2xl'],
        textAlign: 'center',
        color: theme.colors.textSecondary,
      }}>
        No observations found.
      </div>
    );
  }

  return (
    <div style={{
      ...getCardStyle(false),
      overflow: 'hidden',
      padding: 0,
    }}>
      <div style={{
        backgroundColor: theme.colors.gray50,
        padding: theme.spacing.lg,
        borderBottom: `1px solid ${theme.colors.borderLight}`,
        fontWeight: '600',
        color: theme.colors.textPrimary,
        fontSize: '1.125rem',
      }}>
        Recent Observations
      </div>
      <div style={{ overflowX: 'auto' }}>
        <table style={{ 
          width: '100%', 
          borderCollapse: 'collapse',
        }}>
        <thead>
            <tr style={{ 
              backgroundColor: theme.colors.gray50,
              borderBottom: `2px solid ${theme.colors.borderLight}`,
            }}>
              <th style={{ 
                padding: theme.spacing.md, 
                textAlign: 'left',
                fontWeight: '600',
                color: theme.colors.textSecondary,
                fontSize: '0.875rem',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}>
              Postcode
            </th>
              <th style={{ 
                padding: theme.spacing.md, 
                textAlign: 'left',
                fontWeight: '600',
                color: theme.colors.textSecondary,
                fontSize: '0.875rem',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}>
              Measurements
            </th>
              <th style={{ 
                padding: theme.spacing.md, 
                textAlign: 'left',
                fontWeight: '600',
                color: theme.colors.textSecondary,
                fontSize: '0.875rem',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}>
              Observation
            </th>
              <th style={{ 
                padding: theme.spacing.md, 
                textAlign: 'left',
                fontWeight: '600',
                color: theme.colors.textSecondary,
                fontSize: '0.875rem',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}>
              Date
            </th>
              <th style={{ 
                padding: theme.spacing.md, 
                textAlign: 'left',
                fontWeight: '600',
                color: theme.colors.textSecondary,
                fontSize: '0.875rem',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}>
              Contributor
            </th>
          </tr>
        </thead>
        <tbody>
          {observations.map((obs, index) => (
              <tr 
                key={index} 
                style={{ 
                  borderBottom: `1px solid ${theme.colors.borderLight}`,
                  transition: `background-color ${theme.transitions.fast}`,
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = theme.colors.gray50;
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = 'transparent';
                }}
              >
                <td style={{ 
                  padding: theme.spacing.md,
                  fontWeight: '500',
                  color: theme.colors.textPrimary,
                }}>
                  {obs.postcode || '—'}
                </td>
                <td style={{ padding: theme.spacing.md }}>
                {obs.measurements && Object.keys(obs.measurements).length > 0 ? (
                    <div style={{ fontSize: '0.875rem' }}>
                      {Object.entries(obs.measurements).slice(0, 3).map(([key, value]) => (
                        <div key={key} style={{ marginBottom: theme.spacing.xs }}>
                          <span style={{ fontWeight: '600', color: theme.colors.textSecondary }}>
                            {key}:
                          </span>{' '}
                          <span style={{ color: theme.colors.textPrimary }}>
                            {typeof value === 'number' ? value.toFixed(2) : value}
                          </span>
                      </div>
                    ))}
                  </div>
                ) : '—'}
              </td>
                <td style={{ 
                  padding: theme.spacing.md, 
                  fontSize: '0.875rem',
                  color: theme.colors.textSecondary,
                  maxWidth: '200px',
                }}>
                {obs.observation || '—'}
              </td>
                <td style={{ 
                  padding: theme.spacing.md, 
                  fontSize: '0.875rem', 
                  color: theme.colors.textSecondary,
                }}>
                {obs.createdAt ? new Date(obs.createdAt).toLocaleString() : '—'}
              </td>
                <td style={{ 
                  padding: theme.spacing.md, 
                  fontSize: '0.875rem', 
                  color: theme.colors.textSecondary,
                }}>
                {obs.contributorId || '—'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      </div>
    </div>
  );
}


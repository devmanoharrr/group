import { theme, getCardStyle } from '../styles/theme';

// Format contributor ID to a more user-friendly name
function formatContributorName(id) {
  if (!id || id === 'Unknown') return 'Unknown';
  
  // If it's a simple name like "alice", capitalize it
  if (/^[a-z]+$/.test(id)) {
    return id.charAt(0).toUpperCase() + id.slice(1);
  }
  
  // If it's "citizen-123", format as "Citizen #123"
  if (id.startsWith('citizen-')) {
    const num = id.replace('citizen-', '');
    return `Citizen #${num}`;
  }
  
  // Otherwise, return as-is but capitalize first letter
  return id.charAt(0).toUpperCase() + id.slice(1);
}

export default function Leaderboard({ leaderboard, loading, error }) {
  if (loading) {
    return (
      <div style={{ 
        ...getCardStyle(false),
        padding: theme.spacing['2xl'],
        textAlign: 'center',
        color: theme.colors.textSecondary,
      }}>
        <div style={{ fontSize: '0.9375rem' }}>Loading leaderboard...</div>
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

  if (!leaderboard || leaderboard.length === 0) {
    return (
      <div style={{ 
        ...getCardStyle(false),
        padding: theme.spacing['2xl'],
        textAlign: 'center',
        color: theme.colors.textSecondary,
      }}>
        No leaderboard data available.
      </div>
    );
  }

  const getRankColor = (index) => {
    if (index === 0) return { bg: '#fff9e6', border: '#ffd700', icon: '🥇' };
    if (index === 1) return { bg: '#f5f5f5', border: '#c0c0c0', icon: '🥈' };
    if (index === 2) return { bg: '#faf5e6', border: '#cd7f32', icon: '🥉' };
    return { bg: theme.colors.gray50, border: theme.colors.borderLight, icon: null };
  };

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
        Top Contributors
      </div>
      <div style={{ padding: theme.spacing.lg }}>
        {leaderboard.map((entry, index) => {
          const rankStyle = getRankColor(index);
          return (
          <div
            key={entry.contributorId || index}
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
                padding: theme.spacing.md,
                marginBottom: index < leaderboard.length - 1 ? theme.spacing.md : 0,
                backgroundColor: rankStyle.bg,
                borderRadius: theme.borderRadius.md,
                border: `2px solid ${rankStyle.border}`,
                transition: `all ${theme.transitions.fast}`,
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateX(4px)';
                e.currentTarget.style.boxShadow = theme.shadows.md;
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateX(0)';
                e.currentTarget.style.boxShadow = 'none';
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: theme.spacing.md }}>
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '36px',
                  height: '36px',
                  borderRadius: theme.borderRadius.full,
                  backgroundColor: rankStyle.border,
                  color: theme.colors.textInverse,
                  fontWeight: '700',
                  fontSize: '0.875rem',
                }}>
                  {rankStyle.icon || `#${entry.rank || index + 1}`}
                </div>
              <span style={{
                  fontWeight: '600',
                  color: theme.colors.textPrimary,
              }}>
                {formatContributorName(entry.contributorId || entry.citizenId || 'Unknown')}
              </span>
            </div>
              <div style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'flex-end',
              }}>
                <span style={{
                  fontSize: '1.25rem',
                  fontWeight: '700',
                  color: theme.colors.textPrimary,
                }}>
                  {entry.points || 0}
                </span>
            <span style={{
                  fontSize: '0.75rem',
                  color: theme.colors.textSecondary,
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em',
            }}>
                  points
            </span>
          </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}


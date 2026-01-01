export default function Leaderboard({ leaderboard, loading, error }) {
  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: '#666' }}>
        Loading leaderboard...
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: '#c33' }}>
        {error}
      </div>
    );
  }

  if (!leaderboard || leaderboard.length === 0) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: '#666' }}>
        No leaderboard data available.
      </div>
    );
  }

  return (
    <div style={{
      backgroundColor: 'white',
      border: '1px solid #ddd',
      borderRadius: '8px',
      overflow: 'hidden',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    }}>
      <div style={{
        backgroundColor: '#f8f9fa',
        padding: '1rem',
        borderBottom: '1px solid #ddd',
        fontWeight: 'bold',
      }}>
        Top Contributors
      </div>
      <div style={{ padding: '1rem' }}>
        {leaderboard.map((entry, index) => (
          <div
            key={entry.contributorId || index}
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              padding: '0.75rem',
              marginBottom: '0.5rem',
              backgroundColor: index === 0 ? '#fff9e6' : index === 1 ? '#f5f5f5' : '#fafafa',
              borderRadius: '4px',
              border: index === 0 ? '2px solid #ffd700' : '1px solid #eee',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <span style={{
                fontSize: '1.2rem',
                fontWeight: 'bold',
                color: index === 0 ? '#ffd700' : index === 1 ? '#c0c0c0' : '#cd7f32',
                minWidth: '2rem',
                textAlign: 'center',
              }}>
                #{entry.rank || index + 1}
              </span>
              <span style={{ fontWeight: '500' }}>
                {entry.contributorId || 'Unknown'}
              </span>
            </div>
            <span style={{
              fontSize: '1.1rem',
              fontWeight: 'bold',
              color: '#2c3e50',
            }}>
              {entry.points || 0} pts
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}


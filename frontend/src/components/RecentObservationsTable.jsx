export default function RecentObservationsTable({ observations, loading, error }) {
  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: '#666' }}>
        Loading observations...
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

  if (!observations || observations.length === 0) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: '#666' }}>
        No observations found.
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
        Recent Observations
      </div>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ backgroundColor: '#f8f9fa' }}>
            <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #ddd' }}>
              Postcode
            </th>
            <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #ddd' }}>
              Measurements
            </th>
            <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #ddd' }}>
              Observation
            </th>
            <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #ddd' }}>
              Date
            </th>
            <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #ddd' }}>
              Contributor
            </th>
          </tr>
        </thead>
        <tbody>
          {observations.map((obs, index) => (
            <tr key={index} style={{ borderBottom: '1px solid #eee' }}>
              <td style={{ padding: '0.75rem' }}>{obs.postcode || '—'}</td>
              <td style={{ padding: '0.75rem' }}>
                {obs.measurements && Object.keys(obs.measurements).length > 0 ? (
                  <div style={{ fontSize: '0.85rem' }}>
                    {Object.entries(obs.measurements).map(([key, value]) => (
                      <div key={key}>
                        {key}: {typeof value === 'number' ? value.toFixed(2) : value}
                      </div>
                    ))}
                  </div>
                ) : '—'}
              </td>
              <td style={{ padding: '0.75rem', fontSize: '0.9rem' }}>
                {obs.observation || '—'}
              </td>
              <td style={{ padding: '0.75rem', fontSize: '0.85rem', color: '#666' }}>
                {obs.createdAt ? new Date(obs.createdAt).toLocaleString() : '—'}
              </td>
              <td style={{ padding: '0.75rem', fontSize: '0.85rem', color: '#666' }}>
                {obs.contributorId || '—'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}


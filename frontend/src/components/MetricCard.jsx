export default function MetricCard({ title, value, label }) {
  return (
    <div style={{
      backgroundColor: 'white',
      border: '1px solid #ddd',
      borderRadius: '8px',
      padding: '1.5rem',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    }}>
      <h3 style={{ 
        margin: '0 0 0.5rem 0', 
        fontSize: '0.9rem', 
        color: '#666',
        fontWeight: 'normal'
      }}>
        {title}
      </h3>
      <div style={{ 
        fontSize: '2rem', 
        fontWeight: 'bold', 
        color: '#2c3e50' 
      }}>
        {value !== null && value !== undefined ? value.toLocaleString() : '—'}
      </div>
      {label && (
        <p style={{ 
          margin: '0.5rem 0 0 0', 
          fontSize: '0.85rem', 
          color: '#999' 
        }}>
          {label}
        </p>
      )}
    </div>
  );
}


import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { API_CONFIG } from '../config/api';

export default function Hub() {
  const { user, logout } = useAuth();

  const authorities = [
    { id: 'a', name: API_CONFIG.authorities.a.name, path: '/dashboard/authority-a' },
    { id: 'b', name: API_CONFIG.authorities.b.name, path: '/dashboard/authority-b' },
    { id: 'c', name: API_CONFIG.authorities.c.name, path: '/dashboard/authority-c' },
    { id: 'd', name: API_CONFIG.authorities.d.name, path: '/dashboard/authority-d' },
  ];

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <header style={{
        backgroundColor: 'white',
        padding: '1rem 2rem',
        borderBottom: '1px solid #ddd',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}>
        <h1 style={{ margin: 0 }}>Water Quality Monitoring Hub</h1>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          {user && (
            <span style={{ color: '#666' }}>Welcome, {user.name || user.email}</span>
          )}
          <Link
            to="/change-password"
            style={{
              padding: '0.5rem 1rem',
              color: '#3498db',
              textDecoration: 'none',
            }}
          >
            Change Password
          </Link>
          <button
            onClick={logout}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#e74c3c',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            Logout
          </button>
        </div>
      </header>

      <main style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
        <h2 style={{ marginBottom: '2rem' }}>Select an Authority Dashboard</h2>
        
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
          gap: '1.5rem',
        }}>
          {authorities.map((authority) => (
            <Link
              key={authority.id}
              to={authority.path}
              style={{
                textDecoration: 'none',
                color: 'inherit',
              }}
            >
              <div style={{
                backgroundColor: 'white',
                padding: '2rem',
                borderRadius: '8px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                border: '2px solid #ddd',
                transition: 'all 0.2s',
                cursor: 'pointer',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.borderColor = '#3498db';
                e.currentTarget.style.transform = 'translateY(-2px)';
                e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.15)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.borderColor = '#ddd';
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = '0 2px 4px rgba(0,0,0,0.1)';
              }}
              >
                <h3 style={{ margin: '0 0 0.5rem 0', color: '#2c3e50' }}>
                  {authority.name}
                </h3>
                <p style={{ margin: 0, color: '#666', fontSize: '0.9rem' }}>
                  View dashboard →
                </p>
              </div>
            </Link>
          ))}
        </div>
      </main>
    </div>
  );
}


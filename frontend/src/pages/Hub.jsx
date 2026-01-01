import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { API_CONFIG } from '../config/api';
import { theme, getCardStyle, getButtonStyle } from '../styles/theme';

export default function Hub() {
  const { user, logout } = useAuth();

  const authorities = [
    { id: 'a', name: API_CONFIG.authorities.a.name, path: '/dashboard/authority-a', color: '#3b82f6' },
    { id: 'b', name: API_CONFIG.authorities.b.name, path: '/dashboard/authority-b', color: '#10b981' },
    { id: 'c', name: API_CONFIG.authorities.c.name, path: '/dashboard/authority-c', color: '#f59e0b' },
    { id: 'd', name: API_CONFIG.authorities.d.name, path: '/dashboard/authority-d', color: '#8b5cf6' },
    { id: 'e', name: API_CONFIG.authorities.e.name, path: '/dashboard/authority-e', color: '#ec4899' },
  ];

  return (
    <div style={{ minHeight: '100vh', backgroundColor: theme.colors.bgSecondary }}>
      <header style={{
        backgroundColor: theme.colors.bgPrimary,
        padding: `${theme.spacing.lg} ${theme.spacing.xl}`,
        borderBottom: `1px solid ${theme.colors.borderLight}`,
        boxShadow: theme.shadows.sm,
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        width: '100%',
        boxSizing: 'border-box',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: theme.spacing.md }}>
          <h1 style={{ 
            margin: 0, 
            fontSize: '1.5rem',
            fontWeight: '700',
            color: theme.colors.textPrimary,
            background: `linear-gradient(135deg, ${theme.colors.primary} 0%, ${theme.colors.secondary} 100%)`,
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}>
            Water Quality Monitoring Hub
          </h1>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: theme.spacing.md }}>
          {user && (
            <span style={{ 
              color: theme.colors.textSecondary,
              fontWeight: '500',
            }}>
              Welcome, {user.name || user.email}
            </span>
          )}
          <Link
            to="/change-password"
            style={{
              ...getButtonStyle('ghost', 'sm'),
              textDecoration: 'none',
            }}
          >
            Change Password
          </Link>
          <button
            onClick={logout}
            style={getButtonStyle('danger', 'sm')}
          >
            Logout
          </button>
        </div>
      </header>

      <main style={{ 
        padding: theme.spacing.xl, 
        width: '100%',
        margin: '0 auto',
        boxSizing: 'border-box',
      }}>
        <div style={{ marginBottom: theme.spacing['2xl'] }}>
          <h2 style={{ 
            marginBottom: theme.spacing.sm,
            fontSize: '2rem',
            fontWeight: '700',
            color: theme.colors.textPrimary,
          }}>
            Authority Dashboards
          </h2>
          <p style={{ 
            color: theme.colors.textSecondary,
            fontSize: '1.125rem',
          }}>
            Select an authority to view its dashboard and explore available APIs
          </p>
        </div>
        
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
          gap: theme.spacing.xl,
          width: '100%',
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
                ...getCardStyle(),
                borderTop: `4px solid ${authority.color}`,
                position: 'relative',
                overflow: 'hidden',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-4px)';
                e.currentTarget.style.boxShadow = theme.shadows.xl;
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = theme.shadows.md;
              }}
              >
                <div style={{
                  position: 'absolute',
                  top: 0,
                  right: 0,
                  width: '100px',
                  height: '100px',
                  background: `linear-gradient(135deg, ${authority.color}20 0%, transparent 100%)`,
                  borderRadius: '0 0 0 100%',
                }} />
                <div style={{ position: 'relative', zIndex: 1 }}>
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: theme.spacing.md,
                    marginBottom: theme.spacing.md,
                  }}>
                    <div style={{
                      width: '48px',
                      height: '48px',
                      borderRadius: theme.borderRadius.full,
                      backgroundColor: `${authority.color}20`,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: authority.color,
                      fontWeight: '700',
                      fontSize: '1.25rem',
                    }}>
                      {authority.id.toUpperCase()}
                    </div>
                    <h3 style={{ 
                      margin: 0, 
                      color: theme.colors.textPrimary,
                      fontSize: '1.25rem',
                      fontWeight: '600',
                    }}>
                  {authority.name}
                </h3>
                  </div>
                  <p style={{ 
                    margin: 0, 
                    color: theme.colors.textSecondary,
                    fontSize: '0.9375rem',
                    lineHeight: '1.5',
                  }}>
                    View dashboard, metrics, and API explorer →
                </p>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </main>
    </div>
  );
}


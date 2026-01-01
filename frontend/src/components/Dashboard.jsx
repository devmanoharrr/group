import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiRequest, getErrorMessage } from '../utils/api';
import { theme, getButtonStyle } from '../styles/theme';
import { AUTHORITY_ENDPOINTS } from '../config/endpoints';
import MetricCard from './MetricCard';
import RecentObservationsTable from './RecentObservationsTable';
import Leaderboard from './Leaderboard';
import ErrorBanner from './ErrorBanner';
import Loading from './Loading';
import ApiExplorer from './ApiExplorer';

export default function Dashboard({ authorityId, authorityName, baseUrl }) {
  const { logout } = useAuth();
  const [activeTab, setActiveTab] = useState('dashboard');
  const [count, setCount] = useState(null);
  const [observations, setObservations] = useState([]);
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errors, setErrors] = useState({
    count: null,
    observations: null,
    leaderboard: null,
  });
  
  const endpoints = AUTHORITY_ENDPOINTS[authorityId] || [];

  const fetchData = async () => {
    setLoading(true);
    setErrors({ count: null, observations: null, leaderboard: null });

    // Fetch count
    try {
      const { data } = await apiRequest(`${baseUrl}/api/observations/count`);
      setCount(data?.count || 0);
    } catch (error) {
      setErrors(prev => ({ ...prev, count: getErrorMessage(error) }));
    }

    // Fetch recent observations
    try {
      const { data } = await apiRequest(`${baseUrl}/api/observations/recent?limit=5`);
      setObservations(Array.isArray(data) ? data : []);
    } catch (error) {
      setErrors(prev => ({ ...prev, observations: getErrorMessage(error) }));
    }

    // Fetch leaderboard
    try {
      // Authority-C requires authority parameter, others don't
      const leaderboardUrl = authorityId === 'c' 
        ? `${baseUrl}/api/rewards/leaderboard?authority=NE&limit=3`
        : `${baseUrl}/api/rewards/leaderboard?limit=3`;
      const { data } = await apiRequest(leaderboardUrl);
      setLeaderboard(Array.isArray(data) ? data : []);
    } catch (error) {
      setErrors(prev => ({ ...prev, leaderboard: getErrorMessage(error) }));
    }

    setLoading(false);
  };

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [baseUrl]);

  if (loading && count === null && observations.length === 0 && leaderboard.length === 0) {
    return <Loading message="Loading dashboard..." />;
  }

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
        <div style={{ display: 'flex', alignItems: 'center', gap: theme.spacing.lg }}>
          <Link
            to="/hub"
            style={{
              ...getButtonStyle('ghost', 'sm'),
              textDecoration: 'none',
            }}
          >
            ← Back to Hub
          </Link>
          <h1 style={{ 
            margin: 0,
            fontSize: '1.5rem',
            fontWeight: '700',
            color: theme.colors.textPrimary,
          }}>
            {authorityName} Dashboard
          </h1>
        </div>
        <button
          onClick={logout}
          style={getButtonStyle('danger', 'sm')}
        >
          Logout
        </button>
      </header>

      {/* Tabs */}
      <div style={{
        backgroundColor: theme.colors.bgPrimary,
        borderBottom: `1px solid ${theme.colors.borderLight}`,
        padding: `0 ${theme.spacing.xl}`,
        width: '100%',
        boxSizing: 'border-box',
      }}>
        <div style={{
          display: 'flex',
          gap: theme.spacing.md,
          maxWidth: '100%',
          width: '100%',
        }}>
          <button
            onClick={() => setActiveTab('dashboard')}
            style={{
              padding: `${theme.spacing.md} ${theme.spacing.lg}`,
              border: 'none',
              borderBottom: `3px solid ${activeTab === 'dashboard' ? theme.colors.primary : 'transparent'}`,
              backgroundColor: 'transparent',
              color: activeTab === 'dashboard' ? theme.colors.primary : theme.colors.textSecondary,
              fontWeight: activeTab === 'dashboard' ? '600' : '500',
              cursor: 'pointer',
              transition: `all ${theme.transitions.fast}`,
            }}
          >
            Dashboard
          </button>
          <button
            onClick={() => setActiveTab('api')}
            style={{
              padding: `${theme.spacing.md} ${theme.spacing.lg}`,
              border: 'none',
              borderBottom: `3px solid ${activeTab === 'api' ? theme.colors.primary : 'transparent'}`,
              backgroundColor: 'transparent',
              color: activeTab === 'api' ? theme.colors.primary : theme.colors.textSecondary,
              fontWeight: activeTab === 'api' ? '600' : '500',
              cursor: 'pointer',
              transition: `all ${theme.transitions.fast}`,
            }}
          >
            API Explorer
          </button>
        </div>
      </div>

      <main style={{ 
        padding: theme.spacing.xl, 
        width: '100%',
        margin: '0 auto',
        boxSizing: 'border-box',
      }}>
        {activeTab === 'dashboard' ? (
          <>
        {/* Error banners */}
        {errors.count && (
          <ErrorBanner
            error={`Failed to load observation count: ${errors.count}`}
            onRetry={fetchData}
            onDismiss={() => setErrors(prev => ({ ...prev, count: null }))}
          />
        )}
        {errors.observations && (
          <ErrorBanner
            error={`Failed to load observations: ${errors.observations}`}
            onRetry={fetchData}
            onDismiss={() => setErrors(prev => ({ ...prev, observations: null }))}
          />
        )}
        {errors.leaderboard && (
          <ErrorBanner
            error={`Failed to load leaderboard: ${errors.leaderboard}`}
            onRetry={fetchData}
            onDismiss={() => setErrors(prev => ({ ...prev, leaderboard: null }))}
          />
        )}

        {/* Metric Card */}
            <div style={{ marginBottom: theme.spacing.xl }}>
          <MetricCard
            title="Total Observations"
            value={count}
            label="All time"
          />
        </div>

        {/* Two column layout for observations and leaderboard */}
        <div style={{
          display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))',
              gap: theme.spacing.xl,
              marginBottom: theme.spacing.xl,
        }}>
          <RecentObservationsTable
            observations={observations}
            loading={loading && observations.length === 0}
            error={errors.observations}
          />
          <Leaderboard
            leaderboard={leaderboard}
            loading={loading && leaderboard.length === 0}
            error={errors.leaderboard}
          />
        </div>
          </>
        ) : (
          <ApiExplorer 
            authorityId={authorityId}
            baseUrl={baseUrl}
            endpoints={endpoints}
          />
        )}
      </main>
    </div>
  );
}


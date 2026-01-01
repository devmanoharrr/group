import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiRequest, getErrorMessage } from '../utils/api';
import MetricCard from './MetricCard';
import RecentObservationsTable from './RecentObservationsTable';
import Leaderboard from './Leaderboard';
import ErrorBanner from './ErrorBanner';
import Loading from './Loading';

export default function Dashboard({ authorityId, authorityName, baseUrl }) {
  const { logout } = useAuth();
  const [count, setCount] = useState(null);
  const [observations, setObservations] = useState([]);
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errors, setErrors] = useState({
    count: null,
    observations: null,
    leaderboard: null,
  });

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
      const { data } = await apiRequest(`${baseUrl}/api/rewards/leaderboard?limit=3`);
      setLeaderboard(Array.isArray(data) ? data : []);
    } catch (error) {
      setErrors(prev => ({ ...prev, leaderboard: getErrorMessage(error) }));
    }

    setLoading(false);
  };

  useEffect(() => {
    fetchData();
  }, [baseUrl]);

  if (loading && count === null && observations.length === 0 && leaderboard.length === 0) {
    return <Loading message="Loading dashboard..." />;
  }

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
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <Link
            to="/hub"
            style={{
              padding: '0.5rem 1rem',
              color: '#3498db',
              textDecoration: 'none',
            }}
          >
            ← Back to Hub
          </Link>
          <h1 style={{ margin: 0 }}>{authorityName} Dashboard</h1>
        </div>
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
      </header>

      <main style={{ padding: '2rem', maxWidth: '1400px', margin: '0 auto' }}>
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
        <div style={{ marginBottom: '2rem' }}>
          <MetricCard
            title="Total Observations"
            value={count}
            label="All time"
          />
        </div>

        {/* Two column layout for observations and leaderboard */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '2fr 1fr',
          gap: '2rem',
          marginBottom: '2rem',
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
      </main>
    </div>
  );
}


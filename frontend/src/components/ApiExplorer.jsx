import { useState } from 'react';
import { apiRequest, getErrorMessage } from '../utils/api';
import { theme, getButtonStyle, getInputStyle, getCardStyle } from '../styles/theme';

export default function ApiExplorer({ authorityId, baseUrl, endpoints = [] }) {
  const [selectedEndpoint, setSelectedEndpoint] = useState(null);
  const [method, setMethod] = useState('GET');
  const [path, setPath] = useState('');
  const [queryParams, setQueryParams] = useState('');
  const [requestBody, setRequestBody] = useState('');
  const [response, setResponse] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleEndpointSelect = (endpoint) => {
    setSelectedEndpoint(endpoint);
    setMethod(endpoint.method || 'GET');
    setPath(endpoint.path || '');
    setQueryParams(endpoint.queryParams || '');
    setRequestBody(endpoint.body || '');
    setResponse(null);
    setError(null);
  };

  const handleExecute = async () => {
    setLoading(true);
    setError(null);
    setResponse(null);

    try {
      let url = `${baseUrl}${path}`;
      if (queryParams) {
        url += (path.includes('?') ? '&' : '?') + queryParams;
      }

      const options = {
        method,
      };

      if (method !== 'GET' && requestBody) {
        try {
          // Parse and re-stringify to validate JSON
          const parsed = JSON.parse(requestBody);
          options.body = JSON.stringify(parsed);
        } catch (e) {
          setError('Invalid JSON in request body: ' + e.message);
          setLoading(false);
          return;
        }
      }

      const result = await apiRequest(url, options);
      setResponse({
        status: result.status,
        data: result.data,
        headers: {},
      });
    } catch (err) {
      setError(getErrorMessage(err));
      setResponse({
        status: err.status || 500,
        error: true,
        message: err.message,
      });
    } finally {
      setLoading(false);
    }
  };

  const buttonStyle = getButtonStyle('primary');
  const inputStyle = getInputStyle();

  return (
    <div style={{ 
      padding: theme.spacing.xl,
      width: '100%',
      boxSizing: 'border-box',
    }}>
      <div style={{ marginBottom: theme.spacing.xl }}>
        <h2 style={{ 
          marginBottom: theme.spacing.lg,
          color: theme.colors.textPrimary,
          fontSize: '1.5rem',
          fontWeight: '700',
        }}>
          API Explorer - {authorityId.toUpperCase()}
        </h2>
        <p style={{ color: theme.colors.textSecondary, marginBottom: theme.spacing.lg }}>
          Test all available endpoints for this authority. Select a predefined endpoint or create a custom request.
        </p>
      </div>

      {/* Endpoint Selector */}
      {endpoints.length > 0 && (
        <div style={{ 
          marginBottom: theme.spacing.xl,
          padding: theme.spacing.lg,
          backgroundColor: theme.colors.bgSecondary,
          borderRadius: theme.borderRadius.lg,
        }}>
          <h3 style={{ 
            marginBottom: theme.spacing.md,
            color: theme.colors.textPrimary,
            fontSize: '1.125rem',
          }}>
            Quick Select Endpoints
          </h3>
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
            gap: theme.spacing.md,
          }}>
            {endpoints.map((endpoint, idx) => (
              <button
                key={idx}
                onClick={() => handleEndpointSelect(endpoint)}
                style={{
                  textAlign: 'left',
                  padding: theme.spacing.md,
                  backgroundColor: selectedEndpoint === endpoint ? theme.colors.primary : 'transparent',
                  color: selectedEndpoint === endpoint ? theme.colors.textInverse : theme.colors.primary,
                  border: `2px solid ${theme.colors.primary}`,
                  borderRadius: theme.borderRadius.md,
                  cursor: 'pointer',
                  transition: `all ${theme.transitions.fast}`,
                  fontWeight: '500',
                }}
                onMouseEnter={(e) => {
                  if (selectedEndpoint !== endpoint) {
                    e.currentTarget.style.backgroundColor = `${theme.colors.primary}10`;
                  }
                }}
                onMouseLeave={(e) => {
                  if (selectedEndpoint !== endpoint) {
                    e.currentTarget.style.backgroundColor = 'transparent';
                  }
                }}
              >
                <div style={{ fontWeight: '600' }}>{endpoint.method || 'GET'}</div>
                <div style={{ fontSize: '0.875rem', opacity: 0.8 }}>{endpoint.path}</div>
                {endpoint.description && (
                  <div style={{ fontSize: '0.75rem', marginTop: theme.spacing.xs, opacity: 0.7 }}>
                    {endpoint.description}
                  </div>
                )}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Request Builder */}
      <div style={{
        ...getCardStyle(),
        marginBottom: theme.spacing.xl,
      }}>
        <h3 style={{ 
          marginBottom: theme.spacing.lg,
          color: theme.colors.textPrimary,
        }}>
          Request Builder
        </h3>

        <div style={{ display: 'grid', gap: theme.spacing.lg }}>
          {/* Method and Path */}
          <div style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: theme.spacing.md }}>
            <select
              value={method}
              onChange={(e) => setMethod(e.target.value)}
              style={inputStyle}
            >
              <option value="GET">GET</option>
              <option value="POST">POST</option>
              <option value="PUT">PUT</option>
              <option value="DELETE">DELETE</option>
              <option value="PATCH">PATCH</option>
            </select>
            <input
              type="text"
              value={path}
              onChange={(e) => setPath(e.target.value)}
              placeholder="/api/observations/count"
              style={inputStyle}
            />
          </div>

          {/* Query Parameters */}
          <div>
            <label style={{ 
              display: 'block',
              marginBottom: theme.spacing.sm,
              color: theme.colors.textSecondary,
              fontWeight: '500',
            }}>
              Query Parameters (e.g., limit=5&authority=test)
            </label>
            <input
              type="text"
              value={queryParams}
              onChange={(e) => setQueryParams(e.target.value)}
              placeholder="limit=5"
              style={inputStyle}
            />
          </div>

          {/* Request Body */}
          {method !== 'GET' && (
            <div>
              <label style={{ 
                display: 'block',
                marginBottom: theme.spacing.sm,
                color: theme.colors.textSecondary,
                fontWeight: '500',
              }}>
                Request Body (JSON)
              </label>
              <textarea
                value={requestBody}
                onChange={(e) => setRequestBody(e.target.value)}
                placeholder='{"key": "value"}'
                style={{
                  ...inputStyle,
                  minHeight: '150px',
                  fontFamily: 'monospace',
                  resize: 'vertical',
                }}
              />
            </div>
          )}

          {/* Execute Button */}
          <button
            onClick={handleExecute}
            disabled={loading || !path}
            style={{
              ...buttonStyle,
              width: '100%',
            }}
          >
            {loading ? 'Executing...' : 'Execute Request'}
          </button>
        </div>
      </div>

      {/* Response Display */}
      {(response || error) && (
        <div style={{
          ...getCardStyle(false),
        }}>
          <h3 style={{ 
            marginBottom: theme.spacing.lg,
            color: theme.colors.textPrimary,
          }}>
            Response
          </h3>

          {error && (
            <div style={{
              padding: theme.spacing.md,
              backgroundColor: '#fee2e2',
              border: `1px solid ${theme.colors.danger}`,
              borderRadius: theme.borderRadius.md,
              marginBottom: theme.spacing.md,
              color: theme.colors.dangerDark,
            }}>
              <strong>Error:</strong> {error}
            </div>
          )}

          {response && (
            <div>
              <div style={{
                marginBottom: theme.spacing.md,
                padding: theme.spacing.sm,
                backgroundColor: response.error ? '#fee2e2' : '#d1fae5',
                borderRadius: theme.borderRadius.sm,
                display: 'inline-block',
              }}>
                <strong>Status:</strong> {response.status} {response.error ? '(Error)' : '(Success)'}
              </div>

              <pre style={{
                backgroundColor: theme.colors.gray900,
                color: theme.colors.textInverse,
                padding: theme.spacing.lg,
                borderRadius: theme.borderRadius.md,
                overflow: 'auto',
                fontSize: '0.875rem',
                lineHeight: '1.5',
                maxHeight: '500px',
              }}>
                {JSON.stringify(response.data || response, null, 2)}
              </pre>
            </div>
          )}
        </div>
      )}
    </div>
  );
}


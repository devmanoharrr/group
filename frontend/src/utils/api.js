/**
 * API Client Utility
 * Centralized fetch wrapper with timeout, error handling, and token support
 */

const DEFAULT_TIMEOUT = 10000; // 10 seconds

/**
 * Fetch wrapper with timeout and error handling
 */
export async function apiRequest(url, options = {}) {
  const { timeout = DEFAULT_TIMEOUT, ...fetchOptions } = options;

  // Get token from localStorage if available
  const token = localStorage.getItem('authToken');
  if (token) {
    fetchOptions.headers = {
      ...fetchOptions.headers,
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    };
  } else {
    fetchOptions.headers = {
      ...fetchOptions.headers,
      'Content-Type': 'application/json',
    };
  }

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  try {
    const response = await fetch(url, {
      ...fetchOptions,
      signal: controller.signal,
    });

    clearTimeout(timeoutId);

    // Handle non-JSON responses
    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return { ok: true, data: null, status: response.status };
    }

    const data = await response.json();

    if (!response.ok) {
      // Handle different error status codes
      const error = new Error(data.message || `HTTP ${response.status}: ${response.statusText}`);
      error.status = response.status;
      error.data = data;
      throw error;
    }

    return { ok: true, data, status: response.status };
  } catch (error) {
    clearTimeout(timeoutId);

    if (error.name === 'AbortError') {
      const timeoutError = new Error('Request timeout - service may be unavailable');
      timeoutError.status = 408;
      timeoutError.isTimeout = true;
      throw timeoutError;
    }

    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      const networkError = new Error('Network error - service may be down');
      networkError.status = 0;
      networkError.isNetworkError = true;
      throw networkError;
    }

    throw error;
  }
}

/**
 * Get friendly error message based on status code
 */
export function getErrorMessage(error) {
  if (typeof error === 'string') {
    return error;
  }
  if (error.isTimeout) {
    return 'Request timed out. The service may be slow or unavailable.';
  }

  if (error.isNetworkError) {
    return 'Network error. Please check if the service is running.';
  }

  switch (error.status) {
    case 400:
      return error.data?.message || 'Invalid request. Please check your input.';
    case 401:
      return 'Authentication required. Please log in.';
    case 403:
      return 'Access denied. You do not have permission.';
    case 404:
      return 'Resource not found.';
    case 500:
      return 'Server error. Please try again later.';
    case 503:
      return 'Service unavailable. The service may be down or overloaded.';
    default:
      return error.message || 'An unexpected error occurred.';
  }
}


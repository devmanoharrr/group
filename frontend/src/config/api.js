/**
 * API Configuration
 * Reads environment variables for service URLs
 */

export const API_CONFIG = {
  auth: {
    baseUrl: import.meta.env.VITE_AUTH_SERVICE_URL || 'http://localhost:8083',
  },
  authorities: {
    a: {
      baseUrl: import.meta.env.VITE_AUTHORITY_A_URL || 'http://localhost:8080',
      name: 'Authority A',
    },
    b: {
      baseUrl: import.meta.env.VITE_AUTHORITY_B_URL || 'http://localhost:8090',
      name: 'Authority B',
    },
    c: {
      baseUrl: import.meta.env.VITE_AUTHORITY_C_URL || 'http://localhost:8100',
      name: 'Authority C',
    },
    d: {
      baseUrl: import.meta.env.VITE_AUTHORITY_D_URL || 'http://localhost:8110',
      name: 'Authority D',
    },
    e: {
      baseUrl: import.meta.env.VITE_AUTHORITY_E_URL || 'http://localhost:8120',
      name: 'Authority E',
    },
  },
};


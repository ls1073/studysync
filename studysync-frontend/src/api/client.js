import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('studysync_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    // 401 is the correct code for "not authenticated" and is what a properly
    // configured backend returns for a missing/stale/invalid token. 403 is
    // also treated the same way here as a defensive fallback, since some
    // Spring Security configurations return 403 instead of 401 for this
    // exact case (an unauthenticated request hitting a protected endpoint) --
    // either way, the session is invalid and the user should be logged out
    // and sent back to login rather than seeing silent, confusing failures.
    if (error.response?.status === 401 || error.response?.status === 403) {
      const hadToken = !!localStorage.getItem('studysync_token');
      localStorage.removeItem('studysync_token');
      localStorage.removeItem('studysync_user');
      if (hadToken && window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default client;

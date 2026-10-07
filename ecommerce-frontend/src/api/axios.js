import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error.response?.status;
    const isAuthCall = error.config?.url?.includes('/auth/');
    // Expired / invalid token -> force logout
    if (status === 401 && !isAuthCall) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const getErrorMessage = (error) => {
  const data = error.response?.data;
  if (data?.errors && typeof data.errors === 'object') {
    return Object.values(data.errors).join(', ');
  }
  return data?.message || data?.error || (typeof data === 'string' ? data : null) || error.message || 'Something went wrong';
};

export default api;

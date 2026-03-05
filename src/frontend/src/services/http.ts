import axios from 'axios';

const http = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  timeout: 30_000,
});

const PUBLIC_ROUTES = ['/', '/login', '/signup', '/privacy', '/terms', '/verify-email', '/forgot-password', '/reset-password'];

function isPublicRoute(pathname: string): boolean {
  return PUBLIC_ROUTES.some(route => {
    if (route === '/') return pathname === '/';
    return pathname === route || pathname.startsWith(route + '/');
  });
}

// Unwrap Google JSON Style Guide response envelope and redirect on 401
http.interceptors.response.use(
  (response) => {
    if (response.data && typeof response.data === 'object' && 'data' in response.data) {
      response.data = response.data.data;
    }
    return response;
  },
  (error) => {
    const isAuthCheck = error.config?.url?.includes('/auth/me');
    if (error.response?.status === 401 && !isPublicRoute(window.location.pathname) && !isAuthCheck) {
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default http;

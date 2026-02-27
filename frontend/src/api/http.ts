import axios, { AxiosError } from 'axios';
import { getAuthToken, useAuthStore } from '../store/auth';

type ApiResp<T = unknown> = {
  code: number;
  message: string;
  data: T;
};

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const http = axios.create({
  baseURL,
  timeout: 15000
});

function kickToLogin() {
  useAuthStore.getState().clearAuth();
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}

http.interceptors.request.use((config) => {
  const base = String(config.baseURL || '');
  const url = String(config.url || '');
  // In production baseURL is "/api". Prevent duplicated "/api/api/*".
  if (base.endsWith('/api') && url.startsWith('/api/')) {
    config.url = url.replace(/^\/api/, '');
  }
  const token = getAuthToken();
  if (token) {
    config.headers.Authorization = token;
  }
  return config;
});

http.interceptors.response.use(
  (resp) => {
    const body = resp.data as ApiResp;
    if (body && typeof body.code === 'number' && body.code === 401) {
      kickToLogin();
      return Promise.reject(new Error(body.message || 'Unauthorized'));
    }
    return resp;
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      kickToLogin();
    }
    return Promise.reject(error);
  }
);

export async function unwrap<T>(promise: Promise<{ data: ApiResp<T> }>): Promise<T> {
  const res = await promise;
  const body = res.data;
  if (typeof body.code === 'number' && body.code !== 0) {
    throw new Error(body.message || 'Request failed');
  }
  return body.data;
}

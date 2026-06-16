import axios from 'axios';
import type { ApiResponse, AuthResponse, Bank, UserProfile } from '../types';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const res = await axios.post<ApiResponse<AuthResponse>>(
            `${API_BASE}/api/auth/refresh`,
            { refreshToken }
          );
          const data = res.data.data;
          localStorage.setItem('accessToken', data.accessToken);
          localStorage.setItem('refreshToken', data.refreshToken);
          original.headers.Authorization = `Bearer ${data.accessToken}`;
          return api(original);
        } catch {
          localStorage.clear();
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: (username: string, password: string) =>
    api.post<ApiResponse<AuthResponse>>('/api/auth/login', { username, password }),

  register: (payload: {
    username: string;
    email: string;
    password: string;
    fullName: string;
    bankId: number;
  }) => api.post<ApiResponse<AuthResponse>>('/api/auth/register', payload),

  profile: () => api.get<ApiResponse<UserProfile>>('/api/auth/profile'),

  logout: (refreshToken: string) =>
    api.post<ApiResponse<void>>('/api/auth/logout', { refreshToken }),
};

export const bankApi = {
  list: () => api.get<ApiResponse<Bank[]>>('/api/banks'),
};

export default api;

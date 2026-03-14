import api from './api';
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '@/types';

const AUTH_PREFIX = '/auth';

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(`${AUTH_PREFIX}/login`, credentials);
    const data = response.data;
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data;
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(`${AUTH_PREFIX}/register`, data);
    const result = response.data;
    localStorage.setItem('accessToken', result.accessToken);
    localStorage.setItem('refreshToken', result.refreshToken);
    return result;
  },

  async refreshToken(refreshToken: string): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(`${AUTH_PREFIX}/refresh`, { refreshToken });
    const data = response.data;
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data;
  },

  async getProfile(): Promise<User> {
    const response = await api.get<User>(`${AUTH_PREFIX}/me`);
    return response.data;
  },

  async updateProfile(data: Partial<User>): Promise<User> {
    const response = await api.put<User>(`${AUTH_PREFIX}/profile`, data);
    return response.data;
  },

  async logout(): Promise<void> {
    try {
      await api.post(`${AUTH_PREFIX}/logout`);
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    }
  },

  async forgotPassword(email: string): Promise<void> {
    await api.post(`${AUTH_PREFIX}/forgot-password`, { email });
  },

  async resetPassword(token: string, password: string): Promise<void> {
    await api.post(`${AUTH_PREFIX}/reset-password`, { token, password });
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem('accessToken');
  },

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  },
};

export default authService;

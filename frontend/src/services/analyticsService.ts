import api from './api';
import type {
  DashboardStats,
  TimeSeriesDataPoint,
  CategoryBreakdown,
  LeaderboardEntry,
  AnalyticsDashboard,
} from '@/types';

const ANALYTICS_PREFIX = '/analytics';

export const analyticsService = {
  async getDashboard(): Promise<AnalyticsDashboard> {
    const response = await api.get<AnalyticsDashboard>(`${ANALYTICS_PREFIX}/dashboard`);
    return response.data;
  },

  async getStats(): Promise<DashboardStats> {
    const response = await api.get<DashboardStats>(`${ANALYTICS_PREFIX}/stats`);
    return response.data;
  },

  async getFoodSavedTimeSeries(params?: {
    period?: 'week' | 'month' | 'year';
    startDate?: string;
    endDate?: string;
  }): Promise<TimeSeriesDataPoint[]> {
    const response = await api.get<TimeSeriesDataPoint[]>(`${ANALYTICS_PREFIX}/food-saved`, {
      params,
    });
    return response.data;
  },

  async getCategoryBreakdown(): Promise<CategoryBreakdown[]> {
    const response = await api.get<CategoryBreakdown[]>(`${ANALYTICS_PREFIX}/categories`);
    return response.data;
  },

  async getLeaderboard(params?: {
    period?: 'week' | 'month' | 'year' | 'all';
    limit?: number;
  }): Promise<LeaderboardEntry[]> {
    const response = await api.get<LeaderboardEntry[]>(`${ANALYTICS_PREFIX}/leaderboard`, {
      params,
    });
    return response.data;
  },

  async getMyImpact(): Promise<{
    foodSaved: number;
    mealsProvided: number;
    co2Saved: number;
    moneySaved: number;
    streak: number;
  }> {
    const response = await api.get(`${ANALYTICS_PREFIX}/my-impact`);
    return response.data;
  },

  async getSystemHealth(): Promise<{
    uptime: number;
    activeConnections: number;
    requestsPerMinute: number;
    errorRate: number;
    dbStatus: string;
    cacheStatus: string;
  }> {
    const response = await api.get(`${ANALYTICS_PREFIX}/system-health`);
    return response.data;
  },
};

export default analyticsService;

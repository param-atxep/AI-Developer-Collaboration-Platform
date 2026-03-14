import api from './api';
import type { Pickup, SchedulePickupRequest, PaginatedResponse, PickupStatus } from '@/types';

const PICKUP_PREFIX = '/pickups';

export const pickupService = {
  async schedulePickup(data: SchedulePickupRequest): Promise<Pickup> {
    const response = await api.post<Pickup>(PICKUP_PREFIX, data);
    return response.data;
  },

  async getPickups(params?: {
    status?: PickupStatus;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<Pickup>> {
    const response = await api.get<PaginatedResponse<Pickup>>(PICKUP_PREFIX, { params });
    return response.data;
  },

  async getPickupById(id: string): Promise<Pickup> {
    const response = await api.get<Pickup>(`${PICKUP_PREFIX}/${id}`);
    return response.data;
  },

  async updateStatus(id: string, status: PickupStatus, notes?: string): Promise<Pickup> {
    const response = await api.patch<Pickup>(`${PICKUP_PREFIX}/${id}/status`, { status, notes });
    return response.data;
  },

  async cancelPickup(id: string, reason: string): Promise<Pickup> {
    const response = await api.post<Pickup>(`${PICKUP_PREFIX}/${id}/cancel`, { reason });
    return response.data;
  },

  async ratePickup(id: string, rating: number, feedback?: string): Promise<Pickup> {
    const response = await api.post<Pickup>(`${PICKUP_PREFIX}/${id}/rate`, { rating, feedback });
    return response.data;
  },

  async getHistory(params?: {
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<Pickup>> {
    const response = await api.get<PaginatedResponse<Pickup>>(`${PICKUP_PREFIX}/history`, {
      params,
    });
    return response.data;
  },

  async getUpcoming(): Promise<Pickup[]> {
    const response = await api.get<Pickup[]>(`${PICKUP_PREFIX}/upcoming`);
    return response.data;
  },

  async verifyQrCode(pickupId: string, qrCode: string): Promise<Pickup> {
    const response = await api.post<Pickup>(`${PICKUP_PREFIX}/${pickupId}/verify`, { qrCode });
    return response.data;
  },
};

export default pickupService;

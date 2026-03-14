import api from './api';
import type { Notification, PaginatedResponse } from '@/types';

const NOTIFICATION_PREFIX = '/notifications';

export const notificationService = {
  async getNotifications(params?: {
    page?: number;
    size?: number;
    unreadOnly?: boolean;
  }): Promise<PaginatedResponse<Notification>> {
    const response = await api.get<PaginatedResponse<Notification>>(NOTIFICATION_PREFIX, {
      params,
    });
    return response.data;
  },

  async getUnreadCount(): Promise<number> {
    const response = await api.get<{ count: number }>(`${NOTIFICATION_PREFIX}/unread-count`);
    return response.data.count;
  },

  async markAsRead(id: string): Promise<void> {
    await api.patch(`${NOTIFICATION_PREFIX}/${id}/read`);
  },

  async markAllAsRead(): Promise<void> {
    await api.patch(`${NOTIFICATION_PREFIX}/read-all`);
  },

  async deleteNotification(id: string): Promise<void> {
    await api.delete(`${NOTIFICATION_PREFIX}/${id}`);
  },
};

export default notificationService;

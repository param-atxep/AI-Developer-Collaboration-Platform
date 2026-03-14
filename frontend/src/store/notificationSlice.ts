import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import type { Notification, NotificationState } from '@/types';
import notificationService from '@/services/notificationService';

const initialState: NotificationState = {
  notifications: [],
  unreadCount: 0,
  loading: false,
};

export const fetchNotifications = createAsyncThunk(
  'notifications/fetch',
  async (params: { page?: number; size?: number; unreadOnly?: boolean } | undefined) => {
    return await notificationService.getNotifications(params);
  },
);

export const fetchUnreadCount = createAsyncThunk('notifications/unreadCount', async () => {
  return await notificationService.getUnreadCount();
});

export const markAsRead = createAsyncThunk('notifications/markAsRead', async (id: string) => {
  await notificationService.markAsRead(id);
  return id;
});

export const markAllAsRead = createAsyncThunk('notifications/markAllAsRead', async () => {
  await notificationService.markAllAsRead();
});

const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    addNotification(state, action: PayloadAction<Notification>) {
      state.notifications.unshift(action.payload);
      if (!action.payload.read) {
        state.unreadCount += 1;
      }
    },
    clearNotifications(state) {
      state.notifications = [];
      state.unreadCount = 0;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchNotifications.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchNotifications.fulfilled, (state, action) => {
        state.loading = false;
        state.notifications = action.payload.content;
      })
      .addCase(fetchNotifications.rejected, (state) => {
        state.loading = false;
      });

    builder.addCase(fetchUnreadCount.fulfilled, (state, action) => {
      state.unreadCount = action.payload;
    });

    builder.addCase(markAsRead.fulfilled, (state, action) => {
      const notification = state.notifications.find((n) => n.id === action.payload);
      if (notification && !notification.read) {
        notification.read = true;
        state.unreadCount = Math.max(0, state.unreadCount - 1);
      }
    });

    builder.addCase(markAllAsRead.fulfilled, (state) => {
      state.notifications.forEach((n) => {
        n.read = true;
      });
      state.unreadCount = 0;
    });
  },
});

export const { addNotification, clearNotifications } = notificationSlice.actions;
export default notificationSlice.reducer;

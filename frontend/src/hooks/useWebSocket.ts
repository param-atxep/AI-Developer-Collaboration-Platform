import { useEffect, useRef, useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/store';
import { addNotification } from '@/store/notificationSlice';
import { updateListingInStore } from '@/store/foodSlice';
import wsService from '@/services/websocketService';
import type { Notification, FoodListing } from '@/types';
import toast from 'react-hot-toast';

export function useWebSocket() {
  const dispatch = useAppDispatch();
  const { user, token } = useAppSelector((state) => state.auth);
  const connectedRef = useRef(false);
  const unsubscribersRef = useRef<Array<() => void>>([]);

  const connect = useCallback(async () => {
    if (!token || !user || connectedRef.current) return;

    try {
      await wsService.connect(token);
      connectedRef.current = true;

      // Subscribe to user notifications
      const unsubNotif = wsService.subscribeToUserNotifications(
        user.id,
        (data: unknown) => {
          const notification = data as Notification;
          dispatch(addNotification(notification));
          toast(notification.message, {
            icon: getNotificationIcon(notification.type),
            duration: 5000,
          });
        },
      );
      unsubscribersRef.current.push(unsubNotif);

      // Subscribe to food updates
      const unsubFood = wsService.subscribeToFoodUpdates((data: unknown) => {
        const listing = data as FoodListing;
        dispatch(updateListingInStore(listing));
      });
      unsubscribersRef.current.push(unsubFood);
    } catch (error) {
      console.error('WebSocket connection failed:', error);
      connectedRef.current = false;
    }
  }, [token, user, dispatch]);

  const disconnect = useCallback(() => {
    unsubscribersRef.current.forEach((unsub) => unsub());
    unsubscribersRef.current = [];
    wsService.disconnect();
    connectedRef.current = false;
  }, []);

  useEffect(() => {
    if (token && user) {
      connect();
    }

    return () => {
      disconnect();
    };
  }, [token, user, connect, disconnect]);

  return {
    isConnected: wsService.isConnected,
    connect,
    disconnect,
  };
}

function getNotificationIcon(type: string): string {
  switch (type) {
    case 'NEW_FOOD_NEARBY':
      return '🍽️';
    case 'FOOD_CLAIMED':
      return '✅';
    case 'PICKUP_SCHEDULED':
      return '📅';
    case 'PICKUP_REMINDER':
      return '⏰';
    case 'PICKUP_COMPLETED':
      return '🎉';
    case 'FOOD_EXPIRING':
      return '⚠️';
    case 'BADGE_EARNED':
      return '🏆';
    default:
      return '📢';
  }
}

export default useWebSocket;

import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Bell,
  BellOff,
  Check,
  CheckCheck,
  MapPin,
  Truck,
  UtensilsCrossed,
  Clock,
  Award,
  AlertTriangle,
  Settings,
} from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchNotifications, markAsRead, markAllAsRead } from '@/store/notificationSlice';
import type { NotificationType } from '@/types';

interface NotificationPanelProps {
  onClose: () => void;
}

const typeConfig: Record<NotificationType, { icon: React.ElementType; color: string }> = {
  NEW_FOOD_NEARBY: { icon: UtensilsCrossed, color: 'text-primary-500 bg-primary-50 dark:bg-primary-900/20' },
  FOOD_CLAIMED: { icon: Check, color: 'text-blue-500 bg-blue-50 dark:bg-blue-900/20' },
  PICKUP_SCHEDULED: { icon: Clock, color: 'text-purple-500 bg-purple-50 dark:bg-purple-900/20' },
  PICKUP_REMINDER: { icon: Bell, color: 'text-amber-500 bg-amber-50 dark:bg-amber-900/20' },
  PICKUP_COMPLETED: { icon: Truck, color: 'text-green-500 bg-green-50 dark:bg-green-900/20' },
  FOOD_EXPIRING: { icon: AlertTriangle, color: 'text-red-500 bg-red-50 dark:bg-red-900/20' },
  BADGE_EARNED: { icon: Award, color: 'text-amber-500 bg-amber-50 dark:bg-amber-900/20' },
  SYSTEM: { icon: Settings, color: 'text-gray-500 bg-gray-50 dark:bg-gray-800' },
};

export default function NotificationPanel({ onClose }: NotificationPanelProps) {
  const dispatch = useAppDispatch();
  const { notifications, unreadCount, loading } = useAppSelector((state) => state.notifications);

  useEffect(() => {
    dispatch(fetchNotifications({ size: 20 }));
  }, [dispatch]);

  const handleMarkRead = (id: string) => {
    dispatch(markAsRead(id));
  };

  const handleMarkAllRead = () => {
    dispatch(markAllAsRead());
  };

  return (
    <div className="absolute right-0 mt-2 w-96 max-h-[32rem] rounded-xl bg-white shadow-xl ring-1 ring-black/5 dark:bg-gray-800 dark:ring-gray-700 animate-slide-down flex flex-col overflow-hidden z-50">
      {/* Header */}
      <div className="px-4 py-3 border-b border-gray-100 dark:border-gray-700 flex items-center justify-between flex-shrink-0">
        <div className="flex items-center gap-2">
          <h3 className="text-sm font-semibold text-gray-900 dark:text-white">
            Notifications
          </h3>
          {unreadCount > 0 && (
            <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-primary-500 text-[10px] font-bold text-white px-1.5">
              {unreadCount}
            </span>
          )}
        </div>
        {unreadCount > 0 && (
          <button
            onClick={handleMarkAllRead}
            className="text-xs text-primary-600 hover:text-primary-500 font-medium flex items-center gap-1"
          >
            <CheckCheck className="h-3.5 w-3.5" />
            Mark all read
          </button>
        )}
      </div>

      {/* Notifications List */}
      <div className="overflow-y-auto flex-1">
        {loading && notifications.length === 0 ? (
          <div className="p-6 text-center">
            <div className="h-6 w-6 animate-spin rounded-full border-2 border-primary-200 border-t-primary-500 mx-auto" />
          </div>
        ) : notifications.length === 0 ? (
          <div className="p-8 text-center">
            <BellOff className="h-10 w-10 text-gray-300 dark:text-gray-600 mx-auto mb-2" />
            <p className="text-sm text-gray-500 dark:text-gray-400">
              No notifications yet
            </p>
          </div>
        ) : (
          <div className="divide-y divide-gray-100 dark:divide-gray-700">
            {notifications.map((notification) => {
              const config = typeConfig[notification.type];
              const Icon = config.icon;

              return (
                <div
                  key={notification.id}
                  className={`px-4 py-3 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer ${
                    !notification.read ? 'bg-primary-50/30 dark:bg-primary-900/10' : ''
                  }`}
                  onClick={() => {
                    if (!notification.read) {
                      handleMarkRead(notification.id);
                    }
                    if (notification.actionUrl) {
                      onClose();
                    }
                  }}
                >
                  <div className="flex gap-3">
                    <div className={`h-9 w-9 rounded-lg flex items-center justify-center flex-shrink-0 ${config.color}`}>
                      <Icon className="h-4 w-4" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between gap-2">
                        <p className={`text-sm leading-snug ${
                          notification.read
                            ? 'text-gray-600 dark:text-gray-400'
                            : 'text-gray-900 dark:text-white font-medium'
                        }`}>
                          {notification.title}
                        </p>
                        {!notification.read && (
                          <div className="h-2 w-2 rounded-full bg-primary-500 flex-shrink-0 mt-1.5" />
                        )}
                      </div>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5 line-clamp-2">
                        {notification.message}
                      </p>
                      <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">
                        {formatDistanceToNow(new Date(notification.createdAt), { addSuffix: true })}
                      </p>
                    </div>
                  </div>
                  {notification.actionUrl && (
                    <Link
                      to={notification.actionUrl}
                      onClick={(e) => {
                        e.stopPropagation();
                        onClose();
                      }}
                      className="flex items-center gap-1 text-xs text-primary-600 hover:text-primary-500 mt-2 ml-12"
                    >
                      <MapPin className="h-3 w-3" />
                      View details
                    </Link>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="px-4 py-2.5 border-t border-gray-100 dark:border-gray-700 flex-shrink-0">
        <Link
          to="/notifications"
          onClick={onClose}
          className="block text-center text-xs font-medium text-primary-600 hover:text-primary-500"
        >
          View all notifications
        </Link>
      </div>
    </div>
  );
}

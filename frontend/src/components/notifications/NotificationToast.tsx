import { useEffect } from 'react';
import toast from 'react-hot-toast';
import {
  UtensilsCrossed,
  Check,
  Clock,
  Bell,
  Truck,
  AlertTriangle,
  Award,
  X,
} from 'lucide-react';
import type { Notification, NotificationType } from '@/types';

interface NotificationToastProps {
  notification: Notification;
  onDismiss: () => void;
}

const iconMap: Record<NotificationType, React.ElementType> = {
  NEW_FOOD_NEARBY: UtensilsCrossed,
  FOOD_CLAIMED: Check,
  PICKUP_SCHEDULED: Clock,
  PICKUP_REMINDER: Bell,
  PICKUP_COMPLETED: Truck,
  FOOD_EXPIRING: AlertTriangle,
  BADGE_EARNED: Award,
  SYSTEM: Bell,
};

const colorMap: Record<NotificationType, string> = {
  NEW_FOOD_NEARBY: 'bg-primary-500',
  FOOD_CLAIMED: 'bg-blue-500',
  PICKUP_SCHEDULED: 'bg-purple-500',
  PICKUP_REMINDER: 'bg-amber-500',
  PICKUP_COMPLETED: 'bg-green-500',
  FOOD_EXPIRING: 'bg-red-500',
  BADGE_EARNED: 'bg-amber-500',
  SYSTEM: 'bg-gray-500',
};

export default function NotificationToast({ notification, onDismiss }: NotificationToastProps) {
  const Icon = iconMap[notification.type];
  const bgColor = colorMap[notification.type];

  return (
    <div className="flex items-start gap-3 max-w-sm">
      <div className={`h-8 w-8 rounded-lg ${bgColor} flex items-center justify-center flex-shrink-0`}>
        <Icon className="h-4 w-4 text-white" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-semibold text-gray-900 dark:text-white">
          {notification.title}
        </p>
        <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5 line-clamp-2">
          {notification.message}
        </p>
      </div>
      <button
        onClick={onDismiss}
        className="p-1 rounded text-gray-400 hover:text-gray-600 flex-shrink-0"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  );
}

// Helper to show notification toasts
export function showNotificationToast(notification: Notification) {
  toast.custom(
    (t) => (
      <div
        className={`${
          t.visible ? 'animate-slide-down' : 'opacity-0'
        } bg-white dark:bg-gray-800 rounded-xl shadow-lg ring-1 ring-black/5 dark:ring-gray-700 p-4 max-w-sm transition-all`}
      >
        <NotificationToast
          notification={notification}
          onDismiss={() => toast.dismiss(t.id)}
        />
      </div>
    ),
    {
      duration: 5000,
      position: 'top-right',
    },
  );
}

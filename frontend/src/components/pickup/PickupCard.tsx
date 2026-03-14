import {
  Clock,
  MapPin,
  Truck,
  CheckCircle,
  XCircle,
  QrCode,
  Star,
  ChevronRight,
  Phone,
} from 'lucide-react';
import { format, formatDistanceToNow } from 'date-fns';
import type { Pickup, PickupStatus } from '@/types';

interface PickupCardProps {
  pickup: Pickup;
  onUpdateStatus?: (id: string, status: PickupStatus) => void;
  onCancel?: (id: string) => void;
  onRate?: (id: string) => void;
  loading?: boolean;
}

const statusConfig: Record<
  PickupStatus,
  { color: string; icon: React.ElementType; label: string }
> = {
  SCHEDULED: { color: 'badge-blue', icon: Clock, label: 'Scheduled' },
  VOLUNTEER_ASSIGNED: { color: 'badge-purple', icon: Truck, label: 'Volunteer Assigned' },
  EN_ROUTE: { color: 'badge-orange', icon: Truck, label: 'En Route' },
  ARRIVED: { color: 'badge-orange', icon: MapPin, label: 'Arrived' },
  PICKED_UP: { color: 'badge-green', icon: CheckCircle, label: 'Picked Up' },
  DELIVERED: { color: 'badge-green', icon: CheckCircle, label: 'Delivered' },
  COMPLETED: { color: 'badge-green', icon: CheckCircle, label: 'Completed' },
  CANCELLED: { color: 'badge-red', icon: XCircle, label: 'Cancelled' },
};

const nextStatusMap: Partial<Record<PickupStatus, PickupStatus>> = {
  SCHEDULED: 'EN_ROUTE',
  EN_ROUTE: 'ARRIVED',
  ARRIVED: 'PICKED_UP',
  PICKED_UP: 'DELIVERED',
  DELIVERED: 'COMPLETED',
};

const nextStatusLabels: Partial<Record<PickupStatus, string>> = {
  SCHEDULED: 'Start Pickup',
  EN_ROUTE: 'Mark Arrived',
  ARRIVED: 'Confirm Pickup',
  PICKED_UP: 'Mark Delivered',
  DELIVERED: 'Complete',
};

export default function PickupCard({ pickup, onUpdateStatus, onCancel, onRate, loading }: PickupCardProps) {
  const config = statusConfig[pickup.status];
  const StatusIcon = config.icon;
  const nextStatus = nextStatusMap[pickup.status];
  const nextLabel = nextStatusLabels[pickup.status];

  const isActive = !['COMPLETED', 'CANCELLED'].includes(pickup.status);

  return (
    <div className="card overflow-hidden">
      <div className="p-5">
        {/* Header */}
        <div className="flex items-start justify-between mb-4">
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 rounded-xl bg-gray-100 dark:bg-gray-800 overflow-hidden flex-shrink-0">
              {pickup.foodListing.images[0] ? (
                <img
                  src={pickup.foodListing.images[0]}
                  alt={pickup.foodListing.title}
                  className="h-full w-full object-cover"
                />
              ) : (
                <div className="flex items-center justify-center h-full">
                  <Truck className="h-5 w-5 text-gray-400" />
                </div>
              )}
            </div>
            <div>
              <h3 className="font-semibold text-gray-900 dark:text-white">
                {pickup.foodListing.title}
              </h3>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                {pickup.foodListing.restaurant.name}
              </p>
            </div>
          </div>
          <span className={`badge ${config.color} flex items-center gap-1`}>
            <StatusIcon className="h-3 w-3" />
            {config.label}
          </span>
        </div>

        {/* Details */}
        <div className="space-y-2 mb-4">
          <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
            <Clock className="h-4 w-4 text-gray-400" />
            <span>
              Scheduled: {format(new Date(pickup.scheduledAt), 'MMM d, yyyy h:mm a')}
            </span>
          </div>
          <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
            <MapPin className="h-4 w-4 text-gray-400" />
            <span>{pickup.foodListing.location.address || 'Location pending'}</span>
          </div>
          {pickup.volunteer && (
            <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
              <Phone className="h-4 w-4 text-gray-400" />
              <span>Volunteer: {pickup.volunteer.name}</span>
            </div>
          )}
        </div>

        {/* Progress Bar */}
        {isActive && (
          <div className="mb-4">
            <div className="flex items-center gap-1">
              {['SCHEDULED', 'EN_ROUTE', 'ARRIVED', 'PICKED_UP', 'DELIVERED', 'COMPLETED'].map(
                (status, index) => {
                  const statusOrder = [
                    'SCHEDULED',
                    'EN_ROUTE',
                    'ARRIVED',
                    'PICKED_UP',
                    'DELIVERED',
                    'COMPLETED',
                  ];
                  const currentIndex = statusOrder.indexOf(pickup.status);
                  const isCompleted = index <= currentIndex;
                  const isCurrent = index === currentIndex;

                  return (
                    <div
                      key={status}
                      className={`h-1.5 flex-1 rounded-full transition-colors ${
                        isCompleted
                          ? 'bg-primary-500'
                          : isCurrent
                            ? 'bg-primary-300'
                            : 'bg-gray-200 dark:bg-gray-700'
                      }`}
                    />
                  );
                },
              )}
            </div>
          </div>
        )}

        {/* QR Code */}
        {pickup.qrCode && isActive && (
          <div className="mb-4 p-3 rounded-xl bg-gray-50 dark:bg-gray-800/50 border border-gray-200 dark:border-gray-700 flex items-center gap-3">
            <div className="h-16 w-16 bg-white rounded-lg flex items-center justify-center flex-shrink-0">
              <QrCode className="h-10 w-10 text-gray-800" />
            </div>
            <div>
              <p className="text-sm font-medium text-gray-900 dark:text-white">
                Verification QR Code
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Show this code at pickup to verify your identity
              </p>
            </div>
          </div>
        )}

        {/* Notes */}
        {pickup.notes && (
          <div className="mb-4 p-3 rounded-lg bg-blue-50 dark:bg-blue-900/20 text-sm text-blue-700 dark:text-blue-300">
            {pickup.notes}
          </div>
        )}

        {/* Actions */}
        <div className="flex items-center gap-2 pt-3 border-t border-gray-100 dark:border-gray-800">
          {isActive && nextStatus && onUpdateStatus && (
            <button
              onClick={() => onUpdateStatus(pickup.id, nextStatus)}
              disabled={loading}
              className="btn-primary text-sm py-2 flex-1 gap-1"
            >
              {nextLabel}
              <ChevronRight className="h-4 w-4" />
            </button>
          )}

          {isActive && onCancel && (
            <button
              onClick={() => onCancel(pickup.id)}
              disabled={loading}
              className="btn-outline text-sm py-2 text-red-600 border-red-300 hover:bg-red-50 dark:text-red-400 dark:border-red-800 dark:hover:bg-red-900/20"
            >
              Cancel
            </button>
          )}

          {pickup.status === 'COMPLETED' && !pickup.rating && onRate && (
            <button
              onClick={() => onRate(pickup.id)}
              className="btn-secondary text-sm py-2 flex-1 gap-1"
            >
              <Star className="h-4 w-4" />
              Rate Pickup
            </button>
          )}

          {pickup.rating && (
            <div className="flex items-center gap-1 text-sm text-amber-500">
              {Array.from({ length: 5 }).map((_, i) => (
                <Star
                  key={i}
                  className={`h-4 w-4 ${
                    i < pickup.rating! ? 'fill-current' : 'text-gray-300 dark:text-gray-600'
                  }`}
                />
              ))}
            </div>
          )}

          {/* Time ago */}
          <span className="text-xs text-gray-400 dark:text-gray-500 ml-auto">
            {formatDistanceToNow(new Date(pickup.updatedAt), { addSuffix: true })}
          </span>
        </div>
      </div>
    </div>
  );
}

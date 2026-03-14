import { useState, useEffect, useCallback } from 'react';
import { Calendar, Clock, CheckCircle, XCircle, Truck } from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import PickupCard from '@/components/pickup/PickupCard';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import pickupService from '@/services/pickupService';
import type { Pickup, PickupStatus } from '@/types';
import toast from 'react-hot-toast';

type TabKey = 'scheduled' | 'in_progress' | 'completed' | 'cancelled';

const tabs: { key: TabKey; label: string; icon: React.ElementType; statuses: PickupStatus[] }[] = [
  { key: 'scheduled', label: 'Scheduled', icon: Calendar, statuses: ['SCHEDULED', 'VOLUNTEER_ASSIGNED'] },
  { key: 'in_progress', label: 'In Progress', icon: Truck, statuses: ['EN_ROUTE', 'ARRIVED', 'PICKED_UP', 'DELIVERED'] },
  { key: 'completed', label: 'Completed', icon: CheckCircle, statuses: ['COMPLETED'] },
  { key: 'cancelled', label: 'Cancelled', icon: XCircle, statuses: ['CANCELLED'] },
];

export default function PickupsPage() {
  const [activeTab, setActiveTab] = useState<TabKey>('scheduled');
  const [pickups, setPickups] = useState<Pickup[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  const currentTab = tabs.find((t) => t.key === activeTab)!;

  const loadPickups = useCallback(async () => {
    setLoading(true);
    try {
      const promises = currentTab.statuses.map((status) =>
        pickupService.getPickups({ status, size: 50 }),
      );
      const results = await Promise.all(promises);
      const allPickups = results.flatMap((r) => r.content);
      allPickups.sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime());
      setPickups(allPickups);
    } catch {
      toast.error('Failed to load pickups');
    } finally {
      setLoading(false);
    }
  }, [currentTab]);

  useEffect(() => {
    loadPickups();
  }, [loadPickups]);

  const handleUpdateStatus = async (id: string, status: PickupStatus) => {
    setActionLoading(true);
    try {
      await pickupService.updateStatus(id, status);
      toast.success('Pickup status updated');
      loadPickups();
    } catch {
      toast.error('Failed to update status');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async (id: string) => {
    if (!window.confirm('Are you sure you want to cancel this pickup?')) return;

    setActionLoading(true);
    try {
      await pickupService.cancelPickup(id, 'Cancelled by user');
      toast.success('Pickup cancelled');
      loadPickups();
    } catch {
      toast.error('Failed to cancel pickup');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRate = (id: string) => {
    // Rate modal would go here
    toast('Rating feature coming soon!', { icon: '⭐' });
    console.log('Rate pickup:', id);
  };

  return (
    <div className="flex min-h-[calc(100vh-4rem)]">
      <Sidebar />

      <div className="flex-1 overflow-y-auto">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              Pickup Management
            </h1>
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Track and manage your food pickups
            </p>
          </div>

          {/* Tabs */}
          <div className="flex gap-1 bg-gray-100 dark:bg-gray-800 p-1 rounded-xl mb-6 overflow-x-auto">
            {tabs.map((tab) => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key)}
                className={`flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-medium whitespace-nowrap transition-colors flex-1 justify-center ${
                  activeTab === tab.key
                    ? 'bg-white dark:bg-gray-900 text-gray-900 dark:text-white shadow-sm'
                    : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300'
                }`}
              >
                <tab.icon className="h-4 w-4" />
                {tab.label}
              </button>
            ))}
          </div>

          {/* Content */}
          {loading ? (
            <div className="flex items-center justify-center py-20">
              <LoadingSpinner size="lg" label="Loading pickups..." />
            </div>
          ) : pickups.length === 0 ? (
            <div className="text-center py-20">
              <div className="inline-flex h-20 w-20 items-center justify-center rounded-full bg-gray-100 dark:bg-gray-800 mb-4">
                <Clock className="h-10 w-10 text-gray-400" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                No {activeTab.replace('_', ' ')} pickups
              </h3>
              <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
                {activeTab === 'scheduled'
                  ? 'Claim some food to schedule a pickup!'
                  : 'No pickups in this category yet.'}
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {pickups.map((pickup) => (
                <PickupCard
                  key={pickup.id}
                  pickup={pickup}
                  onUpdateStatus={handleUpdateStatus}
                  onCancel={handleCancel}
                  onRate={handleRate}
                  loading={actionLoading}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Package,
  TrendingUp,
  Calendar,
  MapPin,
  PlusCircle,
  Clock,
  Users,
  Leaf,
  Truck,
  BarChart3,
} from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import StatCard from '@/components/common/StatCard';
import Sidebar from '@/components/common/Sidebar';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import type { FoodListing, Pickup, DashboardStats } from '@/types';
import { formatDistanceToNow } from 'date-fns';

export default function DashboardPage() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recentListings, setRecentListings] = useState<FoodListing[]>([]);
  const [upcomingPickups, setUpcomingPickups] = useState<Pickup[]>([]);

  useEffect(() => {
    // Simulated data load
    const timer = setTimeout(() => {
      setStats({
        totalFoodSaved: 1250,
        totalMealsProvided: 4800,
        totalCO2Saved: 2.4,
        totalMoneySaved: 15000,
        activeListings: 23,
        completedPickups: 156,
        activeUsers: 450,
        monthlyGrowth: 12.5,
      });
      setRecentListings([]);
      setUpcomingPickups([]);
      setLoading(false);
    }, 500);
    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <LoadingSpinner size="lg" label="Loading dashboard..." />
      </div>
    );
  }

  return (
    <div className="flex min-h-[calc(100vh-4rem)]">
      <Sidebar />

      <div className="flex-1 overflow-y-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Header */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
            <div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                Welcome back, {user?.name?.split(' ')[0]}
              </h1>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Here is what is happening with your food redistribution efforts.
              </p>
            </div>
            {user?.role === 'RESTAURANT' && (
              <Link to="/food/new" className="btn-primary gap-2">
                <PlusCircle className="h-4 w-4" />
                New Listing
              </Link>
            )}
          </div>

          {/* Stats Grid */}
          {stats && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
              {user?.role === 'RESTAURANT' && (
                <>
                  <StatCard
                    icon={<Package className="h-6 w-6" />}
                    label="Active Listings"
                    value={stats.activeListings}
                    trend={{ value: 8, label: 'vs last week' }}
                    color="green"
                  />
                  <StatCard
                    icon={<Truck className="h-6 w-6" />}
                    label="Completed Pickups"
                    value={stats.completedPickups}
                    trend={{ value: 12, label: 'vs last month' }}
                    color="blue"
                  />
                  <StatCard
                    icon={<Leaf className="h-6 w-6" />}
                    label="Food Saved (kg)"
                    value={stats.totalFoodSaved.toLocaleString()}
                    trend={{ value: 15, label: 'vs last month' }}
                    color="orange"
                  />
                  <StatCard
                    icon={<TrendingUp className="h-6 w-6" />}
                    label="Money Saved"
                    value={`$${stats.totalMoneySaved.toLocaleString()}`}
                    trend={{ value: stats.monthlyGrowth, label: 'monthly growth' }}
                    color="purple"
                  />
                </>
              )}

              {user?.role === 'NGO' && (
                <>
                  <StatCard
                    icon={<MapPin className="h-6 w-6" />}
                    label="Nearby Available"
                    value={stats.activeListings}
                    color="green"
                  />
                  <StatCard
                    icon={<Calendar className="h-6 w-6" />}
                    label="Scheduled Pickups"
                    value={7}
                    color="blue"
                  />
                  <StatCard
                    icon={<Users className="h-6 w-6" />}
                    label="Meals Distributed"
                    value={stats.totalMealsProvided.toLocaleString()}
                    trend={{ value: 18, label: 'vs last month' }}
                    color="orange"
                  />
                  <StatCard
                    icon={<Leaf className="h-6 w-6" />}
                    label="CO2 Saved (T)"
                    value={stats.totalCO2Saved.toFixed(1)}
                    color="purple"
                  />
                </>
              )}

              {user?.role === 'CITIZEN' && (
                <>
                  <StatCard
                    icon={<MapPin className="h-6 w-6" />}
                    label="Nearby Food"
                    value={stats.activeListings}
                    color="green"
                  />
                  <StatCard
                    icon={<Package className="h-6 w-6" />}
                    label="Items Claimed"
                    value={34}
                    color="blue"
                  />
                  <StatCard
                    icon={<Truck className="h-6 w-6" />}
                    label="Pickups Completed"
                    value={28}
                    color="orange"
                  />
                  <StatCard
                    icon={<BarChart3 className="h-6 w-6" />}
                    label="Impact Score"
                    value={420}
                    color="purple"
                  />
                </>
              )}

              {user?.role === 'ADMIN' && (
                <>
                  <StatCard
                    icon={<Users className="h-6 w-6" />}
                    label="Active Users"
                    value={stats.activeUsers}
                    trend={{ value: stats.monthlyGrowth, label: 'monthly growth' }}
                    color="green"
                  />
                  <StatCard
                    icon={<Package className="h-6 w-6" />}
                    label="Active Listings"
                    value={stats.activeListings}
                    color="blue"
                  />
                  <StatCard
                    icon={<Leaf className="h-6 w-6" />}
                    label="Total Food Saved (kg)"
                    value={stats.totalFoodSaved.toLocaleString()}
                    color="orange"
                  />
                  <StatCard
                    icon={<Truck className="h-6 w-6" />}
                    label="Total Pickups"
                    value={stats.completedPickups}
                    color="purple"
                  />
                </>
              )}
            </div>
          )}

          {/* Content Grid */}
          <div className="grid lg:grid-cols-2 gap-6">
            {/* Recent Activity / Listings */}
            <div className="card">
              <div className="p-6 border-b border-gray-100 dark:border-gray-800">
                <div className="flex items-center justify-between">
                  <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                    {user?.role === 'RESTAURANT' ? 'Your Recent Listings' : 'Nearby Food'}
                  </h2>
                  <Link to="/food" className="text-sm text-primary-600 hover:text-primary-500 font-medium">
                    View all
                  </Link>
                </div>
              </div>
              <div className="divide-y divide-gray-100 dark:divide-gray-800">
                {recentListings.length === 0 ? (
                  <div className="p-8 text-center">
                    <Package className="h-12 w-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      No recent listings found.
                    </p>
                    {user?.role === 'RESTAURANT' && (
                      <Link to="/food/new" className="btn-primary mt-4 text-sm px-4 py-2 gap-2 inline-flex">
                        <PlusCircle className="h-4 w-4" />
                        Create Your First Listing
                      </Link>
                    )}
                  </div>
                ) : (
                  recentListings.map((listing) => (
                    <Link
                      key={listing.id}
                      to={`/food/${listing.id}`}
                      className="flex items-center gap-4 p-4 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors"
                    >
                      <div className="h-12 w-12 rounded-lg bg-gray-100 dark:bg-gray-800 flex-shrink-0 overflow-hidden">
                        {listing.images[0] ? (
                          <img src={listing.images[0]} alt={listing.title} className="h-full w-full object-cover" />
                        ) : (
                          <Package className="h-6 w-6 text-gray-400 m-3" />
                        )}
                      </div>
                      <div className="min-w-0 flex-1">
                        <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
                          {listing.title}
                        </p>
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          {listing.quantity} {listing.unit} &middot;{' '}
                          {formatDistanceToNow(new Date(listing.createdAt), { addSuffix: true })}
                        </p>
                      </div>
                      <span
                        className={`badge text-xs ${
                          listing.status === 'AVAILABLE'
                            ? 'badge-green'
                            : listing.status === 'CLAIMED'
                              ? 'badge-blue'
                              : 'badge-orange'
                        }`}
                      >
                        {listing.status}
                      </span>
                    </Link>
                  ))
                )}
              </div>
            </div>

            {/* Upcoming Pickups */}
            <div className="card">
              <div className="p-6 border-b border-gray-100 dark:border-gray-800">
                <div className="flex items-center justify-between">
                  <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                    Upcoming Pickups
                  </h2>
                  <Link to="/pickups" className="text-sm text-primary-600 hover:text-primary-500 font-medium">
                    View all
                  </Link>
                </div>
              </div>
              <div className="divide-y divide-gray-100 dark:divide-gray-800">
                {upcomingPickups.length === 0 ? (
                  <div className="p-8 text-center">
                    <Clock className="h-12 w-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      No upcoming pickups scheduled.
                    </p>
                    <Link to="/food" className="btn-secondary mt-4 text-sm px-4 py-2 inline-flex">
                      Browse Available Food
                    </Link>
                  </div>
                ) : (
                  upcomingPickups.map((pickup) => (
                    <div
                      key={pickup.id}
                      className="flex items-center gap-4 p-4 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors"
                    >
                      <div className="h-10 w-10 rounded-full bg-secondary-50 dark:bg-secondary-900/20 flex items-center justify-center flex-shrink-0">
                        <Truck className="h-5 w-5 text-secondary-600 dark:text-secondary-400" />
                      </div>
                      <div className="min-w-0 flex-1">
                        <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
                          {pickup.foodListing.title}
                        </p>
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          Scheduled for{' '}
                          {formatDistanceToNow(new Date(pickup.scheduledAt), { addSuffix: true })}
                        </p>
                      </div>
                      <span className="badge badge-blue text-xs">{pickup.status}</span>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

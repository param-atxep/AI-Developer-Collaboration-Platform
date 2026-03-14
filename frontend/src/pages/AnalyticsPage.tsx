import { useState, useEffect } from 'react';
import { Leaf, Users, Wind, DollarSign } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import Sidebar from '@/components/common/Sidebar';
import StatCard from '@/components/common/StatCard';
import ImpactChart from '@/components/analytics/ImpactChart';
import LeaderboardTable from '@/components/analytics/LeaderboardTable';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import analyticsService from '@/services/analyticsService';
import type { AnalyticsDashboard } from '@/types';

const CATEGORY_COLORS = ['#22c55e', '#3b82f6', '#f59e0b', '#8b5cf6', '#ec4899', '#6b7280'];

// Mock data for initial render
const mockAnalytics: AnalyticsDashboard = {
  stats: {
    totalFoodSaved: 12500,
    totalMealsProvided: 48000,
    totalCO2Saved: 24.5,
    totalMoneySaved: 150000,
    activeListings: 230,
    completedPickups: 1560,
    activeUsers: 4500,
    monthlyGrowth: 12.5,
  },
  foodSavedTimeSeries: Array.from({ length: 30 }, (_, i) => ({
    date: new Date(Date.now() - (29 - i) * 86400000).toISOString(),
    value: Math.floor(Math.random() * 200 + 300),
  })),
  categoryBreakdown: [
    { category: 'Prepared Meals', count: 450, percentage: 35, color: '#22c55e' },
    { category: 'Fresh Produce', count: 320, percentage: 25, color: '#3b82f6' },
    { category: 'Bakery', count: 200, percentage: 16, color: '#f59e0b' },
    { category: 'Dairy', count: 150, percentage: 12, color: '#8b5cf6' },
    { category: 'Beverages', count: 80, percentage: 6, color: '#ec4899' },
    { category: 'Other', count: 80, percentage: 6, color: '#6b7280' },
  ],
  leaderboard: [
    { rank: 1, userId: '1', name: 'Green Garden Restaurant', organization: 'Downtown', foodSaved: 2500, mealsProvided: 8500, score: 9800 },
    { rank: 2, userId: '2', name: 'Farm Fresh Kitchen', organization: 'Midtown', foodSaved: 2100, mealsProvided: 7200, score: 8500 },
    { rank: 3, userId: '3', name: 'Sunset Diner', organization: 'Brooklyn', foodSaved: 1800, mealsProvided: 6100, score: 7200 },
    { rank: 4, userId: '4', name: 'Harbor Bistro', organization: 'Queens', foodSaved: 1500, mealsProvided: 5000, score: 6100 },
    { rank: 5, userId: '5', name: 'River Cafe', organization: 'Manhattan', foodSaved: 1200, mealsProvided: 4000, score: 5000 },
  ],
};

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<AnalyticsDashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState<'week' | 'month' | 'year'>('month');

  useEffect(() => {
    const loadAnalytics = async () => {
      try {
        const data = await analyticsService.getDashboard();
        setAnalytics(data);
      } catch {
        // Use mock data as fallback
        setAnalytics(mockAnalytics);
      } finally {
        setLoading(false);
      }
    };
    loadAnalytics();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <LoadingSpinner size="lg" label="Loading analytics..." />
      </div>
    );
  }

  if (!analytics) return null;

  const { stats } = analytics;

  const CustomPieTooltip = ({
    active,
    payload,
  }: {
    active?: boolean;
    payload?: Array<{ name: string; value: number; payload: { percentage: number } }>;
  }) => {
    if (!active || !payload?.length) return null;
    return (
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-3 text-sm">
        <p className="font-medium text-gray-900 dark:text-white">{payload[0].name}</p>
        <p className="text-gray-500 dark:text-gray-400">
          {payload[0].value} listings ({payload[0].payload.percentage}%)
        </p>
      </div>
    );
  };

  return (
    <div className="flex min-h-[calc(100vh-4rem)]">
      <Sidebar />

      <div className="flex-1 overflow-y-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Header */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
            <div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                Analytics Dashboard
              </h1>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Track your impact on reducing food waste
              </p>
            </div>
            <div className="flex gap-1 bg-gray-100 dark:bg-gray-800 p-1 rounded-lg">
              {(['week', 'month', 'year'] as const).map((p) => (
                <button
                  key={p}
                  onClick={() => setPeriod(p)}
                  className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                    period === p
                      ? 'bg-white dark:bg-gray-700 text-gray-900 dark:text-white shadow-sm'
                      : 'text-gray-500 dark:text-gray-400 hover:text-gray-700'
                  }`}
                >
                  {p.charAt(0).toUpperCase() + p.slice(1)}
                </button>
              ))}
            </div>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard
              icon={<Leaf className="h-6 w-6" />}
              label="Total Food Saved"
              value={`${(stats.totalFoodSaved / 1000).toFixed(1)}T`}
              trend={{ value: 15, label: 'vs last month' }}
              color="green"
            />
            <StatCard
              icon={<Wind className="h-6 w-6" />}
              label="CO2 Emissions Saved"
              value={`${stats.totalCO2Saved.toFixed(1)}T`}
              trend={{ value: 12, label: 'vs last month' }}
              color="blue"
            />
            <StatCard
              icon={<Users className="h-6 w-6" />}
              label="Meals Provided"
              value={stats.totalMealsProvided.toLocaleString()}
              trend={{ value: 18, label: 'vs last month' }}
              color="orange"
            />
            <StatCard
              icon={<DollarSign className="h-6 w-6" />}
              label="Money Saved"
              value={`$${(stats.totalMoneySaved / 1000).toFixed(0)}K`}
              trend={{ value: stats.monthlyGrowth, label: 'monthly growth' }}
              color="purple"
            />
          </div>

          {/* Charts Row */}
          <div className="grid lg:grid-cols-3 gap-6 mb-6">
            {/* Impact Chart */}
            <div className="lg:col-span-2">
              <ImpactChart data={analytics.foodSavedTimeSeries} />
            </div>

            {/* Category Breakdown */}
            <div className="card p-6">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                Category Breakdown
              </h3>
              <div className="h-56">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={analytics.categoryBreakdown}
                      dataKey="count"
                      nameKey="category"
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={90}
                      paddingAngle={2}
                    >
                      {analytics.categoryBreakdown.map((_, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={CATEGORY_COLORS[index % CATEGORY_COLORS.length]}
                        />
                      ))}
                    </Pie>
                    <Tooltip content={<CustomPieTooltip />} />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              {/* Legend */}
              <div className="space-y-2 mt-4">
                {analytics.categoryBreakdown.map((cat, index) => (
                  <div key={cat.category} className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div
                        className="h-3 w-3 rounded-full"
                        style={{ backgroundColor: CATEGORY_COLORS[index % CATEGORY_COLORS.length] }}
                      />
                      <span className="text-sm text-gray-600 dark:text-gray-400">
                        {cat.category}
                      </span>
                    </div>
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      {cat.percentage}%
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Leaderboard */}
          <LeaderboardTable entries={analytics.leaderboard} />
        </div>
      </div>
    </div>
  );
}

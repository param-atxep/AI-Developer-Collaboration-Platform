import { useState, useEffect } from 'react';
import {
  Users,
  Shield,
  Package,
  Activity,
  Search,
  MoreVertical,
  CheckCircle,
  XCircle,
  Ban,
  Eye,
  Leaf,
  Truck,
  TrendingUp,
  Server,
  Database,
  Wifi,
} from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import StatCard from '@/components/common/StatCard';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import type { User, UserRole } from '@/types';
import toast from 'react-hot-toast';

// Mock admin data
const mockUsers: (User & { status: 'active' | 'pending' | 'banned' })[] = [
  {
    id: '1', email: 'greengardn@email.com', name: 'Green Garden Restaurant', role: 'RESTAURANT',
    verified: true, badges: [], createdAt: '2024-01-15T00:00:00Z', updatedAt: '2024-01-15T00:00:00Z',
    organization: 'Green Garden LLC', status: 'active',
  },
  {
    id: '2', email: 'community@ngo.org', name: 'Community Food Bank', role: 'NGO',
    verified: true, badges: [], createdAt: '2024-02-20T00:00:00Z', updatedAt: '2024-02-20T00:00:00Z',
    organization: 'CFB Foundation', status: 'active',
  },
  {
    id: '3', email: 'jane@email.com', name: 'Jane Smith', role: 'CITIZEN',
    verified: false, badges: [], createdAt: '2024-03-10T00:00:00Z', updatedAt: '2024-03-10T00:00:00Z',
    status: 'pending',
  },
  {
    id: '4', email: 'sunset@diner.com', name: 'Sunset Diner', role: 'RESTAURANT',
    verified: true, badges: [], createdAt: '2024-01-05T00:00:00Z', updatedAt: '2024-01-05T00:00:00Z',
    organization: 'Sunset Foods Inc', status: 'active',
  },
  {
    id: '5', email: 'spam@fake.com', name: 'Spam Account', role: 'CITIZEN',
    verified: false, badges: [], createdAt: '2024-04-01T00:00:00Z', updatedAt: '2024-04-01T00:00:00Z',
    status: 'banned',
  },
];

const mockSystemHealth = {
  uptime: 99.9,
  activeConnections: 234,
  requestsPerMinute: 1250,
  errorRate: 0.02,
  dbStatus: 'healthy',
  cacheStatus: 'healthy',
};

export default function AdminPage() {
  const [users, setUsers] = useState(mockUsers);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState<UserRole | ''>('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [openMenuId, setOpenMenuId] = useState<string | null>(null);

  useEffect(() => {
    const timer = setTimeout(() => setLoading(false), 500);
    return () => clearTimeout(timer);
  }, []);

  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesRole = !roleFilter || user.role === roleFilter;
    const matchesStatus = !statusFilter || user.status === statusFilter;
    return matchesSearch && matchesRole && matchesStatus;
  });

  const handleApprove = (userId: string) => {
    setUsers((prev) =>
      prev.map((u) => (u.id === userId ? { ...u, verified: true, status: 'active' as const } : u)),
    );
    toast.success('User approved successfully');
    setOpenMenuId(null);
  };

  const handleBan = (userId: string) => {
    if (!window.confirm('Are you sure you want to ban this user?')) return;
    setUsers((prev) =>
      prev.map((u) => (u.id === userId ? { ...u, status: 'banned' as const } : u)),
    );
    toast.success('User has been banned');
    setOpenMenuId(null);
  };

  const handleUnban = (userId: string) => {
    setUsers((prev) =>
      prev.map((u) => (u.id === userId ? { ...u, status: 'active' as const } : u)),
    );
    toast.success('User has been unbanned');
    setOpenMenuId(null);
  };

  const statusBadge = (status: string) => {
    switch (status) {
      case 'active':
        return <span className="badge badge-green">Active</span>;
      case 'pending':
        return <span className="badge badge-orange">Pending</span>;
      case 'banned':
        return <span className="badge badge-red">Banned</span>;
      default:
        return null;
    }
  };

  const roleBadge = (role: UserRole) => {
    switch (role) {
      case 'RESTAURANT':
        return <span className="badge badge-blue">Restaurant</span>;
      case 'NGO':
        return <span className="badge badge-purple">NGO</span>;
      case 'CITIZEN':
        return <span className="badge badge-green">Citizen</span>;
      case 'ADMIN':
        return <span className="badge badge-red">Admin</span>;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <LoadingSpinner size="lg" label="Loading admin panel..." />
      </div>
    );
  }

  return (
    <div className="flex min-h-[calc(100vh-4rem)]">
      <Sidebar />

      <div className="flex-1 overflow-y-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
              <Shield className="h-6 w-6 text-primary-500" />
              Admin Panel
            </h1>
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Manage users, monitor platform health, and oversee operations
            </p>
          </div>

          {/* Platform Stats */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard
              icon={<Users className="h-6 w-6" />}
              label="Total Users"
              value={users.length}
              trend={{ value: 12, label: 'this month' }}
              color="green"
            />
            <StatCard
              icon={<Package className="h-6 w-6" />}
              label="Active Listings"
              value={230}
              color="blue"
            />
            <StatCard
              icon={<Leaf className="h-6 w-6" />}
              label="Food Saved (T)"
              value="12.5"
              trend={{ value: 15, label: 'vs last month' }}
              color="orange"
            />
            <StatCard
              icon={<Truck className="h-6 w-6" />}
              label="Total Pickups"
              value="1,560"
              color="purple"
            />
          </div>

          {/* System Health */}
          <div className="card p-6 mb-6">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
              <Activity className="h-5 w-5 text-primary-500" />
              System Health
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
              <div className="p-3 rounded-xl bg-green-50 dark:bg-green-900/20 text-center">
                <TrendingUp className="h-5 w-5 text-green-500 mx-auto mb-1" />
                <p className="text-lg font-bold text-gray-900 dark:text-white">{mockSystemHealth.uptime}%</p>
                <p className="text-xs text-gray-500">Uptime</p>
              </div>
              <div className="p-3 rounded-xl bg-blue-50 dark:bg-blue-900/20 text-center">
                <Wifi className="h-5 w-5 text-blue-500 mx-auto mb-1" />
                <p className="text-lg font-bold text-gray-900 dark:text-white">{mockSystemHealth.activeConnections}</p>
                <p className="text-xs text-gray-500">Connections</p>
              </div>
              <div className="p-3 rounded-xl bg-purple-50 dark:bg-purple-900/20 text-center">
                <Activity className="h-5 w-5 text-purple-500 mx-auto mb-1" />
                <p className="text-lg font-bold text-gray-900 dark:text-white">{mockSystemHealth.requestsPerMinute}</p>
                <p className="text-xs text-gray-500">Req/min</p>
              </div>
              <div className="p-3 rounded-xl bg-amber-50 dark:bg-amber-900/20 text-center">
                <Activity className="h-5 w-5 text-amber-500 mx-auto mb-1" />
                <p className="text-lg font-bold text-gray-900 dark:text-white">{mockSystemHealth.errorRate}%</p>
                <p className="text-xs text-gray-500">Error Rate</p>
              </div>
              <div className="p-3 rounded-xl bg-green-50 dark:bg-green-900/20 text-center">
                <Database className="h-5 w-5 text-green-500 mx-auto mb-1" />
                <p className="text-sm font-bold text-green-600 dark:text-green-400 capitalize">{mockSystemHealth.dbStatus}</p>
                <p className="text-xs text-gray-500">Database</p>
              </div>
              <div className="p-3 rounded-xl bg-green-50 dark:bg-green-900/20 text-center">
                <Server className="h-5 w-5 text-green-500 mx-auto mb-1" />
                <p className="text-sm font-bold text-green-600 dark:text-green-400 capitalize">{mockSystemHealth.cacheStatus}</p>
                <p className="text-xs text-gray-500">Cache</p>
              </div>
            </div>
          </div>

          {/* User Management */}
          <div className="card overflow-hidden">
            <div className="p-6 border-b border-gray-100 dark:border-gray-800">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                User Management
              </h3>
              <div className="flex flex-col sm:flex-row gap-3">
                <div className="relative flex-1">
                  <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search users..."
                    className="input-field pl-10 text-sm"
                  />
                </div>
                <select
                  value={roleFilter}
                  onChange={(e) => setRoleFilter(e.target.value as UserRole | '')}
                  className="input-field w-auto text-sm"
                >
                  <option value="">All Roles</option>
                  <option value="RESTAURANT">Restaurant</option>
                  <option value="NGO">NGO</option>
                  <option value="CITIZEN">Citizen</option>
                </select>
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className="input-field w-auto text-sm"
                >
                  <option value="">All Statuses</option>
                  <option value="active">Active</option>
                  <option value="pending">Pending</option>
                  <option value="banned">Banned</option>
                </select>
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-100 dark:border-gray-800">
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">User</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Role</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Status</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Verified</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Joined</th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                  {filteredUsers.map((user) => (
                    <tr key={user.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-3">
                          <div className="h-9 w-9 rounded-full bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center text-primary-700 dark:text-primary-400 text-sm font-semibold">
                            {user.name.charAt(0).toUpperCase()}
                          </div>
                          <div>
                            <p className="text-sm font-medium text-gray-900 dark:text-white">{user.name}</p>
                            <p className="text-xs text-gray-500 dark:text-gray-400">{user.email}</p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">{roleBadge(user.role)}</td>
                      <td className="px-6 py-4 whitespace-nowrap">{statusBadge(user.status)}</td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {user.verified ? (
                          <CheckCircle className="h-5 w-5 text-green-500" />
                        ) : (
                          <XCircle className="h-5 w-5 text-gray-300 dark:text-gray-600" />
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                        {new Date(user.createdAt).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right">
                        <div className="relative">
                          <button
                            onClick={() => setOpenMenuId(openMenuId === user.id ? null : user.id)}
                            className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                          >
                            <MoreVertical className="h-4 w-4 text-gray-400" />
                          </button>

                          {openMenuId === user.id && (
                            <div className="absolute right-0 mt-1 w-40 rounded-xl bg-white shadow-lg ring-1 ring-black/5 dark:bg-gray-800 dark:ring-gray-700 z-20 animate-slide-down">
                              <div className="p-1">
                                <button className="flex w-full items-center gap-2 px-3 py-2 text-sm text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700">
                                  <Eye className="h-4 w-4" />
                                  View Profile
                                </button>
                                {user.status === 'pending' && (
                                  <button
                                    onClick={() => handleApprove(user.id)}
                                    className="flex w-full items-center gap-2 px-3 py-2 text-sm text-green-600 rounded-lg hover:bg-green-50 dark:text-green-400 dark:hover:bg-green-900/20"
                                  >
                                    <CheckCircle className="h-4 w-4" />
                                    Approve
                                  </button>
                                )}
                                {user.status !== 'banned' ? (
                                  <button
                                    onClick={() => handleBan(user.id)}
                                    className="flex w-full items-center gap-2 px-3 py-2 text-sm text-red-600 rounded-lg hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/20"
                                  >
                                    <Ban className="h-4 w-4" />
                                    Ban User
                                  </button>
                                ) : (
                                  <button
                                    onClick={() => handleUnban(user.id)}
                                    className="flex w-full items-center gap-2 px-3 py-2 text-sm text-green-600 rounded-lg hover:bg-green-50 dark:text-green-400 dark:hover:bg-green-900/20"
                                  >
                                    <CheckCircle className="h-4 w-4" />
                                    Unban
                                  </button>
                                )}
                              </div>
                            </div>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {filteredUsers.length === 0 && (
              <div className="p-8 text-center">
                <Users className="h-10 w-10 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  No users match your search criteria.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

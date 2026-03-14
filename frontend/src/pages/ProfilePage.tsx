import { useState } from 'react';
import {
  User,
  Mail,
  Phone,
  MapPin,
  Building,
  Clock,
  Camera,
  Save,
  Award,
  Star,
  Leaf,
  Users,
} from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { useAppDispatch } from '@/store';
import { updateProfile } from '@/store/authSlice';
import Sidebar from '@/components/common/Sidebar';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import type { OperatingHours } from '@/types';

const daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

export default function ProfilePage() {
  const { user } = useAuth();
  const dispatch = useAppDispatch();
  const [loading, setLoading] = useState(false);

  const [name, setName] = useState(user?.name || '');
  const [phone, setPhone] = useState(user?.phone || '');
  const [bio, setBio] = useState(user?.bio || '');
  const [organization, setOrganization] = useState(user?.organization || '');
  const [cuisineType, setCuisineType] = useState(user?.cuisineType || '');
  const [address, setAddress] = useState(user?.location?.address || '');
  const [city, setCity] = useState(user?.location?.city || '');
  const [state, setState] = useState(user?.location?.state || '');
  const [operatingHours, setOperatingHours] = useState<OperatingHours[]>(
    user?.operatingHours ||
      daysOfWeek.map((day) => ({
        day,
        openTime: '09:00',
        closeTime: '22:00',
        closed: day === 'Sunday',
      })),
  );

  const updateHours = (index: number, field: keyof OperatingHours, value: string | boolean) => {
    setOperatingHours((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      return updated;
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await dispatch(
        updateProfile({
          name,
          phone: phone || undefined,
          bio: bio || undefined,
          organization: organization || undefined,
          cuisineType: cuisineType || undefined,
          location: {
            latitude: user?.location?.latitude || 0,
            longitude: user?.location?.longitude || 0,
            address,
            city,
            state,
          },
          operatingHours: user?.role === 'RESTAURANT' ? operatingHours : undefined,
        }),
      ).unwrap();
      toast.success('Profile updated successfully');
    } catch {
      toast.error('Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  // Mock badges for display
  const badges = user?.badges || [
    { id: '1', name: 'First Donation', description: 'Made your first food donation', icon: '🌱', earnedAt: '2024-01-15' },
    { id: '2', name: 'Community Hero', description: 'Provided 100+ meals', icon: '🦸', earnedAt: '2024-03-20' },
    { id: '3', name: 'Eco Warrior', description: 'Saved 50kg of CO2', icon: '🌍', earnedAt: '2024-05-10' },
  ];

  return (
    <div className="flex min-h-[calc(100vh-4rem)]">
      <Sidebar />

      <div className="flex-1 overflow-y-auto">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-8">
            Profile Settings
          </h1>

          {/* Avatar Section */}
          <div className="card p-6 mb-6">
            <div className="flex items-center gap-6">
              <div className="relative">
                <div className="h-24 w-24 rounded-full bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center text-primary-700 dark:text-primary-400 text-3xl font-bold">
                  {user?.avatar ? (
                    <img src={user.avatar} alt={user.name} className="h-24 w-24 rounded-full object-cover" />
                  ) : (
                    user?.name?.charAt(0).toUpperCase()
                  )}
                </div>
                <button className="absolute bottom-0 right-0 h-8 w-8 rounded-full bg-primary-500 text-white flex items-center justify-center shadow-lg hover:bg-primary-600 transition-colors">
                  <Camera className="h-4 w-4" />
                </button>
              </div>
              <div>
                <h2 className="text-xl font-bold text-gray-900 dark:text-white">{user?.name}</h2>
                <p className="text-sm text-gray-500 dark:text-gray-400">{user?.email}</p>
                <span className="mt-1 inline-block badge-green">{user?.role}</span>
              </div>
            </div>
          </div>

          {/* Badges */}
          {badges.length > 0 && (
            <div className="card p-6 mb-6">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
                <Award className="h-5 w-5 text-amber-500" />
                Badges & Achievements
              </h3>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                {badges.map((badge) => (
                  <div
                    key={badge.id}
                    className="p-4 rounded-xl bg-gradient-to-br from-amber-50 to-orange-50 dark:from-amber-900/20 dark:to-orange-900/20 border border-amber-200 dark:border-amber-800/30 text-center"
                  >
                    <span className="text-3xl mb-2 block">{badge.icon}</span>
                    <p className="text-sm font-semibold text-gray-900 dark:text-white">{badge.name}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">{badge.description}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Impact Stats */}
          <div className="card p-6 mb-6">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
              Your Impact
            </h3>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="text-center p-3">
                <Leaf className="h-6 w-6 text-primary-500 mx-auto mb-2" />
                <p className="text-2xl font-bold text-gray-900 dark:text-white">245</p>
                <p className="text-xs text-gray-500">kg Food Saved</p>
              </div>
              <div className="text-center p-3">
                <Users className="h-6 w-6 text-secondary-500 mx-auto mb-2" />
                <p className="text-2xl font-bold text-gray-900 dark:text-white">820</p>
                <p className="text-xs text-gray-500">Meals Provided</p>
              </div>
              <div className="text-center p-3">
                <Star className="h-6 w-6 text-amber-500 mx-auto mb-2" />
                <p className="text-2xl font-bold text-gray-900 dark:text-white">4.8</p>
                <p className="text-xs text-gray-500">Avg Rating</p>
              </div>
              <div className="text-center p-3">
                <Award className="h-6 w-6 text-purple-500 mx-auto mb-2" />
                <p className="text-2xl font-bold text-gray-900 dark:text-white">12</p>
                <p className="text-xs text-gray-500">Day Streak</p>
              </div>
            </div>
          </div>

          {/* Edit Form */}
          <form onSubmit={handleSubmit} className="card p-6 space-y-6">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
              Edit Profile
            </h3>

            <div className="grid sm:grid-cols-2 gap-4">
              <div>
                <label htmlFor="profile-name" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                  <User className="inline h-4 w-4 mr-1" />
                  Full Name
                </label>
                <input
                  id="profile-name"
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="input-field"
                />
              </div>

              <div>
                <label htmlFor="profile-phone" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                  <Phone className="inline h-4 w-4 mr-1" />
                  Phone Number
                </label>
                <input
                  id="profile-phone"
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  className="input-field"
                />
              </div>
            </div>

            <div>
              <label htmlFor="profile-email" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                <Mail className="inline h-4 w-4 mr-1" />
                Email Address
              </label>
              <input
                id="profile-email"
                type="email"
                value={user?.email || ''}
                className="input-field bg-gray-50 dark:bg-gray-800/50"
                disabled
              />
              <p className="text-xs text-gray-400 mt-1">Email cannot be changed</p>
            </div>

            <div>
              <label htmlFor="profile-bio" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                Bio
              </label>
              <textarea
                id="profile-bio"
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                rows={3}
                placeholder="Tell us about yourself or your organization..."
                className="input-field resize-y"
              />
            </div>

            {/* Restaurant-specific fields */}
            {user?.role === 'RESTAURANT' && (
              <>
                <div className="grid sm:grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="profile-org" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                      <Building className="inline h-4 w-4 mr-1" />
                      Restaurant Name
                    </label>
                    <input
                      id="profile-org"
                      type="text"
                      value={organization}
                      onChange={(e) => setOrganization(e.target.value)}
                      className="input-field"
                    />
                  </div>

                  <div>
                    <label htmlFor="profile-cuisine" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                      Cuisine Type
                    </label>
                    <input
                      id="profile-cuisine"
                      type="text"
                      value={cuisineType}
                      onChange={(e) => setCuisineType(e.target.value)}
                      placeholder="Italian, Mexican, Asian..."
                      className="input-field"
                    />
                  </div>
                </div>

                {/* Operating Hours */}
                <div>
                  <h4 className="text-sm font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
                    <Clock className="h-4 w-4" />
                    Operating Hours
                  </h4>
                  <div className="space-y-2">
                    {operatingHours.map((hours, index) => (
                      <div key={hours.day} className="flex items-center gap-3">
                        <label className="w-24 text-sm text-gray-600 dark:text-gray-400">
                          {hours.day.slice(0, 3)}
                        </label>
                        <label className="flex items-center gap-2 cursor-pointer">
                          <input
                            type="checkbox"
                            checked={!hours.closed}
                            onChange={(e) => updateHours(index, 'closed', !e.target.checked)}
                            className="rounded border-gray-300 text-primary-500 focus:ring-primary-500"
                          />
                          <span className="text-xs text-gray-500">{hours.closed ? 'Closed' : 'Open'}</span>
                        </label>
                        {!hours.closed && (
                          <>
                            <input
                              type="time"
                              value={hours.openTime}
                              onChange={(e) => updateHours(index, 'openTime', e.target.value)}
                              className="input-field w-auto text-xs py-1.5 px-2"
                            />
                            <span className="text-gray-400">to</span>
                            <input
                              type="time"
                              value={hours.closeTime}
                              onChange={(e) => updateHours(index, 'closeTime', e.target.value)}
                              className="input-field w-auto text-xs py-1.5 px-2"
                            />
                          </>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              </>
            )}

            {/* Location */}
            <div>
              <h4 className="text-sm font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
                <MapPin className="h-4 w-4" />
                Location
              </h4>
              <div className="grid sm:grid-cols-3 gap-4">
                <div className="sm:col-span-3">
                  <input
                    type="text"
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    placeholder="Street Address"
                    className="input-field"
                  />
                </div>
                <div>
                  <input
                    type="text"
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    placeholder="City"
                    className="input-field"
                  />
                </div>
                <div>
                  <input
                    type="text"
                    value={state}
                    onChange={(e) => setState(e.target.value)}
                    placeholder="State"
                    className="input-field"
                  />
                </div>
              </div>
            </div>

            {/* Submit */}
            <div className="flex justify-end pt-4 border-t border-gray-200 dark:border-gray-800">
              <button type="submit" disabled={loading} className="btn-primary gap-2">
                {loading ? <LoadingSpinner size="sm" /> : <><Save className="h-4 w-4" /> Save Changes</>}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

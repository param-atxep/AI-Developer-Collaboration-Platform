import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { List, SlidersHorizontal, Clock, MapPin, Users, ChevronRight } from 'lucide-react';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchListings } from '@/store/foodSlice';
import FoodMap, { FoodMapFallback } from '@/components/map/FoodMap';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { formatDistanceToNow } from 'date-fns';
import type { FoodCategory } from '@/types';

const categories: { value: FoodCategory | ''; label: string }[] = [
  { value: '', label: 'All' },
  { value: 'PREPARED_MEALS', label: 'Meals' },
  { value: 'FRESH_PRODUCE', label: 'Produce' },
  { value: 'BAKERY', label: 'Bakery' },
  { value: 'DAIRY', label: 'Dairy' },
  { value: 'BEVERAGES', label: 'Drinks' },
];

export default function MapPage() {
  const dispatch = useAppDispatch();
  const { listings, loading } = useAppSelector((state) => state.food);
  const [selectedCategory, setSelectedCategory] = useState<FoodCategory | ''>('');
  const [showSidePanel, setShowSidePanel] = useState(true);
  const hasGoogleMapsKey = !!import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

  useEffect(() => {
    dispatch(
      fetchListings({
        status: 'AVAILABLE',
        size: 50,
        category: selectedCategory || undefined,
      }),
    );
  }, [dispatch, selectedCategory]);

  return (
    <div className="h-[calc(100vh-4rem)] flex relative">
      {/* Side Panel */}
      {showSidePanel && (
        <div className="w-96 flex-shrink-0 bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-800 flex flex-col overflow-hidden z-10">
          {/* Panel Header */}
          <div className="p-4 border-b border-gray-200 dark:border-gray-800">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-lg font-bold text-gray-900 dark:text-white">
                Nearby Food
              </h2>
              <Link to="/food" className="text-sm text-primary-600 hover:text-primary-500 flex items-center gap-1">
                <List className="h-4 w-4" />
                List View
              </Link>
            </div>

            {/* Category Filters */}
            <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
              {categories.map((cat) => (
                <button
                  key={cat.value}
                  onClick={() => setSelectedCategory(cat.value as FoodCategory | '')}
                  className={`px-3 py-1.5 rounded-full text-xs font-medium whitespace-nowrap transition-colors ${
                    selectedCategory === cat.value
                      ? 'bg-primary-500 text-white'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-gray-700'
                  }`}
                >
                  {cat.label}
                </button>
              ))}
            </div>
          </div>

          {/* Listings */}
          <div className="flex-1 overflow-y-auto">
            {loading ? (
              <div className="flex items-center justify-center py-12">
                <LoadingSpinner size="md" />
              </div>
            ) : listings.length === 0 ? (
              <div className="p-8 text-center">
                <MapPin className="h-10 w-10 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  No available food found nearby.
                </p>
              </div>
            ) : (
              <div className="divide-y divide-gray-100 dark:divide-gray-800">
                {listings.map((listing) => (
                  <Link
                    key={listing.id}
                    to={`/food/${listing.id}`}
                    className="flex gap-3 p-4 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors group"
                  >
                    <div className="h-16 w-16 rounded-lg bg-gray-100 dark:bg-gray-800 flex-shrink-0 overflow-hidden">
                      {listing.images[0] ? (
                        <img
                          src={listing.images[0]}
                          alt={listing.title}
                          className="h-full w-full object-cover"
                        />
                      ) : (
                        <div className="flex items-center justify-center h-full">
                          <MapPin className="h-6 w-6 text-gray-400" />
                        </div>
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <h3 className="text-sm font-semibold text-gray-900 dark:text-white truncate group-hover:text-primary-600 dark:group-hover:text-primary-400">
                        {listing.title}
                      </h3>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                        {listing.restaurant.name}
                      </p>
                      <div className="flex items-center gap-3 mt-1.5 text-xs text-gray-400 dark:text-gray-500">
                        <span className="flex items-center gap-1">
                          <Users className="h-3 w-3" />
                          {listing.quantity} {listing.unit}
                        </span>
                        <span className="flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          {formatDistanceToNow(new Date(listing.expiresAt), { addSuffix: false })}
                        </span>
                        {listing.distance !== undefined && (
                          <span className="flex items-center gap-1">
                            <MapPin className="h-3 w-3" />
                            {listing.distance.toFixed(1)} km
                          </span>
                        )}
                      </div>
                    </div>
                    <ChevronRight className="h-4 w-4 text-gray-300 dark:text-gray-600 self-center flex-shrink-0 group-hover:text-primary-500" />
                  </Link>
                ))}
              </div>
            )}
          </div>

          {/* Panel Footer */}
          <div className="p-3 border-t border-gray-200 dark:border-gray-800 text-center">
            <p className="text-xs text-gray-400">
              {listings.length} listings found
            </p>
          </div>
        </div>
      )}

      {/* Map */}
      <div className="flex-1 relative">
        {/* Toggle Side Panel */}
        <button
          onClick={() => setShowSidePanel(!showSidePanel)}
          className="absolute top-4 left-4 z-10 p-2 bg-white dark:bg-gray-900 rounded-lg shadow-md hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
        >
          <SlidersHorizontal className="h-5 w-5 text-gray-600 dark:text-gray-400" />
        </button>

        {hasGoogleMapsKey ? (
          <FoodMap
            listings={listings}
            className="h-full"
          />
        ) : (
          <FoodMapFallback listings={listings} className="h-full" />
        )}
      </div>
    </div>
  );
}

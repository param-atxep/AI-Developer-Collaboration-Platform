import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Search, SlidersHorizontal, PlusCircle, MapPin, X } from 'lucide-react';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchListings } from '@/store/foodSlice';
import { useAuth } from '@/hooks/useAuth';
import FoodListingCard from '@/components/food/FoodListingCard';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import type { FoodCategory, DietaryTag, FoodSearchParams } from '@/types';
import toast from 'react-hot-toast';
import foodService from '@/services/foodService';

const categoryFilters: { value: FoodCategory | ''; label: string }[] = [
  { value: '', label: 'All Categories' },
  { value: 'PREPARED_MEALS', label: 'Prepared Meals' },
  { value: 'FRESH_PRODUCE', label: 'Fresh Produce' },
  { value: 'BAKERY', label: 'Bakery' },
  { value: 'DAIRY', label: 'Dairy' },
  { value: 'CANNED_GOODS', label: 'Canned Goods' },
  { value: 'BEVERAGES', label: 'Beverages' },
  { value: 'SNACKS', label: 'Snacks' },
];

const dietaryFilters: { value: DietaryTag; label: string }[] = [
  { value: 'VEGAN', label: 'Vegan' },
  { value: 'VEGETARIAN', label: 'Vegetarian' },
  { value: 'HALAL', label: 'Halal' },
  { value: 'KOSHER', label: 'Kosher' },
  { value: 'GLUTEN_FREE', label: 'Gluten Free' },
];

const sortOptions = [
  { value: 'created', label: 'Newest First' },
  { value: 'expiry', label: 'Expiring Soon' },
  { value: 'distance', label: 'Nearest' },
  { value: 'quantity', label: 'Most Available' },
];

export default function FoodListingsPage() {
  const dispatch = useAppDispatch();
  const { user } = useAuth();
  const { listings, loading, totalPages, currentPage } = useAppSelector((state) => state.food);

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<FoodCategory | ''>('');
  const [selectedDietary, setSelectedDietary] = useState<DietaryTag[]>([]);
  const [sortBy, setSortBy] = useState('created');
  const [maxDistance, setMaxDistance] = useState(10);
  const [showFilters, setShowFilters] = useState(false);
  const [claimingId, setClaimingId] = useState<string | null>(null);

  const loadListings = useCallback(
    (page: number = 0) => {
      const params: FoodSearchParams = {
        page,
        size: 12,
        sortBy: sortBy as FoodSearchParams['sortBy'],
        sortOrder: sortBy === 'expiry' ? 'asc' : 'desc',
        status: 'AVAILABLE',
      };
      if (searchQuery) params.query = searchQuery;
      if (selectedCategory) params.category = selectedCategory;
      if (selectedDietary.length > 0) params.dietaryTags = selectedDietary;
      if (maxDistance) params.maxDistance = maxDistance;

      dispatch(fetchListings(params));
    },
    [dispatch, searchQuery, selectedCategory, selectedDietary, sortBy, maxDistance],
  );

  useEffect(() => {
    loadListings();
  }, [loadListings]);

  const handleClaim = async (id: string) => {
    if (!user) {
      toast.error('Please sign in to claim food');
      return;
    }
    setClaimingId(id);
    try {
      await foodService.claimListing(id);
      toast.success('Food claimed successfully! Check your pickups for details.');
      loadListings(currentPage);
    } catch {
      toast.error('Failed to claim food. It may have already been claimed.');
    } finally {
      setClaimingId(null);
    }
  };

  const toggleDietary = (tag: DietaryTag) => {
    setSelectedDietary((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag],
    );
  };

  const clearFilters = () => {
    setSearchQuery('');
    setSelectedCategory('');
    setSelectedDietary([]);
    setSortBy('created');
    setMaxDistance(10);
  };

  const hasActiveFilters = selectedCategory || selectedDietary.length > 0 || maxDistance !== 10;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Available Food
          </h1>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Browse surplus food from local restaurants and businesses
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/map" className="btn-outline text-sm px-4 py-2 gap-2">
            <MapPin className="h-4 w-4" />
            Map View
          </Link>
          {user?.role === 'RESTAURANT' && (
            <Link to="/food/new" className="btn-primary text-sm px-4 py-2 gap-2">
              <PlusCircle className="h-4 w-4" />
              New Listing
            </Link>
          )}
        </div>
      </div>

      {/* Search & Filters */}
      <div className="mb-6 space-y-4">
        <div className="flex gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search food listings..."
              className="input-field pl-11"
            />
          </div>
          <button
            onClick={() => setShowFilters(!showFilters)}
            className={`btn-outline px-4 gap-2 ${showFilters ? 'bg-gray-100 dark:bg-gray-800' : ''}`}
          >
            <SlidersHorizontal className="h-4 w-4" />
            <span className="hidden sm:inline">Filters</span>
            {hasActiveFilters && (
              <span className="flex h-5 w-5 items-center justify-center rounded-full bg-primary-500 text-[10px] font-bold text-white">
                {(selectedCategory ? 1 : 0) + selectedDietary.length + (maxDistance !== 10 ? 1 : 0)}
              </span>
            )}
          </button>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="input-field w-auto"
          >
            {sortOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        {/* Filter Panel */}
        {showFilters && (
          <div className="card p-5 animate-slide-down">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-semibold text-gray-900 dark:text-white">Filters</h3>
              {hasActiveFilters && (
                <button onClick={clearFilters} className="text-xs text-primary-600 hover:text-primary-500 flex items-center gap-1">
                  <X className="h-3 w-3" />
                  Clear all
                </button>
              )}
            </div>

            <div className="space-y-4">
              {/* Category */}
              <div>
                <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-2 uppercase tracking-wider">
                  Category
                </label>
                <div className="flex flex-wrap gap-2">
                  {categoryFilters.map((cat) => (
                    <button
                      key={cat.value}
                      onClick={() => setSelectedCategory(cat.value as FoodCategory | '')}
                      className={`px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors ${
                        selectedCategory === cat.value
                          ? 'bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900/30 dark:border-primary-500 dark:text-primary-400'
                          : 'border-gray-200 text-gray-600 hover:border-gray-300 dark:border-gray-700 dark:text-gray-400'
                      }`}
                    >
                      {cat.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Dietary */}
              <div>
                <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-2 uppercase tracking-wider">
                  Dietary Preferences
                </label>
                <div className="flex flex-wrap gap-2">
                  {dietaryFilters.map((d) => (
                    <button
                      key={d.value}
                      onClick={() => toggleDietary(d.value)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors ${
                        selectedDietary.includes(d.value)
                          ? 'bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900/30 dark:border-primary-500 dark:text-primary-400'
                          : 'border-gray-200 text-gray-600 hover:border-gray-300 dark:border-gray-700 dark:text-gray-400'
                      }`}
                    >
                      {d.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Distance */}
              <div>
                <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-2 uppercase tracking-wider">
                  Max Distance: {maxDistance} km
                </label>
                <input
                  type="range"
                  min="1"
                  max="50"
                  value={maxDistance}
                  onChange={(e) => setMaxDistance(Number(e.target.value))}
                  className="w-full h-2 rounded-lg appearance-none cursor-pointer bg-gray-200 dark:bg-gray-700 accent-primary-500"
                />
                <div className="flex justify-between text-xs text-gray-400 mt-1">
                  <span>1 km</span>
                  <span>50 km</span>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Listings Grid */}
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <LoadingSpinner size="lg" label="Loading listings..." />
        </div>
      ) : listings.length === 0 ? (
        <div className="text-center py-20">
          <div className="inline-flex h-20 w-20 items-center justify-center rounded-full bg-gray-100 dark:bg-gray-800 mb-4">
            <Search className="h-10 w-10 text-gray-400" />
          </div>
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            No food listings found
          </h3>
          <p className="mt-2 text-sm text-gray-500 dark:text-gray-400 max-w-md mx-auto">
            Try adjusting your filters or search query. New listings are added regularly.
          </p>
          {hasActiveFilters && (
            <button onClick={clearFilters} className="btn-secondary mt-4">
              Clear Filters
            </button>
          )}
        </div>
      ) : (
        <>
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {listings.map((listing) => (
              <FoodListingCard
                key={listing.id}
                listing={listing}
                onClaim={handleClaim}
                claimLoading={claimingId === listing.id}
              />
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="mt-8 flex items-center justify-center gap-2">
              {Array.from({ length: totalPages }, (_, i) => (
                <button
                  key={i}
                  onClick={() => loadListings(i)}
                  className={`h-10 w-10 rounded-lg text-sm font-medium transition-colors ${
                    currentPage === i
                      ? 'bg-primary-500 text-white'
                      : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
                  }`}
                >
                  {i + 1}
                </button>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}

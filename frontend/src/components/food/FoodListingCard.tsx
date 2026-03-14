import { Link } from 'react-router-dom';
import { Clock, MapPin, Users, Leaf, AlertTriangle } from 'lucide-react';
import { formatDistanceToNow, isPast } from 'date-fns';
import type { FoodListing, FoodCategory, DietaryTag } from '@/types';

interface FoodListingCardProps {
  listing: FoodListing;
  onClaim?: (id: string) => void;
  claimLoading?: boolean;
}

const categoryColors: Record<FoodCategory, string> = {
  PREPARED_MEALS: 'badge-orange',
  FRESH_PRODUCE: 'badge-green',
  BAKERY: 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400',
  DAIRY: 'badge-blue',
  CANNED_GOODS: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300',
  BEVERAGES: 'badge-purple',
  SNACKS: 'bg-pink-100 text-pink-800 dark:bg-pink-900/30 dark:text-pink-400',
  OTHER: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300',
};

const categoryLabels: Record<FoodCategory, string> = {
  PREPARED_MEALS: 'Prepared Meals',
  FRESH_PRODUCE: 'Fresh Produce',
  BAKERY: 'Bakery',
  DAIRY: 'Dairy',
  CANNED_GOODS: 'Canned Goods',
  BEVERAGES: 'Beverages',
  SNACKS: 'Snacks',
  OTHER: 'Other',
};

const dietaryColors: Record<DietaryTag, string> = {
  VEGAN: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
  VEGETARIAN: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
  HALAL: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
  KOSHER: 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-400',
  GLUTEN_FREE: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400',
  NUT_FREE: 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400',
  DAIRY_FREE: 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
};

export default function FoodListingCard({ listing, onClaim, claimLoading }: FoodListingCardProps) {
  const isExpired = isPast(new Date(listing.expiresAt));
  const isAvailable = listing.status === 'AVAILABLE' && !isExpired;

  return (
    <div className="card overflow-hidden group">
      {/* Image */}
      <div className="relative h-48 bg-gray-100 dark:bg-gray-800 overflow-hidden">
        {listing.images[0] ? (
          <img
            src={listing.images[0]}
            alt={listing.title}
            className="h-full w-full object-cover group-hover:scale-105 transition-transform duration-300"
          />
        ) : (
          <div className="flex items-center justify-center h-full">
            <Leaf className="h-12 w-12 text-gray-300 dark:text-gray-600" />
          </div>
        )}

        {/* Category Badge */}
        <div className="absolute top-3 left-3">
          <span className={`badge text-xs ${categoryColors[listing.category]}`}>
            {categoryLabels[listing.category]}
          </span>
        </div>

        {/* Status Overlay */}
        {!isAvailable && (
          <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
            <span className="px-3 py-1.5 rounded-full bg-white/90 text-sm font-semibold text-gray-900">
              {isExpired ? 'Expired' : listing.status}
            </span>
          </div>
        )}

        {/* Expiry Warning */}
        {isAvailable && (
          <div className="absolute top-3 right-3">
            <div className="flex items-center gap-1 px-2 py-1 rounded-full bg-black/50 text-white text-xs backdrop-blur-sm">
              <Clock className="h-3 w-3" />
              {formatDistanceToNow(new Date(listing.expiresAt), { addSuffix: false })} left
            </div>
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-4">
        <Link to={`/food/${listing.id}`}>
          <h3 className="text-base font-semibold text-gray-900 dark:text-white hover:text-primary-600 dark:hover:text-primary-400 transition-colors line-clamp-1">
            {listing.title}
          </h3>
        </Link>

        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400 line-clamp-2">
          {listing.description}
        </p>

        {/* Meta info */}
        <div className="mt-3 flex items-center gap-4 text-xs text-gray-500 dark:text-gray-400">
          <div className="flex items-center gap-1">
            <Users className="h-3.5 w-3.5" />
            <span>
              {listing.quantity} {listing.unit}
            </span>
          </div>
          {listing.distance !== undefined && (
            <div className="flex items-center gap-1">
              <MapPin className="h-3.5 w-3.5" />
              <span>{listing.distance.toFixed(1)} km</span>
            </div>
          )}
          {listing.allergens.length > 0 && (
            <div className="flex items-center gap-1 text-amber-600 dark:text-amber-400">
              <AlertTriangle className="h-3.5 w-3.5" />
              <span>{listing.allergens.length} allergens</span>
            </div>
          )}
        </div>

        {/* Dietary Tags */}
        {listing.dietaryTags.length > 0 && (
          <div className="mt-3 flex flex-wrap gap-1.5">
            {listing.dietaryTags.map((tag) => (
              <span
                key={tag}
                className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-medium ${dietaryColors[tag]}`}
              >
                {tag.replace('_', ' ')}
              </span>
            ))}
          </div>
        )}

        {/* Restaurant + Claim */}
        <div className="mt-4 flex items-center justify-between pt-3 border-t border-gray-100 dark:border-gray-800">
          <div className="flex items-center gap-2 min-w-0">
            <div className="h-7 w-7 rounded-full bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center text-primary-700 dark:text-primary-400 text-xs font-semibold flex-shrink-0">
              {listing.restaurant.avatar ? (
                <img
                  src={listing.restaurant.avatar}
                  alt={listing.restaurant.name}
                  className="h-7 w-7 rounded-full object-cover"
                />
              ) : (
                listing.restaurant.name.charAt(0)
              )}
            </div>
            <span className="text-sm text-gray-600 dark:text-gray-400 truncate">
              {listing.restaurant.name}
            </span>
          </div>

          {isAvailable && onClaim && (
            <button
              onClick={(e) => {
                e.preventDefault();
                onClaim(listing.id);
              }}
              disabled={claimLoading}
              className="btn-primary text-xs px-3 py-1.5 flex-shrink-0"
            >
              {claimLoading ? 'Claiming...' : 'Claim'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

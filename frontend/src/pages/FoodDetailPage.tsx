import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Clock,
  MapPin,
  Users,
  Calendar,
  AlertTriangle,
  Leaf,
  Star,
  Share2,
  Heart,
} from 'lucide-react';
import { formatDistanceToNow, format, isPast } from 'date-fns';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchListingById, clearSelectedListing } from '@/store/foodSlice';
import { useAuth } from '@/hooks/useAuth';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import foodService from '@/services/foodService';
import toast from 'react-hot-toast';

export default function FoodDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { user } = useAuth();
  const { selectedListing: listing, loading } = useAppSelector((state) => state.food);
  const [claimLoading, setClaimLoading] = useState(false);
  const [selectedImage, setSelectedImage] = useState(0);

  useEffect(() => {
    if (id) {
      dispatch(fetchListingById(id));
    }
    return () => {
      dispatch(clearSelectedListing());
    };
  }, [id, dispatch]);

  const handleClaim = async () => {
    if (!user) {
      navigate('/login', { state: { from: { pathname: `/food/${id}` } } });
      return;
    }
    if (!listing) return;

    setClaimLoading(true);
    try {
      await foodService.claimListing(listing.id);
      toast.success('Food claimed successfully!');
      dispatch(fetchListingById(listing.id));
    } catch {
      toast.error('Failed to claim food. It may have already been claimed.');
    } finally {
      setClaimLoading(false);
    }
  };

  if (loading || !listing) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <LoadingSpinner size="lg" label="Loading food details..." />
      </div>
    );
  }

  const isExpired = isPast(new Date(listing.expiresAt));
  const isAvailable = listing.status === 'AVAILABLE' && !isExpired;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Back button */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white mb-6 transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to listings
      </button>

      <div className="grid lg:grid-cols-5 gap-8">
        {/* Left: Images */}
        <div className="lg:col-span-3">
          <div className="aspect-[4/3] rounded-2xl overflow-hidden bg-gray-100 dark:bg-gray-800 mb-4">
            {listing.images[selectedImage] ? (
              <img
                src={listing.images[selectedImage]}
                alt={listing.title}
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="flex items-center justify-center h-full">
                <Leaf className="h-20 w-20 text-gray-300 dark:text-gray-600" />
              </div>
            )}
          </div>

          {listing.images.length > 1 && (
            <div className="flex gap-3">
              {listing.images.map((img, i) => (
                <button
                  key={i}
                  onClick={() => setSelectedImage(i)}
                  className={`h-20 w-20 rounded-xl overflow-hidden flex-shrink-0 border-2 transition-colors ${
                    selectedImage === i
                      ? 'border-primary-500'
                      : 'border-transparent hover:border-gray-300 dark:hover:border-gray-600'
                  }`}
                >
                  <img src={img} alt={`${listing.title} ${i + 1}`} className="h-full w-full object-cover" />
                </button>
              ))}
            </div>
          )}

          {/* Description */}
          <div className="mt-8">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">
              Description
            </h2>
            <p className="text-gray-600 dark:text-gray-400 leading-relaxed whitespace-pre-line">
              {listing.description || 'No description provided.'}
            </p>
          </div>

          {/* Pickup Instructions */}
          {listing.pickupInstructions && (
            <div className="mt-6">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">
                Pickup Instructions
              </h2>
              <div className="p-4 rounded-xl bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800">
                <p className="text-sm text-blue-800 dark:text-blue-300 leading-relaxed">
                  {listing.pickupInstructions}
                </p>
              </div>
            </div>
          )}

          {/* Allergens */}
          {listing.allergens.length > 0 && (
            <div className="mt-6">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
                <AlertTriangle className="h-5 w-5 text-amber-500" />
                Allergen Information
              </h2>
              <div className="flex flex-wrap gap-2">
                {listing.allergens.map((allergen) => (
                  <span
                    key={allergen}
                    className="px-3 py-1.5 rounded-lg bg-amber-50 text-amber-800 text-sm font-medium border border-amber-200 dark:bg-amber-900/20 dark:text-amber-300 dark:border-amber-800"
                  >
                    {allergen.replace('_', ' ')}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Map */}
          <div className="mt-8">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">
              Pickup Location
            </h2>
            <div className="h-64 rounded-xl bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 flex items-center justify-center">
              <div className="text-center">
                <MapPin className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  {listing.location.address || 'Map view'}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Right: Details Card */}
        <div className="lg:col-span-2">
          <div className="card p-6 sticky top-24">
            {/* Status */}
            <div className="flex items-center justify-between mb-4">
              <span
                className={`badge text-sm ${
                  isAvailable
                    ? 'badge-green'
                    : isExpired
                      ? 'badge-red'
                      : 'badge-blue'
                }`}
              >
                {isExpired ? 'Expired' : listing.status.replace('_', ' ')}
              </span>
              <div className="flex items-center gap-2">
                <button className="p-2 rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors">
                  <Heart className="h-5 w-5" />
                </button>
                <button className="p-2 rounded-lg text-gray-400 hover:text-primary-600 hover:bg-primary-50 dark:hover:bg-primary-900/20 transition-colors">
                  <Share2 className="h-5 w-5" />
                </button>
              </div>
            </div>

            <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
              {listing.title}
            </h1>

            {/* Category & Dietary */}
            <div className="flex flex-wrap gap-2 mb-4">
              <span className="badge badge-green">{listing.category.replace('_', ' ')}</span>
              {listing.dietaryTags.map((tag) => (
                <span key={tag} className="badge badge-blue">
                  {tag.replace('_', ' ')}
                </span>
              ))}
            </div>

            {/* Details */}
            <div className="space-y-3 mb-6">
              <div className="flex items-center gap-3 text-sm">
                <Users className="h-5 w-5 text-gray-400 flex-shrink-0" />
                <span className="text-gray-600 dark:text-gray-400">
                  <span className="font-semibold text-gray-900 dark:text-white">
                    {listing.quantity} {listing.unit}
                  </span>{' '}
                  available
                </span>
              </div>

              <div className="flex items-center gap-3 text-sm">
                <Clock className="h-5 w-5 text-gray-400 flex-shrink-0" />
                <span className="text-gray-600 dark:text-gray-400">
                  Expires{' '}
                  <span className={`font-semibold ${isExpired ? 'text-red-500' : 'text-gray-900 dark:text-white'}`}>
                    {formatDistanceToNow(new Date(listing.expiresAt), { addSuffix: true })}
                  </span>
                </span>
              </div>

              <div className="flex items-center gap-3 text-sm">
                <Calendar className="h-5 w-5 text-gray-400 flex-shrink-0" />
                <span className="text-gray-600 dark:text-gray-400">
                  Pickup: {format(new Date(listing.pickupStart), 'MMM d, h:mm a')} -{' '}
                  {format(new Date(listing.pickupEnd), 'h:mm a')}
                </span>
              </div>

              {listing.distance !== undefined && (
                <div className="flex items-center gap-3 text-sm">
                  <MapPin className="h-5 w-5 text-gray-400 flex-shrink-0" />
                  <span className="text-gray-600 dark:text-gray-400">
                    <span className="font-semibold text-gray-900 dark:text-white">
                      {listing.distance.toFixed(1)} km
                    </span>{' '}
                    away
                  </span>
                </div>
              )}
            </div>

            {/* Claim Button */}
            {isAvailable && (
              <button
                onClick={handleClaim}
                disabled={claimLoading}
                className="btn-primary w-full py-3.5 text-base mb-4"
              >
                {claimLoading ? <LoadingSpinner size="sm" /> : 'Claim This Food'}
              </button>
            )}

            {listing.status === 'CLAIMED' && listing.claimedBy?.id === user?.id && (
              <Link to="/pickups" className="btn-secondary w-full py-3.5 text-base mb-4 text-center">
                View Pickup Details
              </Link>
            )}

            {/* Restaurant Info */}
            <div className="pt-4 border-t border-gray-100 dark:border-gray-800">
              <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-3">
                Listed By
              </h3>
              <div className="flex items-center gap-3">
                <div className="h-12 w-12 rounded-full bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center text-primary-700 dark:text-primary-400 font-bold text-lg flex-shrink-0">
                  {listing.restaurant.avatar ? (
                    <img
                      src={listing.restaurant.avatar}
                      alt={listing.restaurant.name}
                      className="h-12 w-12 rounded-full object-cover"
                    />
                  ) : (
                    listing.restaurant.name.charAt(0)
                  )}
                </div>
                <div>
                  <p className="font-semibold text-gray-900 dark:text-white">
                    {listing.restaurant.name}
                  </p>
                  {listing.restaurant.rating && (
                    <div className="flex items-center gap-1 text-sm text-gray-500 dark:text-gray-400">
                      <Star className="h-4 w-4 fill-amber-400 text-amber-400" />
                      <span>{listing.restaurant.rating.toFixed(1)}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Posted time */}
            <p className="mt-4 text-xs text-gray-400 dark:text-gray-500 text-center">
              Listed {formatDistanceToNow(new Date(listing.createdAt), { addSuffix: true })}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

import { useState, useCallback, useRef, useEffect } from 'react';
import { GoogleMap, useJsApiLoader, MarkerF, InfoWindowF } from '@react-google-maps/api';
import { Link } from 'react-router-dom';
import { Clock, MapPin, Leaf } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import type { FoodListing, FoodCategory } from '@/types';
import LoadingSpinner from '@/components/common/LoadingSpinner';

interface FoodMapProps {
  listings: FoodListing[];
  center?: { lat: number; lng: number };
  zoom?: number;
  className?: string;
  onBoundsChanged?: (bounds: google.maps.LatLngBounds) => void;
}

const categoryMarkerColors: Record<FoodCategory, string> = {
  PREPARED_MEALS: '#f59e0b',
  FRESH_PRODUCE: '#22c55e',
  BAKERY: '#d97706',
  DAIRY: '#3b82f6',
  CANNED_GOODS: '#6b7280',
  BEVERAGES: '#8b5cf6',
  SNACKS: '#ec4899',
  OTHER: '#9ca3af',
};

const mapContainerStyle = {
  width: '100%',
  height: '100%',
};

const defaultCenter = { lat: 40.7128, lng: -74.006 };

const mapOptions: google.maps.MapOptions = {
  disableDefaultUI: false,
  zoomControl: true,
  mapTypeControl: false,
  streetViewControl: false,
  fullscreenControl: true,
  styles: [
    {
      featureType: 'poi',
      elementType: 'labels',
      stylers: [{ visibility: 'off' }],
    },
  ],
};

export default function FoodMap({
  listings,
  center,
  zoom = 13,
  className = '',
  onBoundsChanged,
}: FoodMapProps) {
  const { isLoaded, loadError } = useJsApiLoader({
    googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '',
  });

  const [selectedListing, setSelectedListing] = useState<FoodListing | null>(null);
  const [userLocation, setUserLocation] = useState<{ lat: number; lng: number } | null>(null);
  const mapRef = useRef<google.maps.Map | null>(null);

  useEffect(() => {
    navigator.geolocation?.getCurrentPosition(
      (position) => {
        setUserLocation({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        });
      },
      () => {
        // Silently fail
      },
    );
  }, []);

  const onLoad = useCallback((map: google.maps.Map) => {
    mapRef.current = map;
  }, []);

  const handleBoundsChanged = useCallback(() => {
    if (mapRef.current && onBoundsChanged) {
      const bounds = mapRef.current.getBounds();
      if (bounds) {
        onBoundsChanged(bounds);
      }
    }
  }, [onBoundsChanged]);

  const createMarkerIcon = (category: FoodCategory) => {
    const color = categoryMarkerColors[category];
    return {
      path: 'M12 0C7.03 0 3 4.03 3 9c0 7.13 9 15 9 15s9-7.87 9-15c0-4.97-4.03-9-9-9z',
      fillColor: color,
      fillOpacity: 1,
      strokeColor: '#ffffff',
      strokeWeight: 2,
      scale: 1.5,
      anchor: new google.maps.Point(12, 24),
    };
  };

  if (loadError) {
    return (
      <div className={`flex items-center justify-center bg-gray-100 dark:bg-gray-800 rounded-xl ${className}`}>
        <div className="text-center p-8">
          <MapPin className="h-12 w-12 text-gray-400 mx-auto mb-3" />
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Unable to load Google Maps. Please check your API key configuration.
          </p>
        </div>
      </div>
    );
  }

  if (!isLoaded) {
    return (
      <div className={`flex items-center justify-center bg-gray-100 dark:bg-gray-800 rounded-xl ${className}`}>
        <LoadingSpinner size="lg" label="Loading map..." />
      </div>
    );
  }

  return (
    <div className={`rounded-xl overflow-hidden ${className}`}>
      <GoogleMap
        mapContainerStyle={mapContainerStyle}
        center={center || userLocation || defaultCenter}
        zoom={zoom}
        options={mapOptions}
        onLoad={onLoad}
        onBoundsChanged={handleBoundsChanged}
      >
        {/* User location marker */}
        {userLocation && (
          <MarkerF
            position={userLocation}
            icon={{
              path: google.maps.SymbolPath.CIRCLE,
              fillColor: '#3b82f6',
              fillOpacity: 1,
              strokeColor: '#ffffff',
              strokeWeight: 3,
              scale: 8,
            }}
            title="Your Location"
          />
        )}

        {/* Food listing markers */}
        {listings.map((listing) => (
          <MarkerF
            key={listing.id}
            position={{
              lat: listing.location.latitude,
              lng: listing.location.longitude,
            }}
            icon={createMarkerIcon(listing.category)}
            onClick={() => setSelectedListing(listing)}
            title={listing.title}
          />
        ))}

        {/* Info window */}
        {selectedListing && (
          <InfoWindowF
            position={{
              lat: selectedListing.location.latitude,
              lng: selectedListing.location.longitude,
            }}
            onCloseClick={() => setSelectedListing(null)}
          >
            <div className="max-w-xs p-1">
              {selectedListing.images[0] && (
                <img
                  src={selectedListing.images[0]}
                  alt={selectedListing.title}
                  className="h-32 w-full object-cover rounded-lg mb-2"
                />
              )}
              <Link
                to={`/food/${selectedListing.id}`}
                className="text-sm font-semibold text-gray-900 hover:text-primary-600"
              >
                {selectedListing.title}
              </Link>
              <p className="text-xs text-gray-500 mt-1">
                {selectedListing.quantity} {selectedListing.unit} &middot;{' '}
                {selectedListing.restaurant.name}
              </p>
              <div className="flex items-center gap-3 mt-2 text-xs text-gray-400">
                <span className="flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  {formatDistanceToNow(new Date(selectedListing.expiresAt), { addSuffix: false })} left
                </span>
                {selectedListing.distance !== undefined && (
                  <span className="flex items-center gap-1">
                    <MapPin className="h-3 w-3" />
                    {selectedListing.distance.toFixed(1)} km
                  </span>
                )}
              </div>
              <Link
                to={`/food/${selectedListing.id}`}
                className="block mt-2 text-center text-xs font-medium text-primary-600 hover:text-primary-500"
              >
                View Details
              </Link>
            </div>
          </InfoWindowF>
        )}
      </GoogleMap>

      {/* Legend */}
      <div className="absolute bottom-4 left-4 bg-white dark:bg-gray-900 rounded-xl shadow-lg p-3 text-xs">
        <p className="font-semibold text-gray-900 dark:text-white mb-2">Categories</p>
        <div className="grid grid-cols-2 gap-x-4 gap-y-1">
          {Object.entries(categoryMarkerColors).slice(0, 6).map(([cat, color]) => (
            <div key={cat} className="flex items-center gap-1.5">
              <div className="h-3 w-3 rounded-full" style={{ backgroundColor: color }} />
              <span className="text-gray-600 dark:text-gray-400">
                {cat.replace('_', ' ').replace(/\b\w/g, (c) => c.toUpperCase()).substring(0, 12)}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// Fallback component when Google Maps is not configured
export function FoodMapFallback({ listings, className = '' }: { listings: FoodListing[]; className?: string }) {
  return (
    <div className={`bg-gray-100 dark:bg-gray-800 rounded-xl flex items-center justify-center ${className}`}>
      <div className="text-center p-8">
        <Leaf className="h-16 w-16 text-gray-300 dark:text-gray-600 mx-auto mb-4" />
        <h3 className="text-lg font-semibold text-gray-600 dark:text-gray-400 mb-2">
          Map View
        </h3>
        <p className="text-sm text-gray-500 dark:text-gray-500 mb-4 max-w-md">
          Configure your Google Maps API key to see food listings on an interactive map.
          {listings.length > 0 && ` (${listings.length} listings available)`}
        </p>
        <p className="text-xs text-gray-400">
          Set VITE_GOOGLE_MAPS_API_KEY in your environment variables.
        </p>
      </div>
    </div>
  );
}

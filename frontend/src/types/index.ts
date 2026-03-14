// ──────────────────────────────────────
// User & Auth
// ──────────────────────────────────────
export type UserRole = 'RESTAURANT' | 'NGO' | 'CITIZEN' | 'ADMIN';

export interface GeoLocation {
  latitude: number;
  longitude: number;
  address?: string;
  city?: string;
  state?: string;
  zipCode?: string;
}

export interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  phone?: string;
  avatar?: string;
  organization?: string;
  location?: GeoLocation;
  operatingHours?: OperatingHours[];
  cuisineType?: string;
  bio?: string;
  verified: boolean;
  badges: Badge[];
  createdAt: string;
  updatedAt: string;
}

export interface Badge {
  id: string;
  name: string;
  description: string;
  icon: string;
  earnedAt: string;
}

export interface OperatingHours {
  day: string;
  openTime: string;
  closeTime: string;
  closed: boolean;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  role: UserRole;
  phone?: string;
  organization?: string;
  location?: GeoLocation;
}

export interface AuthResponse {
  user: User;
  accessToken: string;
  refreshToken: string;
}

// ──────────────────────────────────────
// Food Listings
// ──────────────────────────────────────
export type FoodCategory =
  | 'PREPARED_MEALS'
  | 'FRESH_PRODUCE'
  | 'BAKERY'
  | 'DAIRY'
  | 'CANNED_GOODS'
  | 'BEVERAGES'
  | 'SNACKS'
  | 'OTHER';

export type FoodStatus = 'AVAILABLE' | 'CLAIMED' | 'PICKED_UP' | 'EXPIRED' | 'CANCELLED';

export type DietaryTag = 'VEGAN' | 'VEGETARIAN' | 'HALAL' | 'KOSHER' | 'GLUTEN_FREE' | 'NUT_FREE' | 'DAIRY_FREE';

export type Allergen =
  | 'PEANUTS'
  | 'TREE_NUTS'
  | 'MILK'
  | 'EGGS'
  | 'WHEAT'
  | 'SOY'
  | 'FISH'
  | 'SHELLFISH'
  | 'SESAME';

export interface FoodListing {
  id: string;
  title: string;
  description: string;
  category: FoodCategory;
  quantity: number;
  unit: string;
  images: string[];
  status: FoodStatus;
  expiresAt: string;
  pickupStart: string;
  pickupEnd: string;
  pickupInstructions?: string;
  allergens: Allergen[];
  dietaryTags: DietaryTag[];
  location: GeoLocation;
  distance?: number;
  restaurant: {
    id: string;
    name: string;
    avatar?: string;
    rating?: number;
  };
  claimedBy?: {
    id: string;
    name: string;
  };
  createdAt: string;
  updatedAt: string;
}

export interface CreateFoodListingRequest {
  title: string;
  description: string;
  category: FoodCategory;
  quantity: number;
  unit: string;
  images: File[];
  expiresAt: string;
  pickupStart: string;
  pickupEnd: string;
  pickupInstructions?: string;
  allergens: Allergen[];
  dietaryTags: DietaryTag[];
  location?: GeoLocation;
}

export interface FoodSearchParams {
  query?: string;
  category?: FoodCategory;
  dietaryTags?: DietaryTag[];
  maxDistance?: number;
  latitude?: number;
  longitude?: number;
  status?: FoodStatus;
  sortBy?: 'distance' | 'expiry' | 'created' | 'quantity';
  sortOrder?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

export interface FoodListingState {
  listings: FoodListing[];
  selectedListing: FoodListing | null;
  loading: boolean;
  error: string | null;
  totalPages: number;
  currentPage: number;
  totalElements: number;
}

// ──────────────────────────────────────
// Pickups
// ──────────────────────────────────────
export type PickupStatus =
  | 'SCHEDULED'
  | 'VOLUNTEER_ASSIGNED'
  | 'EN_ROUTE'
  | 'ARRIVED'
  | 'PICKED_UP'
  | 'DELIVERED'
  | 'COMPLETED'
  | 'CANCELLED';

export interface Pickup {
  id: string;
  foodListing: FoodListing;
  claimedBy: User;
  volunteer?: User;
  status: PickupStatus;
  scheduledAt: string;
  pickedUpAt?: string;
  deliveredAt?: string;
  completedAt?: string;
  cancelledAt?: string;
  cancelReason?: string;
  qrCode?: string;
  notes?: string;
  rating?: number;
  feedback?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SchedulePickupRequest {
  foodListingId: string;
  scheduledAt: string;
  notes?: string;
}

// ──────────────────────────────────────
// Notifications
// ──────────────────────────────────────
export type NotificationType =
  | 'NEW_FOOD_NEARBY'
  | 'FOOD_CLAIMED'
  | 'PICKUP_SCHEDULED'
  | 'PICKUP_REMINDER'
  | 'PICKUP_COMPLETED'
  | 'FOOD_EXPIRING'
  | 'BADGE_EARNED'
  | 'SYSTEM';

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  read: boolean;
  actionUrl?: string;
  data?: Record<string, unknown>;
  createdAt: string;
}

export interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  loading: boolean;
}

// ──────────────────────────────────────
// Analytics
// ──────────────────────────────────────
export interface DashboardStats {
  totalFoodSaved: number;
  totalMealsProvided: number;
  totalCO2Saved: number;
  totalMoneySaved: number;
  activeListings: number;
  completedPickups: number;
  activeUsers: number;
  monthlyGrowth: number;
}

export interface TimeSeriesDataPoint {
  date: string;
  value: number;
  label?: string;
}

export interface CategoryBreakdown {
  category: string;
  count: number;
  percentage: number;
  color: string;
}

export interface LeaderboardEntry {
  rank: number;
  userId: string;
  name: string;
  avatar?: string;
  organization?: string;
  foodSaved: number;
  mealsProvided: number;
  score: number;
}

export interface AnalyticsDashboard {
  stats: DashboardStats;
  foodSavedTimeSeries: TimeSeriesDataPoint[];
  categoryBreakdown: CategoryBreakdown[];
  leaderboard: LeaderboardEntry[];
}

// ──────────────────────────────────────
// API
// ──────────────────────────────────────
export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

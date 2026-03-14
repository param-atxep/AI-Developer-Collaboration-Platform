import api from './api';
import type {
  FoodListing,
  CreateFoodListingRequest,
  FoodSearchParams,
  PaginatedResponse,
} from '@/types';

const FOOD_PREFIX = '/food-listings';

export const foodService = {
  async getListings(params?: FoodSearchParams): Promise<PaginatedResponse<FoodListing>> {
    const response = await api.get<PaginatedResponse<FoodListing>>(FOOD_PREFIX, { params });
    return response.data;
  },

  async getListingById(id: string): Promise<FoodListing> {
    const response = await api.get<FoodListing>(`${FOOD_PREFIX}/${id}`);
    return response.data;
  },

  async createListing(data: CreateFoodListingRequest): Promise<FoodListing> {
    const formData = new FormData();
    formData.append('title', data.title);
    formData.append('description', data.description);
    formData.append('category', data.category);
    formData.append('quantity', data.quantity.toString());
    formData.append('unit', data.unit);
    formData.append('expiresAt', data.expiresAt);
    formData.append('pickupStart', data.pickupStart);
    formData.append('pickupEnd', data.pickupEnd);
    if (data.pickupInstructions) {
      formData.append('pickupInstructions', data.pickupInstructions);
    }
    data.allergens.forEach((a) => formData.append('allergens', a));
    data.dietaryTags.forEach((t) => formData.append('dietaryTags', t));
    data.images.forEach((img) => formData.append('images', img));
    if (data.location) {
      formData.append('location', JSON.stringify(data.location));
    }

    const response = await api.post<FoodListing>(FOOD_PREFIX, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  async updateListing(id: string, data: Partial<CreateFoodListingRequest>): Promise<FoodListing> {
    const response = await api.put<FoodListing>(`${FOOD_PREFIX}/${id}`, data);
    return response.data;
  },

  async deleteListing(id: string): Promise<void> {
    await api.delete(`${FOOD_PREFIX}/${id}`);
  },

  async claimListing(id: string): Promise<FoodListing> {
    const response = await api.post<FoodListing>(`${FOOD_PREFIX}/${id}/claim`);
    return response.data;
  },

  async searchNearby(
    latitude: number,
    longitude: number,
    radiusKm: number = 10,
    params?: Omit<FoodSearchParams, 'latitude' | 'longitude'>,
  ): Promise<PaginatedResponse<FoodListing>> {
    const response = await api.get<PaginatedResponse<FoodListing>>(`${FOOD_PREFIX}/nearby`, {
      params: { latitude, longitude, radius: radiusKm, ...params },
    });
    return response.data;
  },

  async getMyListings(params?: FoodSearchParams): Promise<PaginatedResponse<FoodListing>> {
    const response = await api.get<PaginatedResponse<FoodListing>>(`${FOOD_PREFIX}/mine`, {
      params,
    });
    return response.data;
  },
};

export default foodService;

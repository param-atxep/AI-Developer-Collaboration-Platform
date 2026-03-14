import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import type { FoodListing, FoodListingState, FoodSearchParams } from '@/types';
import foodService from '@/services/foodService';

const initialState: FoodListingState = {
  listings: [],
  selectedListing: null,
  loading: false,
  error: null,
  totalPages: 0,
  currentPage: 0,
  totalElements: 0,
};

export const fetchListings = createAsyncThunk(
  'food/fetchListings',
  async (params: FoodSearchParams | undefined, { rejectWithValue }) => {
    try {
      return await foodService.getListings(params);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch listings');
    }
  },
);

export const fetchListingById = createAsyncThunk(
  'food/fetchListingById',
  async (id: string, { rejectWithValue }) => {
    try {
      return await foodService.getListingById(id);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch listing');
    }
  },
);

export const fetchNearbyListings = createAsyncThunk(
  'food/fetchNearby',
  async (
    params: { latitude: number; longitude: number; radius?: number },
    { rejectWithValue },
  ) => {
    try {
      return await foodService.searchNearby(params.latitude, params.longitude, params.radius);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch nearby listings');
    }
  },
);

export const claimListing = createAsyncThunk(
  'food/claimListing',
  async (id: string, { rejectWithValue }) => {
    try {
      return await foodService.claimListing(id);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      return rejectWithValue(error.response?.data?.message || 'Failed to claim listing');
    }
  },
);

const foodSlice = createSlice({
  name: 'food',
  initialState,
  reducers: {
    clearSelectedListing(state) {
      state.selectedListing = null;
    },
    clearError(state) {
      state.error = null;
    },
    updateListingInStore(state, action: PayloadAction<FoodListing>) {
      const index = state.listings.findIndex((l) => l.id === action.payload.id);
      if (index !== -1) {
        state.listings[index] = action.payload;
      }
      if (state.selectedListing?.id === action.payload.id) {
        state.selectedListing = action.payload;
      }
    },
    removeListingFromStore(state, action: PayloadAction<string>) {
      state.listings = state.listings.filter((l) => l.id !== action.payload);
    },
  },
  extraReducers: (builder) => {
    // Fetch Listings
    builder
      .addCase(fetchListings.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchListings.fulfilled, (state, action) => {
        state.loading = false;
        state.listings = action.payload.content;
        state.totalPages = action.payload.totalPages;
        state.currentPage = action.payload.number;
        state.totalElements = action.payload.totalElements;
      })
      .addCase(fetchListings.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Fetch by ID
    builder
      .addCase(fetchListingById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchListingById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedListing = action.payload;
      })
      .addCase(fetchListingById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Fetch Nearby
    builder
      .addCase(fetchNearbyListings.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchNearbyListings.fulfilled, (state, action) => {
        state.loading = false;
        state.listings = action.payload.content;
        state.totalPages = action.payload.totalPages;
        state.currentPage = action.payload.number;
        state.totalElements = action.payload.totalElements;
      })
      .addCase(fetchNearbyListings.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Claim
    builder
      .addCase(claimListing.fulfilled, (state, action) => {
        const index = state.listings.findIndex((l) => l.id === action.payload.id);
        if (index !== -1) {
          state.listings[index] = action.payload;
        }
        if (state.selectedListing?.id === action.payload.id) {
          state.selectedListing = action.payload;
        }
      });
  },
});

export const {
  clearSelectedListing,
  clearError,
  updateListingInStore,
  removeListingFromStore,
} = foodSlice.actions;
export default foodSlice.reducer;
